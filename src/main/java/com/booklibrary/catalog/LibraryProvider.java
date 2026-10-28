package com.booklibrary.catalog;

import com.booklibrary.model.CatalogBook;

import java.io.IOException;
import java.util.List;

/**
 * A searchable library catalog (Strategy pattern).
 *
 * Author: Farid Rahmani
 * Contribution: Designed the provider interface so more libraries can be added without changing the service or the UI.
 * Purpose: every catalog (Open Library, Google Books, ...) implements this
 * interface, so {@link CatalogService} can search all of them the same way.
 */
public interface LibraryProvider {

    /**
     * Returns the display name of this library catalog.
     *
     * @return the catalog name, for example "Open Library"
     */
    String getName();

    /**
     * Searches the catalog for books matching a text.
     *
     * @param query the search text, for example a title or an author
     * @return the matching books, possibly empty
     * @throws IOException          if the catalog cannot be reached or returns an error
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    List<CatalogBook> search(String query) throws IOException, InterruptedException;
}
