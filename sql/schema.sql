-- Private Book Library Manager
-- Database schema and sample data
-- Run this script in MySQL before starting the application.

CREATE DATABASE IF NOT EXISTS book_library CHARACTER SET utf8mb4;
USE book_library;

DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS writer;
DROP TABLE IF EXISTS genre;
DROP TABLE IF EXISTS bookshelf;

CREATE TABLE writer (
    writer_id    INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(150) NOT NULL,
    nationality  VARCHAR(100)
);

CREATE TABLE genre (
    genre_id     INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    description  VARCHAR(255)
);

CREATE TABLE bookshelf (
    bookshelf_id INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    room         VARCHAR(100),
    shelf_number VARCHAR(20)
);

CREATE TABLE book (
    book_id         INT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    release_year    INT,
    reading_status  ENUM('READ', 'UNREAD', 'READING') NOT NULL DEFAULT 'UNREAD',
    writer_id       INT,
    genre_id        INT,
    bookshelf_id    INT,
    CONSTRAINT fk_book_writer    FOREIGN KEY (writer_id)    REFERENCES writer(writer_id)       ON DELETE SET NULL,
    CONSTRAINT fk_book_genre     FOREIGN KEY (genre_id)     REFERENCES genre(genre_id)         ON DELETE SET NULL,
    CONSTRAINT fk_book_bookshelf FOREIGN KEY (bookshelf_id) REFERENCES bookshelf(bookshelf_id) ON DELETE SET NULL
);

-- Sample data

INSERT INTO writer (name, nationality) VALUES
    ('George Orwell', 'British'),
    ('Jane Austen', 'British'),
    ('Haruki Murakami', 'Japanese'),
    ('Agatha Christie', 'British');

INSERT INTO genre (name, description) VALUES
    ('Dystopian', 'Fiction set in a bleak imagined future or society'),
    ('Classic', 'Widely recognized works of enduring literary quality'),
    ('Mystery', 'Fiction centered on solving a crime or puzzle'),
    ('Magical Realism', 'Fiction blending realistic narrative with magical elements');

INSERT INTO bookshelf (name, room, shelf_number) VALUES
    ('Living room shelf', 'Living room', 'A1'),
    ('Office shelf', 'Home office', 'B2'),
    ('Bedroom shelf', 'Bedroom', 'C1');

INSERT INTO book (title, release_year, reading_status, writer_id, genre_id, bookshelf_id) VALUES
    ('1984', 1949, 'READ', 1, 1, 1),
    ('Pride and Prejudice', 1813, 'READ', 2, 2, 2),
    ('Norwegian Wood', 1987, 'READING', 3, 4, 3),
    ('Murder on the Orient Express', 1934, 'UNREAD', 4, 3, 1);
