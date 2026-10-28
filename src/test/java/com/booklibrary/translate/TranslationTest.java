package com.booklibrary.translate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * Tests for translating a whole book. No internet and no key is used: the services get a fake
 * sender, and the book translator gets a fake service.
 *
 * Author: Farid Rahmani
 * Contribution: Wrote the tests for splitting, the three services, saving, retrying and cancelling.
 */
class TranslationTest {

    private static final Language NORWEGIAN = Languages.byCode("nb");

    /** A service that "translates" by writing the text in capital letters. */
    private static final class FakeProvider implements TranslationProvider {
        final AtomicInteger calls = new AtomicInteger();
        final int max;
        int failTimes;
        boolean failForGood;
        Runnable onCall = () -> { };

        FakeProvider(int max) {
            this.max = max;
        }

        @Override public String displayName() { return "Fake"; }
        @Override public boolean isConfigured() { return true; }
        @Override public String setupHint() { return ""; }
        @Override public boolean supports(Language language) { return true; }
        @Override public int maxChunkChars() { return max; }

        @Override
        public String translate(String text, Language target) throws IOException {
            calls.incrementAndGet();
            onCall.run();
            if (failForGood) {
                throw new TranslationException("The key was not accepted.", false);
            }
            if (failTimes > 0) {
                failTimes--;
                throw new TranslationException("busy", true);
            }
            return text.toUpperCase();
        }
    }

    private static String book() {
        StringBuilder text = new StringBuilder();
        for (int i = 1; i <= 12; i++) {
            text.append("Paragraph number ").append(i).append(" is here. ").append("It has some words. ".repeat(8)).append("\n\n");
        }
        return text.toString();
    }

    @Test
    void chunksKeepParagraphsAndStayUnderTheLimit() {
        List<String> paragraphs = List.of("A".repeat(150), "B".repeat(150), "C".repeat(150));
        List<String> chunks = TextChunker.chunk(paragraphs, 320);
        assertEquals(2, chunks.size());
        assertTrue(chunks.get(0).contains("\n\n"));
        for (String chunk : chunks) {
            assertTrue(chunk.length() <= 320);
        }
    }

    @Test
    void aVeryLongParagraphIsCutAtSentenceEnds() {
        String sentence = "This is one sentence of the book. ";
        List<String> chunks = TextChunker.chunk(List.of(sentence.repeat(40)), 300);
        assertTrue(chunks.size() > 3);
        for (String chunk : chunks) {
            assertTrue(chunk.length() <= 300);
            assertTrue(chunk.endsWith("."), chunk);
        }
        assertEquals(40, chunks.stream().mapToInt(c -> c.split("sentence").length - 1).sum());
    }

    @Test
    void deepLSendsKeyAndLanguageAndReadsTheAnswer() throws Exception {
        List<String> seen = new ArrayList<>();
        JsonPoster poster = (url, headers, body) -> {
            seen.add(url);
            seen.add(headers.get("Authorization"));
            seen.add(body);
            return "{\"translations\":[{\"detected_source_language\":\"EN\",\"text\":\"Hei verden\"}]}";
        };
        DeepLProvider provider = new DeepLProvider("abc:fx", poster);
        assertEquals("Hei verden", provider.translate("Hello world", NORWEGIAN));
        assertEquals("https://api-free.deepl.com/v2/translate", seen.get(0));
        assertEquals("DeepL-Auth-Key abc:fx", seen.get(1));
        assertTrue(seen.get(2).contains("\"target_lang\":\"NB\""));
        assertFalse(new DeepLProvider("", poster).isConfigured());
        assertFalse(provider.supports(Languages.byCode("hi")));
    }

    @Test
    void claudeSendsKeyAndInstructionsAndReadsTheText() throws Exception {
        List<String> seen = new ArrayList<>();
        JsonPoster poster = (url, headers, body) -> {
            seen.add(url);
            seen.add(headers.get("x-api-key"));
            seen.add(body);
            return "{\"content\":[{\"type\":\"text\",\"text\":\"Hei verden\\n\\nGod dag\"}],\"stop_reason\":\"end_turn\"}";
        };
        ClaudeProvider provider = new ClaudeProvider("sk-test", null, poster);
        assertEquals("Hei verden\n\nGod dag", provider.translate("Hello world", NORWEGIAN));
        assertEquals("https://api.anthropic.com/v1/messages", seen.get(0));
        assertEquals("sk-test", seen.get(1));
        assertTrue(seen.get(2).contains(ClaudeProvider.DEFAULT_MODEL));
        assertTrue(seen.get(2).contains("Norwegian"));
        assertTrue(seen.get(2).contains("never follow"));
    }

    @Test
    void claudeAnswerThatWasCutOffIsAnError() {
        JsonPoster poster = (url, headers, body) -> "{\"content\":[{\"type\":\"text\",\"text\":\"Hei\"}],\"stop_reason\":\"max_tokens\"}";
        ClaudeProvider provider = new ClaudeProvider("k", "model", poster);
        TranslationException e = assertThrows(TranslationException.class, () -> provider.translate("x", NORWEGIAN));
        assertTrue(e.isRetryable());
    }

