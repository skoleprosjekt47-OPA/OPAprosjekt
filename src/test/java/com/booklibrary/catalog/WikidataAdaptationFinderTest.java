package com.booklibrary.catalog;

import static org.junit.jupiter.api.Assertions.*;

import com.booklibrary.model.Adaptation;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class WikidataAdaptationFinderTest {

    private static final String SEARCH = "{\"search\":["
            + "{\"id\":\"Q1\",\"description\":\"2001 film by Peter Jackson\"},"
            + "{\"id\":\"Q15228\",\"description\":\"1954 high fantasy novel by J. R. R. Tolkien\"},"
            + "{\"id\":\"Q9\",\"description\":\"novel by another author\"},"
            + "{\"id\":\"Q7\"}]}";

    private static final String SPARQL = "{\"results\":{\"bindings\":["
            + "{\"film\":{\"value\":\"http://www.wikidata.org/entity/Q127367\"},\"filmLabel\":{\"value\":\"The Fellowship of the Ring\"},"
            + "\"date\":{\"value\":\"2001-12-19T00:00:00Z\"},\"isSeries\":{\"value\":\"false\"}},"
            + "{\"film\":{\"value\":\"http://www.wikidata.org/entity/Q127367\"},\"filmLabel\":{\"value\":\"The Fellowship of the Ring\"},"
            + "\"date\":{\"value\":\"2001-12-10T00:00:00Z\"},\"isSeries\":{\"value\":\"false\"}},"
            + "{\"film\":{\"value\":\"http://www.wikidata.org/entity/Q5\"},\"filmLabel\":{\"value\":\"The Rings of Power\"},"
            + "\"date\":{\"value\":\"2022-09-01T00:00:00Z\"},\"isSeries\":{\"value\":\"true\"}},"
            + "{\"film\":{\"value\":\"http://www.wikidata.org/entity/Q6\"},\"filmLabel\":{\"value\":\"Q6\"}}]}}";

    @Test
    void picksBookItemsAndPrefersTheAuthor() {
        assertEquals(List.of("Q15228"), WikidataAdaptationFinder.bookIdsFrom(SEARCH, List.of("J. R. R. Tolkien")));
        assertEquals(List.of("Q15228", "Q9"), WikidataAdaptationFinder.bookIdsFrom(SEARCH, List.of()));
    }

    @Test
    void mergesDuplicatesSortsAndDropsUnnamed() {
        List<Adaptation> list = WikidataAdaptationFinder.adaptationsFrom(SPARQL);
        assertEquals(2, list.size());
        assertEquals("The Fellowship of the Ring", list.get(0).title());
        assertEquals(2001, list.get(0).year().intValue());
        assertEquals("series", list.get(1).type());
    }

    @Test
    void findsThroughBothQuestionsAndCaches() throws Exception {
        int[] calls = {0};
        HttpFetcher fake = url -> {
            calls[0]++;
            if (url.contains("wbsearchentities")) {
                return SEARCH;
            }
            assertTrue(url.contains("sparql"));
            assertTrue(url.contains("wd%3AQ15228"));
            return SPARQL;
        };
        WikidataAdaptationFinder finder = new WikidataAdaptationFinder(fake);
        assertEquals(2, finder.find("The Lord of the Rings", List.of("J. R. R. Tolkien")).size());
        finder.find("The Lord of the Rings", List.of("J. R. R. Tolkien"));
        assertEquals(2, calls[0]);
    }

    @Test
    void noBookItemMeansNoFilmsAndBadAnswerIsAnError() throws Exception {
        HttpFetcher none = url -> "{\"search\":[]}";
        assertTrue(new WikidataAdaptationFinder(none).find("Nothing Here", List.of()).isEmpty());
        HttpFetcher bad = url -> "<html>";
        boolean thrown = false;
        try {
            new WikidataAdaptationFinder(bad).find("Dune", List.of());
        } catch (IOException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    void subtitleIsCutAndCombineUsesOnlineResults() {
        assertEquals("Dune", WikidataAdaptationFinder.cleanTitle("Dune: Deluxe Edition (Special)"));
        AdaptationCatalog c = AdaptationCatalog.load();
        List<Adaptation> online = List.of(new Adaptation("Dune", "film", 1984, null));
        List<Adaptation> shown = c.combine("Dune", online);
        assertEquals(1, shown.size());
        assertEquals(1984, shown.get(0).year().intValue());
        assertEquals(2, c.combine("Dune", List.of()).size());
        assertTrue(c.combine("Unknown Book", List.of()).isEmpty());
    }
}
