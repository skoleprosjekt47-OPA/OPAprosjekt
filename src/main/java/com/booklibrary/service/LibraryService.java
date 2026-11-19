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
import com.booklibrary.model.Bookshelf;
import com.booklibrary.model.Genre;
import com.booklibrary.model.ReadingStatus;
import com.booklibrary.model.Writer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
     * @param titleContains text that must appear in the title or in the writer's name, or null
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
     * Updates an existing writer.
     *
     * @param writer the writer to update, identified by its id
     * @throws SQLException if the database update fails
     */
    public void updateWriter(Writer writer) throws SQLException {
        writerDao.update(writer);
    }

    /**
     * Deletes a writer. Books by this writer stay in the library with no writer
     * (the database uses ON DELETE SET NULL).
     *
     * @param writerId id of the writer to delete
     * @throws SQLException if the database delete fails
     */
    public void deleteWriter(int writerId) throws SQLException {
        writerDao.delete(writerId);
    }

    /**
     * Updates an existing genre.
     *
     * @param genre the genre to update, identified by its id
     * @throws SQLException if the database update fails
     */
    public void updateGenre(Genre genre) throws SQLException {
        genreDao.update(genre);
    }

    /**
     * Deletes a genre. Books in this genre stay in the library with no genre.
     *
     * @param genreId id of the genre to delete
     * @throws SQLException if the database delete fails
     */
    public void deleteGenre(int genreId) throws SQLException {
        genreDao.delete(genreId);
    }

    /**
     * Updates an existing bookshelf.
     *
     * @param bookshelf the bookshelf to update, identified by its id
     * @throws SQLException if the database update fails
     */
    public void updateBookshelf(Bookshelf bookshelf) throws SQLException {
        bookshelfDao.update(bookshelf);
    }

    /**
     * Deletes a bookshelf. Books on this shelf stay in the library with no shelf.
     *
     * @param bookshelfId id of the bookshelf to delete
     * @throws SQLException if the database delete fails
     */
    public void deleteBookshelf(int bookshelfId) throws SQLException {
        bookshelfDao.delete(bookshelfId);
    }

    /**
     * Turns a list of books into CSV text with a header row. Values that contain
     * a comma, a quote or a line break are wrapped in quotes.
     *
     * @param books the books to convert
     * @return the CSV text
     */
    public static String toCsv(List<Book> books) {
        StringBuilder csv = new StringBuilder("Title,Year,Writer,Genre,Bookshelf,Status\n");
        for (Book book : books) {
            csv.append(csvValue(book.getTitle())).append(',')
               .append(book.getReleaseYear()).append(',')
               .append(csvValue(book.getWriter() != null ? book.getWriter().getName() : "")).append(',')
               .append(csvValue(book.getGenre() != null ? book.getGenre().getName() : "")).append(',')
               .append(csvValue(book.getBookshelf() != null ? book.getBookshelf().getName() : "")).append(',')
               .append(csvValue(book.getReadingStatus().getDisplayLabel())).append('\n');
        }
        return csv.toString();
    }

    private static String csvValue(String value) {
        String text = value == null ? "" : value;
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    /**
     * Writes every book in the library to a CSV file (UTF-8).
     *
     * @param file the file to write; it is created or overwritten
     * @return the number of books written
     * @throws SQLException if the books cannot be read from the database
     * @throws IOException  if the file cannot be written
     */
    public int exportBooksToCsv(Path file) throws SQLException, IOException {
        List<Book> books = bookDao.findAll();
        Files.writeString(file, toCsv(books), StandardCharsets.UTF_8);
        return books.size();
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
