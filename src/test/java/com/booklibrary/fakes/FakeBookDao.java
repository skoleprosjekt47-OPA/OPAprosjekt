package com.booklibrary.fakes;

import com.booklibrary.dao.BookDao;
import com.booklibrary.model.Book;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Farid Rahmani
 * Contribution: Implemented the in-memory fake DAOs used by the unit tests, so the
 * service layer can be tested without a real MySQL database.
 * Purpose: an in-memory stand-in for {@link BookDao}, used by
 * {@code LibraryServiceTest} to test business logic in isolation from
 * the real MySQL database. Only implements as much behavior as the
 * tests need.
 */
public class FakeBookDao implements BookDao {

    private final List<Book> books = new ArrayList<>();
    private int nextId = 1;

    public FakeBookDao withBooks(List<Book> initialBooks) {
        books.addAll(initialBooks);
        return this;
    }

    @Override
    public List<Book> findAll() {
        return new ArrayList<>(books);
    }

    @Override
    public Book findById(int bookId) {
        return books.stream().filter(b -> b.getBookId() == bookId).findFirst().orElse(null);
    }

    @Override
    public List<Book> search(String titleContains, Integer writerId, Integer genreId,
                              Integer bookshelfId, String readingStatus) {
        return books.stream()
                .filter(b -> titleContains == null || titleContains.isBlank()
                        || b.getTitle().toLowerCase().contains(titleContains.toLowerCase()))
                .filter(b -> writerId == null || (b.getWriter() != null && b.getWriter().getWriterId() == writerId))
                .filter(b -> genreId == null || (b.getGenre() != null && b.getGenre().getGenreId() == genreId))
                .filter(b -> bookshelfId == null || (b.getBookshelf() != null && b.getBookshelf().getBookshelfId() == bookshelfId))
                .filter(b -> readingStatus == null || b.getReadingStatus().name().equals(readingStatus))
                .toList();
    }

    @Override
    public Book insert(Book book) {
        book.setBookId(nextId++);
        books.add(book);
        return book;
    }

    @Override
    public void update(Book book) {
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getBookId() == book.getBookId()) {
                books.set(i, book);
                return;
            }
        }
    }

    @Override
    public void delete(int bookId) {
        books.removeIf(b -> b.getBookId() == bookId);
    }
}
