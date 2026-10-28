package com.booklibrary.catalog;

import com.booklibrary.model.CatalogBook;

import java.util.List;

/**
 * The outcome of a search across all library catalogs.
 *
 * Author: Farid Rahmani
 * Contribution: Designed the result type so one failing catalog does not hide results from the others.
 * Purpose: carries the books found together with an error message for every
 * catalog that could not be searched.
 *
 * @param books  the books found, grouped by catalog
 * @param errors one message per catalog that failed, possibly empty
 */
public record CatalogSearchResult(List<CatalogBook> books, List<String> errors) {
}