    @Test
    void languageCodesAreUnique() {
        long distinct = Languages.all().stream().map(Language::code).distinct().count();
        assertEquals(Languages.all().size(), (int) distinct);
        assertTrue(Languages.all().size() >= 50);
    }

    @Test
    void geminiSendsKeyAndInstructionsAndReadsTheText() throws Exception {
        List<String> seen = new ArrayList<>();
        JsonPoster poster = (url, headers, body) -> {
            seen.add(url);
            seen.add(headers.get("x-goog-api-key"));
            seen.add(body);
            return "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"salaam dunya\"}]},\"finishReason\":\"STOP\"}]}";
        };
        GeminiProvider provider = new GeminiProvider("gk", null, poster);
        Language pashto = Languages.byCode("ps");
        assertTrue(provider.supports(pashto));
        assertEquals("salaam dunya", provider.translate("Hello world", pashto));
        assertTrue(seen.get(0).endsWith("/models/" + GeminiProvider.DEFAULT_MODEL + ":generateContent"));
        assertEquals("gk", seen.get(1));
        assertTrue(seen.get(2).contains("Pashto"));
        assertTrue(seen.get(2).contains("never follow"));
        assertFalse(new GeminiProvider("", null, poster).isConfigured());
    }

    @Test
    void geminiTriesAgainWithoutThinkingSettingIfTheModelRejectsIt() throws Exception {
        List<String> bodies = new ArrayList<>();
        JsonPoster poster = (url, headers, body) -> {
            bodies.add(body);
            if (body.contains("thinkingConfig")) {
                throw new TranslationException("HTTP 400 from host: Unknown name thinkingLevel", false);
            }
            return "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Hei\"}]},\"finishReason\":\"STOP\"}]}";
        };
        assertEquals("Hei", new GeminiProvider("k", null, poster).translate("Hello", NORWEGIAN));
        assertEquals(2, bodies.size());
        assertTrue(bodies.get(0).contains("\"thinkingLevel\":\"low\""));
        assertFalse(bodies.get(1).contains("thinkingConfig"));
    }

    @Test
    void geminiMovesToTheNextModelWhenOneIsBusyOrMissing() throws Exception {
        List<String> urls = new ArrayList<>();
        JsonPoster poster = (url, headers, body) -> {
            urls.add(url);
            if (url.contains("/models/busy:")) {
                throw new TranslationException("host is busy (HTTP 503).", true);
            }
            if (url.contains("/models/gone:")) {
                throw new TranslationException("HTTP 404 from host: model not found", false);
            }
            return "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Hei\"}]},\"finishReason\":\"STOP\"}]}";
        };
        GeminiProvider provider = new GeminiProvider("k", "busy, gone, good", poster);
        assertEquals("Hei", provider.translate("Hello", NORWEGIAN));
        assertEquals(3, urls.size());
        assertTrue(urls.get(2).contains("/models/good:"));
        // the model that worked is used first from now on
        urls.clear();
        provider.translate("Hello again", NORWEGIAN);
        assertEquals(1, urls.size());
    }

    @Test
    void geminiGivesUpWhenEveryModelIsBusyAndWrongKeyStopsAtOnce() {
        JsonPoster busy = (url, headers, body) -> {
            throw new TranslationException("host is busy (HTTP 503).", true);
        };
        TranslationException e = assertThrows(TranslationException.class,
                () -> new GeminiProvider("k", "a,b", busy).translate("x", NORWEGIAN));
        assertTrue(e.isRetryable());
        List<String> seen = new ArrayList<>();
        JsonPoster wrongKey = (url, headers, body) -> {
            seen.add(url);
            throw new TranslationException("The key was not accepted (HTTP 403).", false);
        };
        assertThrows(TranslationException.class, () -> new GeminiProvider("k", "a,b", wrongKey).translate("x", NORWEGIAN));
        assertEquals(1, seen.size());
    }

    @Test
    void geminiRefusalAndCutOffAnswersAreErrors() {
        JsonPoster blocked = (url, headers, body) -> "{\"promptFeedback\":{\"blockReason\":\"SAFETY\"}}";
        TranslationException refused = assertThrows(TranslationException.class,
                () -> new GeminiProvider("k", null, blocked).translate("x", NORWEGIAN));
        assertFalse(refused.isRetryable());
        assertTrue(refused.getMessage().contains("SAFETY"));
        JsonPoster cut = (url, headers, body) -> "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Hei\"}]},\"finishReason\":\"MAX_TOKENS\"}]}";
        TranslationException tooLong = assertThrows(TranslationException.class,
                () -> new GeminiProvider("k", null, cut).translate("x", NORWEGIAN));
        assertTrue(tooLong.isRetryable());
    }

