package com.booklibrary.catalog;

import com.booklibrary.model.Adaptation;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Builds the web address where a film or series can be looked up. The site
 * is JustWatch, which shows which streaming services have a title in the
 * user's country. The address is opened in the normal web browser, where the
 * user signs in to the service of their choice.
 *
 * <p>Author: Farid Rahmani (team 10), written with help from Claude (AI).
 * Purpose: one small place that knows the film-search web address.</p>
 */
public final class FilmSite {

    private FilmSite() {
    }

    /**
     * Builds the JustWatch search address for an adaptation.
     *
     * @param adaptation the film or series
     * @param country    a two-letter country code such as "NO"; blank means "US"
     * @return an {@code https} address that searches JustWatch for the title
     */
    public static String urlFor(Adaptation adaptation, String country) {
        String code = country == null || country.isBlank() ? "us" : country.toLowerCase(Locale.ROOT);
        return "https://www.justwatch.com/" + code + "/search?q="
                + URLEncoder.encode(adaptation.title(), StandardCharsets.UTF_8);
    }

    /**
     * Builds the address using the country of this computer's settings.
     *
     * @param adaptation the film or series
     * @return an {@code https} address that searches JustWatch for the title
     */
    public static String urlFor(Adaptation adaptation) {
        return urlFor(adaptation, Locale.getDefault().getCountry());
    }
}
