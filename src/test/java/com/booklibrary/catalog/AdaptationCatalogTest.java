package com.booklibrary.catalog;

import static org.junit.jupiter.api.Assertions.*;

import com.booklibrary.model.Adaptation;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdaptationCatalogTest {

    @Test
    void bundledListFindsTheLordOfTheRingsFilms() {
        List<Adaptation> found = AdaptationCatalog.load().forBook("The Lord of the Rings");
        assertEquals(3, found.size());
    }

    @Test
    void matchingUsesWholeWordsAndHandlesNull() {
        AdaptationCatalog c = AdaptationCatalog.load();
        assertTrue(c.forBook("Dunedin Days").isEmpty());
        assertFalse(c.forBook("Dune").isEmpty());
        assertTrue(c.forBook(null).isEmpty());
    }

    @Test
    void onlineResultsWinAndBundledListIsTheOfflineFallback() {
        AdaptationCatalog c = AdaptationCatalog.load();
        List<Adaptation> online = List.of(new Adaptation("Dune", "film", 1984, null));
        assertEquals(1, c.combine("Dune", online).size());
        assertEquals(2, c.combine("Dune", List.of()).size());
        assertTrue(c.combine("Unknown Book", List.of()).isEmpty());
    }
}
