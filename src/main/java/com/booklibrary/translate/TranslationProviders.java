package com.booklibrary.translate;

import java.util.List;
import java.util.function.Function;

/**
 * Creates the translation services from the environment variables.
 *
 * Author: Farid Rahmani
 * Contribution: Made sure that no key is ever written in the code or saved by the app.
 * Purpose: keys are read from environment variables, so they are not in the source code or in git.
 */
public final class TranslationProviders {

    private TranslationProviders() {
    }

    /**
     * Returns all known services, set up or not.
     *
     * @param env    reads an environment variable (use {@code System::getenv})
     * @param poster the HTTP sender
     * @return the services in the order Claude, Gemini, DeepL, LibreTranslate
     */
    public static List<TranslationProvider> all(Function<String, String> env, JsonPoster poster) {
        return List.of(
                new ClaudeProvider(env.apply("ANTHROPIC_API_KEY"), env.apply("CLAUDE_TRANSLATE_MODEL"), poster),
                new GeminiProvider(env.apply("GEMINI_API_KEY"), env.apply("GEMINI_TRANSLATE_MODEL"), poster),
                new DeepLProvider(env.apply("DEEPL_API_KEY"), poster),
                new LibreTranslateProvider(env.apply("LIBRETRANSLATE_URL"), env.apply("LIBRETRANSLATE_API_KEY"), poster));
    }

    /**
     * Returns only the services that have a key or address.
     *
     * @param env    reads an environment variable
     * @param poster the HTTP sender
     * @return the usable services
     */
    public static List<TranslationProvider> configured(Function<String, String> env, JsonPoster poster) {
        return all(env, poster).stream().filter(TranslationProvider::isConfigured).toList();
    }
}
