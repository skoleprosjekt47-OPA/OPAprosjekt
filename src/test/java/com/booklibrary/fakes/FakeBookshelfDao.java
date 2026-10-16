package com.booklibrary.fakes;

import com.booklibrary.dao.BookshelfDao;
import com.booklibrary.model.Bookshelf;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Farid Rahmani
 * Contribution: Implemented the in-memory fake DAOs used by the unit tests, so the
 * service layer can be tested without a real MySQL database.
 * Purpose: a minimal in-memory stand-in for {@link BookshelfDao} for tests.
 */
public class FakeBookshelfDao implements BookshelfDao {

    private final List<Bookshelf> bookshelves = new ArrayList<>();

    public FakeBookshelfDao withBookshelves(List<Bookshelf> initialBookshelves) {
        bookshelves.addAll(initialBookshelves);
        return this;
    }

    @Override
    public List<Bookshelf> findAll() {
        return new ArrayList<>(bookshelves);
    }

    @Override
    public Bookshelf findById(int bookshelfId) {
        return bookshelves.stream().filter(b -> b.getBookshelfId() == bookshelfId).findFirst().orElse(null);
    }

    @Override
    public Bookshelf insert(Bookshelf bookshelf) {
        bookshelves.add(bookshelf);
        return bookshelf;
    }

    @Override
    public void update(Bookshelf bookshelf) {
    }

    @Override
    public void delete(int bookshelfId) {
        bookshelves.removeIf(b -> b.getBookshelfId() == bookshelfId);
    }
}
