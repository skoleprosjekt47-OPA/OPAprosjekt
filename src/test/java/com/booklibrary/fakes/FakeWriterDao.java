package com.booklibrary.fakes;

import com.booklibrary.dao.WriterDao;
import com.booklibrary.model.Writer;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Farid Rahmani
 * Contribution: Implemented the in-memory fake DAOs used by the unit tests, so the
 * service layer can be tested without a real MySQL database.
 * Purpose: a minimal in-memory stand-in for {@link WriterDao} for tests
 * that only need to list writers, not fully manage them.
 */
public class FakeWriterDao implements WriterDao {

    private final List<Writer> writers = new ArrayList<>();

    public FakeWriterDao withWriters(List<Writer> initialWriters) {
        writers.addAll(initialWriters);
        return this;
    }

    @Override
    public List<Writer> findAll() {
        return new ArrayList<>(writers);
    }

    @Override
    public Writer findById(int writerId) {
        return writers.stream().filter(w -> w.getWriterId() == writerId).findFirst().orElse(null);
    }

    @Override
    public Writer insert(Writer writer) {
        writers.add(writer);
        return writer;
    }

    @Override
    public void update(Writer writer) {
    }

    @Override
    public void delete(int writerId) {
        writers.removeIf(w -> w.getWriterId() == writerId);
    }
}
