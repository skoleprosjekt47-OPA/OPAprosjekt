package com.booklibrary.service;

import com.booklibrary.dao.BookDao;
import com.booklibrary.dao.BookshelfDao;
import com.booklibrary.dao.GenreDao;
import com.booklibrary.dao.WriterDao;
import com.booklibrary.dao.impl.BookDaoImpl;
import com.booklibrary.dao.impl.BookshelfDaoImpl;
import com.booklibrary.dao.impl.GenreDaoImpl;
import com.booklibrary.dao.impl.WriterDaoImpl;
import com.booklibrary.model.Book;
import com.booklibrary.model.CatalogBook;
import com.booklibrary.model.Bookshelf;
import com.booklibrary.model.Genre;
import com.booklibrary.model.ReadingStatus;
import com.booklibrary.model.Writer;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Business logic for managing the private book collection.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the business-logic facade connecting the DAO layer to the JavaFX UI, including the reporting logic.
 * Purpose: the single entry point the UI (controllers) talk to. Holds no
 * SQL itself - it depends only on the DAO interfaces, so the persistence
 * technology can be swapped without changing this class or the UI.
 */
public class LibraryService {

    private final BookDao bookDao;
    private final WriterDao writerDao;
    private final GenreDao genreDao;
    private final BookshelfDao bookshelfDao;

    /**
     * Default constructor used by the application, wiring up the real
     * JDBC-backed DAO implementations.
     */
    public LibraryService() {
        this(new BookDaoImpl(), new WriterDaoImpl(), new GenreDaoImpl(), new BookshelfDaoImpl());
    }

    /**
     * Constructor that accepts DAO implementations directly, useful for
     * unit testing with fakes/mocks instead of a real database.
     */
    public LibraryService(BookDao bookDao, WriterDao writerDao, GenreDao genreDao, BookshelfDao bookshelfDao) {
        this.bookDao = bookDao;
        this.writerDao = writerDao;
        this.genreDao = genreDao;
        this.bookshelfDao = bookshelfDao;
    }

    /**
     * Returns every book in the collection, with writer, genre and
     * bookshelf populated.
     *
     * @return a list of all books
     * @throws SQLException if the underlying query fails
     */
    public List<Book> getAllBooks() throws SQLException {
        return bookDao.findAll();
    }

    /**
     * Searches the collection by title, writer, genre, bookshelf and/or
     * reading status. Any parameter left {@code null} is not filtered on.
     *
     * @param titleContains text that must appear in the title, or null
     * @param writerId      restrict to this writer's id, or null
     * @param genreId       restrict to this genre's id, or null
     * @param bookshelfId   restrict to this bookshelf's id, or null
     * @param readingStatus restrict to this reading status, or null
     * @return the list of books matching the given filters
     * @throws SQLException if the underlying query fails
     */
    public List<Book> searchBooks(String titleContains, Integer writerId, Integer genreId,
                                   Integer bookshelfId, ReadingStatus readingStatus) throws SQLException {
        String statusName = readingStatus != null ? readingStatus.name() : null;
        return bookDao.search(titleContains, writerId, genreId, bookshelfId, statusName);
    }

    /**
     * Adds a new book to the collection.
     *
     * @param book the book to add
     * @return the added book, with its generated id populated
     * @throws SQLException if the underlying insert fails
     */
    public Book addBook(Book book) throws SQLException {
        return bookDao.insert(book);
    }

    /**
     * Saves changes made to an existing book.
     *
     * @param book the book to update
     * @throws SQLException if the underlying update fails
     */
    public void updateBook(Book book) throws SQLException {
        bookDao.update(book);
    }

    /**
     * Removes a book from the collection.
     *
     * @param bookId the id of the book to remove
     * @throws SQLException if the underlying delete fails
     */
    public void deleteBook(int bookId) throws SQLException {
        bookDao.delete(bookId);
    }

    /**
     * Returns every writer, used to populate writer pickers in the UI.
     *
     * @return a list of all writers
     * @throws SQLException if the underlying query fails
     */
    public List<Writer> getAllWriters() throws SQLException {
        return writerDao.findAll();
    }

    /**
     * Adds a new writer.
     *
     * @param writer the writer to add
     * @return the added writer, with its generated id populated
     * @throws SQLException if the underlying insert fails
     */
    public Writer addWriter(Writer writer) throws SQLException {
        return writerDao.insert(writer);
    }

    /**
     * Returns every genre, used to populate genre pickers in the UI.
     *
     * @return a list of all genres
     * @throws SQLException if the underlying query fails
     */
    public List<Genre> getAllGenres() throws SQLException {
        return genreDao.findAll();
    }

    /**
     * Adds a new genre.
     *
     * @param genre the genre to add
     * @return the added genre, with its generated id populated
     * @throws SQLException if the underlying insert fails
     */
    public Genre addGenre(Genre genre) throws SQLException {
        return genreDao.insert(genre);
    }

    /**
     * Returns every bookshelf, used to populate bookshelf pickers in the UI.
     *
     * @return a list of all bookshelves
     * @throws SQLException if the underlying query fails
     */
    public List<Bookshelf> getAllBookshelves() throws SQLException {
        return bookshelfDao.findAll();
    }

