package com.booklibrary.fakes;

import com.booklibrary.dao.GenreDao;
import com.booklibrary.model.Genre;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Farid Rahmani
 * Contribution: Implemented the in-memory fake DAOs used by the unit tests, so the
 * service layer can be tested without a real MySQL database.
 * Purpose: a minimal in-memory stand-in for {@link GenreDao} for tests.
 */
public class FakeGenreDao implements GenreDao {

    private final List<Genre> genres = new ArrayList<>();

    public FakeGenreDao withGenres(List<Genre> initialGenres) {
        genres.addAll(initialGenres);
        return this;
    }

    @Override
    public List<Genre> findAll() {
        return new ArrayList<>(genres);
    }

    @Override
    public Genre findById(int genreId) {
        return genres.stream().filter(g -> g.getGenreId() == genreId).findFirst().orElse(null);
    }

    @Override
    public Genre insert(Genre genre) {
        genres.add(genre);
        return genre;
    }

    @Override
    public void update(Genre genre) {
    }

    @Override
    public void delete(int genreId) {
        genres.removeIf(g -> g.getGenreId() == genreId);
    }
}
