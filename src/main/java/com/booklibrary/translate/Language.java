package com.booklibrary.translate;

/**
 * A language the book can be translated into.
 *
 * @param name        the English name shown in the list
 * @param code        the language code such as "nb" or "es" (ISO 639)
 * @param deeplCode   the code DeepL uses for this language, or {@code null} if DeepL does not offer it
 * @param lowResource {@code true} for languages with little written material online, where AI translation is
 *                    weaker and a native speaker should check the result
 */
public record Language(String name, String code, String deeplCode, boolean lowResource) {

    @Override
    public String toString() {
        return name;
    }
}
