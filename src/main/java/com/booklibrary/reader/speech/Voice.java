package com.booklibrary.reader.speech;

import java.util.Locale;

/**
 * A voice that can read text aloud.
 *
 * @param name     the name the operating system uses for the voice, for example "Samantha"
 * @param language the language code such as "en_US" or "nb_NO"
 */
public record Voice(String name, String language) {

    /**
     * Returns the language in words.
     *
     * @return for example "English" or "Norwegian Bokmål"
     */
    public String languageName() {
        Locale locale = Locale.forLanguageTag(language.replace('_', '-'));
        String text = locale.getDisplayLanguage(Locale.ENGLISH);
        return text.isBlank() ? language : text;
    }

    @Override
    public String toString() {
        return name + "  -  " + languageName() + " (" + language + ")";
    }
}
