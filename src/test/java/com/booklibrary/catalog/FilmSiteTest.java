package com.booklibrary.catalog;

import static org.junit.jupiter.api.Assertions.*;

import com.booklibrary.model.Adaptation;
import org.junit.jupiter.api.Test;

class FilmSiteTest {

    private final Adaptation film = new Adaptation("The Two Towers", "film", 2002, null);

    @Test
    void addressUsesTheCountryAndEncodesTheTitle() {
        assertEquals("https://www.justwatch.com/no/search?q=The+Two+Towers", FilmSite.urlFor(film, "NO"));
    }

    @Test
    void blankCountryFallsBackToUs() {
        assertEquals("https://www.justwatch.com/us/search?q=The+Two+Towers", FilmSite.urlFor(film, ""));
        assertEquals("https://www.justwatch.com/us/search?q=The+Two+Towers", FilmSite.urlFor(film, null));
    }
}