    /**
     * Adds a new bookshelf.
     *
     * @param bookshelf the bookshelf to add
     * @return the added bookshelf, with its generated id populated
     * @throws SQLException if the underlying insert fails
     */
    public Bookshelf addBookshelf(Bookshelf bookshelf) throws SQLException {
        return bookshelfDao.insert(bookshelf);
    }

    /**
     * Checks whether the private library already holds a book with the same
     * title and the same first author as a catalog book.
     *
     * @param catalogBook the book found in an external catalog
     * @return {@code true} if a book with the same title and writer already exists
     * @throws SQLException if the database cannot be read
     */
    public boolean isInLibrary(CatalogBook catalogBook) throws SQLException {
        String author = catalogBook.firstAuthor();
        return bookDao.findAll().stream().anyMatch(b ->
                b.getTitle().equalsIgnoreCase(catalogBook.title())
                        && (author == null
                        ? b.getWriter() == null
                        : b.getWriter() != null && b.getWriter().getName().equalsIgnoreCase(author)));
    }

    /**
     * Adds a book found in an external catalog to the private library. The
     * writer and genre are reused if they exist and created otherwise.
     *
     * @param catalogBook the book found in an external catalog
     * @param bookshelf   the bookshelf the book is placed on
     * @param status      the reading status to start with
     * @return the saved book, including its new id
     * @throws SQLException if the database cannot be read or written
     */
    public Book importCatalogBook(CatalogBook catalogBook, Bookshelf bookshelf, ReadingStatus status)
            throws SQLException {
        Writer writer = findOrCreateWriter(catalogBook.firstAuthor());
        Genre genre = findOrCreateGenre(catalogBook.firstSubject());
        String title = shorten(catalogBook.title(), 255);
        int year = catalogBook.year() == null ? 0 : catalogBook.year();
        return bookDao.insert(new Book(title, year, status, writer, genre, bookshelf));
    }

    /**
     * Finds a writer by name (ignoring case) or creates it.
     *
     * @param name the writer name, may be {@code null}
     * @return the existing or new writer, or {@code null} if no name was given
     * @throws SQLException if the database cannot be read or written
     */
    private Writer findOrCreateWriter(String name) throws SQLException {
        if (name == null || name.isBlank()) {
            return null;
        }
        String wanted = shorten(name.trim(), 150);
        for (Writer existing : writerDao.findAll()) {
            if (existing.getName().equalsIgnoreCase(wanted)) {
                return existing;
            }
        }
        return writerDao.insert(new Writer(wanted, null));
    }

    /**
     * Finds a genre by name (ignoring case) or creates it.
     *
     * @param name the genre name, may be {@code null}
     * @return the existing or new genre, or {@code null} if no name was given
     * @throws SQLException if the database cannot be read or written
     */
    private Genre findOrCreateGenre(String name) throws SQLException {
        if (name == null || name.isBlank()) {
            return null;
        }
        String wanted = shorten(name.trim(), 100);
        for (Genre existing : genreDao.findAll()) {
            if (existing.getName().equalsIgnoreCase(wanted)) {
                return existing;
            }
        }
        return genreDao.insert(new Genre(wanted, null));
    }

    /**
     * Cuts a text to a maximum length so it fits the database column.
     *
     * @param text      the text
     * @param maxLength the maximum number of characters
     * @return the text, shortened if it was too long
     */
    private static String shorten(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    /**
     * Builds a simple summary report: how many books fall into each
     * reading status, each genre, and each writer. Used by the Reports
     * tab in the UI.
     *
     * @return the aggregated {@link LibrarySummary} counts
     * @throws SQLException if the underlying query fails
     */
    public LibrarySummary buildSummary() throws SQLException {
        List<Book> books = bookDao.findAll();

        Map<String, Long> byStatus = books.stream()
                .collect(Collectors.groupingBy(b -> b.getReadingStatus().getDisplayLabel(), Collectors.counting()));

        Map<String, Long> byGenre = books.stream()
                .filter(b -> b.getGenre() != null)
                .collect(Collectors.groupingBy(b -> b.getGenre().getName(), Collectors.counting()));

        Map<String, Long> byWriter = books.stream()
                .filter(b -> b.getWriter() != null)
                .collect(Collectors.groupingBy(b -> b.getWriter().getName(), Collectors.counting()));

        return new LibrarySummary(books.size(), byStatus, byGenre, byWriter);
    }

    /**
     * Simple read-only holder for the numbers shown in the Reports tab.
     *
     * Author: Farid Rahmani
 * Contribution: Implemented the business-logic facade connecting the DAO layer to the JavaFX UI, including the reporting logic.
     * Purpose: carries the aggregated counts computed by
     * {@link LibraryService#buildSummary()} to the UI layer.
     */
    public record LibrarySummary(int totalBooks, Map<String, Long> byReadingStatus,
                                  Map<String, Long> byGenre, Map<String, Long> byWriter) {
    }
}
