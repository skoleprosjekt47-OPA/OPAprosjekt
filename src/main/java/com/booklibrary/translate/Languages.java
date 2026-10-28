package com.booklibrary.translate;

import java.util.List;

/**
 * The list of target languages.
 *
 * Author: Farid Rahmani
 * Contribution: Collected the languages and their codes for the translation services.
 * Purpose: new languages are added here and appear in the app without other changes.
 */
public final class Languages {

    private static final List<Language> ALL = List.of(
            new Language("Norwegian", "nb", "NB", false),
            new Language("English", "en", "EN-GB", false),
            new Language("Spanish", "es", "ES", false),
            new Language("French", "fr", "FR", false),
            new Language("German", "de", "DE", false),
            new Language("Italian", "it", "IT", false),
            new Language("Portuguese", "pt", "PT-PT", false),
            new Language("Dutch", "nl", "NL", false),
            new Language("Swedish", "sv", "SV", false),
            new Language("Danish", "da", "DA", false),
            new Language("Finnish", "fi", "FI", false),
            new Language("Polish", "pl", "PL", false),
            new Language("Czech", "cs", "CS", false),
            new Language("Slovak", "sk", "SK", false),
            new Language("Hungarian", "hu", "HU", false),
            new Language("Romanian", "ro", "RO", false),
            new Language("Bulgarian", "bg", "BG", false),
            new Language("Greek", "el", "EL", false),
            new Language("Estonian", "et", "ET", false),
            new Language("Latvian", "lv", "LV", false),
            new Language("Lithuanian", "lt", "LT", false),
            new Language("Slovenian", "sl", "SL", false),
            new Language("Croatian", "hr", null, false),
            new Language("Serbian", "sr", null, false),
            new Language("Turkish", "tr", "TR", false),
            new Language("Russian", "ru", "RU", false),
            new Language("Ukrainian", "uk", "UK", false),
            new Language("Hebrew", "he", null, false),
            new Language("Arabic", "ar", "AR", false),
            new Language("Persian (Farsi)", "fa", null, false),
            new Language("Dari (Afghan Persian)", "prs", null, true),
            new Language("Pashto", "ps", null, true),
            new Language("Kurdish (Sorani)", "ckb", null, true),
            new Language("Urdu", "ur", null, false),
            new Language("Hindi", "hi", null, false),
            new Language("Bengali", "bn", null, false),
            new Language("Punjabi", "pa", null, true),
            new Language("Nepali", "ne", null, false),
            new Language("Tamil", "ta", null, false),
            new Language("Telugu", "te", null, false),
            new Language("Tigrinya", "ti", null, true),
            new Language("Amharic", "am", null, true),
            new Language("Somali", "so", null, true),
            new Language("Swahili", "sw", null, false),
            new Language("Japanese", "ja", "JA", false),
            new Language("Korean", "ko", "KO", false),
            new Language("Chinese", "zh", "ZH", false),
            new Language("Vietnamese", "vi", null, false),
            new Language("Thai", "th", null, false),
            new Language("Indonesian", "id", "ID", false),
            new Language("Malay", "ms", null, false),
            new Language("Filipino (Tagalog)", "tl", null, false));

    private Languages() {
    }

    /**
     * Returns all languages.
     *
     * @return the languages, in the order shown in the app
     */
    public static List<Language> all() {
        return ALL;
    }

    /**
     * Finds a language by its code.
     *
     * @param code for example "nb"
     * @return the language, or {@code null} if it is unknown
     */
    public static Language byCode(String code) {
        for (Language language : ALL) {
            if (language.code().equalsIgnoreCase(code)) {
                return language;
            }
        }
        return null;
    }
}