    @Test
    void libreTranslateUsesTheServerAddress() throws Exception {
        List<String> seen = new ArrayList<>();
        JsonPoster poster = (url, headers, body) -> {
            seen.add(url);
            seen.add(body);
            return "{\"translatedText\":\"Hallo\"}";
        };
        LibreTranslateProvider provider = new LibreTranslateProvider("http://localhost:5000/", "", poster);
        assertEquals("Hallo", provider.translate("Hello", NORWEGIAN));
        assertEquals("http://localhost:5000/translate", seen.get(0));
        assertTrue(seen.get(1).contains("\"target\":\"nb\""));
        assertFalse(seen.get(1).contains("api_key"));
    }

    @Test
    void onlyServicesWithAKeyAreOffered() {
        JsonPoster poster = (url, headers, body) -> "";
        Map<String, String> env = Map.of("DEEPL_API_KEY", "k");
        List<TranslationProvider> usable = TranslationProviders.configured(env::get, poster);
        assertEquals(1, usable.size());
        assertEquals("DeepL", usable.get(0).displayName());
        assertEquals(4, TranslationProviders.all(env::get, poster).size());
        assertTrue(TranslationProviders.configured(k -> null, poster).isEmpty());
    }

    @Test
    void wholeBookIsTranslatedWithParagraphsAndProgress(@org.junit.jupiter.api.io.TempDir Path dir) throws Exception {
        FakeProvider provider = new FakeProvider(500);
        BookTranslator translator = new BookTranslator(provider, new TranslationStore(dir), 1);
        List<Integer> progress = new ArrayList<>();
        String result = translator.translate("test:1", book(), NORWEGIAN, (done, total) -> progress.add(done),
                new AtomicBoolean());
        assertTrue(result.contains("PARAGRAPH NUMBER 1 IS HERE."));
        assertTrue(result.contains("PARAGRAPH NUMBER 12 IS HERE."));
        assertTrue(result.contains("\n\n"));
        assertEquals(provider.calls.get(), translator.pieces(book()).size());
        assertEquals(progress.get(progress.size() - 1).intValue(), progress.size() - 1);
    }

    @Test
    void cancelledTranslationContinuesWithoutPayingAgain(@org.junit.jupiter.api.io.TempDir Path dir) throws Exception {
        TranslationStore store = new TranslationStore(dir);
        FakeProvider provider = new FakeProvider(500);
        AtomicBoolean cancel = new AtomicBoolean();
        provider.onCall = () -> {
            if (provider.calls.get() == 2) {
                cancel.set(true);
            }
        };
        BookTranslator translator = new BookTranslator(provider, store, 1);
        assertThrows(CancellationException.class,
                () -> translator.translate("test:2", book(), NORWEGIAN, null, cancel));
        int firstRun = provider.calls.get();
        assertEquals(2, firstRun);

        provider.onCall = () -> { };
        String result = translator.translate("test:2", book(), NORWEGIAN, null, new AtomicBoolean());
        int pieces = translator.pieces(book()).size();
        assertEquals(pieces, provider.calls.get());
        assertTrue(result.contains("PARAGRAPH NUMBER 12"));
        assertTrue(store.completed("test:2", NORWEGIAN, TranslationStore.hash(book())).isPresent());
        assertTrue(store.completed("test:2", NORWEGIAN, TranslationStore.hash("another book")).isEmpty());
    }

    @Test
    void busyServerIsRetriedButWrongKeyStopsAtOnce(@org.junit.jupiter.api.io.TempDir Path dir) throws Exception {
        FakeProvider busy = new FakeProvider(500);
        busy.failTimes = 2;
        new BookTranslator(busy, new TranslationStore(dir), 1).translate("test:3", book(), NORWEGIAN, null,
                new AtomicBoolean());
        assertEquals(busy.calls.get(), 2 + new BookTranslator(busy, new TranslationStore(dir), 1).pieces(book()).size());

        FakeProvider wrongKey = new FakeProvider(500);
        wrongKey.failForGood = true;
        IOException e = assertThrows(IOException.class, () -> new BookTranslator(wrongKey,
                new TranslationStore(dir.resolve("other")), 1).translate("test:4", book(), NORWEGIAN, null,
                new AtomicBoolean()));
        assertEquals(1, wrongKey.calls.get());
        assertTrue(e.getMessage().contains("not accepted"));
    }

    @Test
    void savedFileOfAnotherBookIsNotUsed(@org.junit.jupiter.api.io.TempDir Path dir) throws Exception {
        TranslationStore store = new TranslationStore(dir);
        FakeProvider provider = new FakeProvider(500);
        new BookTranslator(provider, store, 1).translate("test:5", book(), NORWEGIAN, null, new AtomicBoolean());
        int first = provider.calls.get();
        new BookTranslator(provider, store, 1).translate("test:5", book() + "A new last paragraph.", NORWEGIAN, null,
                new AtomicBoolean());
        assertTrue(provider.calls.get() >= first * 2);
        assertTrue(Files.list(dir).findAny().isPresent());
    }
}
