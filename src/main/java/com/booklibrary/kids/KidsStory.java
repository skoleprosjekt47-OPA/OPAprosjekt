package com.booklibrary.kids;

import java.util.List;

/**
 * A children's story with animated pages.
 *
 * @param title   the title
 * @param summary one short line for the picker
 * @param pages   the pages in order
 */
public record KidsStory(String title, String summary, List<KidsPage> pages) {

    @Override
    public String toString() {
        return title;
    }
}
