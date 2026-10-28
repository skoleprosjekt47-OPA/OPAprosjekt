package com.booklibrary.model;

/**
 * Represents a physical storage location for books (a shelf in a room).
 *
 * Author: Farid Rahmani
 * Contribution: Designed the domain model and relationships between books, writers, genres and bookshelves.
 * Purpose: a simple domain entity describing where a book is physically
 * placed. One bookshelf can be linked to many {@link Book} records.
 */
public class Bookshelf {

    private int bookshelfId;
    private String name;
    private String room;
    private String shelfNumber;

    /**
     * Creates a new bookshelf that has not yet been assigned a database id
     * (used before an INSERT).
     *
     * @param name        the bookshelf's display name
     * @param room        the room the bookshelf is located in
     * @param shelfNumber the shelf identifier within the room
     */
    public Bookshelf(String name, String room, String shelfNumber) {
        this(0, name, room, shelfNumber);
    }

    /**
     * Creates a bookshelf with a known database id (used after a SELECT).
     *
     * @param bookshelfId the primary key of the bookshelf row
     * @param name        the bookshelf's display name
     * @param room        the room the bookshelf is located in
     * @param shelfNumber the shelf identifier within the room
     */
    public Bookshelf(int bookshelfId, String name, String room, String shelfNumber) {
        this.bookshelfId = bookshelfId;
        this.name = name;
        this.room = room;
        this.shelfNumber = shelfNumber;
    }

    /** Returns the bookshelf's database id. */
    public int getBookshelfId() {
        return bookshelfId;
    }

    /** Sets the bookshelf's database id. */
    public void setBookshelfId(int bookshelfId) {
        this.bookshelfId = bookshelfId;
    }

    /** Returns the bookshelf's name. */
    public String getName() {
        return name;
    }

    /** Sets the bookshelf's name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Returns the room the bookshelf is located in. */
    public String getRoom() {
        return room;
    }

    /** Sets the room the bookshelf is located in. */
    public void setRoom(String room) {
        this.room = room;
    }

    /** Returns the shelf number within the room. */
    public String getShelfNumber() {
        return shelfNumber;
    }

    /** Sets the shelf number within the room. */
    public void setShelfNumber(String shelfNumber) {
        this.shelfNumber = shelfNumber;
    }

    /**
     * Returns a display string combining the bookshelf's name, room and shelf number.
     *
     * @return a human-readable description of the bookshelf
     */
    @Override
    public String toString() {
        StringBuilder text = new StringBuilder(name);
        boolean hasRoom = room != null && !room.isBlank();
        boolean hasNumber = shelfNumber != null && !shelfNumber.isBlank();
        if (hasRoom || hasNumber) {
            text.append(" (").append(hasRoom ? room : "").append(hasRoom && hasNumber ? ", " : "")
                    .append(hasNumber ? shelfNumber : "").append(')');
        }
        return text.toString();
    }

    /** The name used when the user does not type any bookshelf. */
    public static final String DEFAULT_NAME = "My bookshelf";

    /**
     * Finds the bookshelf the user meant by a typed name, or describes a new one to create.
     *
     * @param typed    the text the user typed or picked; blank means the default shelf
     * @param existing the bookshelves that already exist
     * @return an existing shelf with the same name (ignoring case), or a new shelf with id 0
     */
    public static Bookshelf fromName(String typed, java.util.List<Bookshelf> existing) {
        String wanted = typed == null || typed.isBlank() ? DEFAULT_NAME : typed.trim();
        for (Bookshelf shelf : existing) {
            if (shelf.getName() != null && shelf.getName().equalsIgnoreCase(wanted)) {
                return shelf;
            }
        }
        return new Bookshelf(wanted, null, null);
    }
}
