package bg.sofia.uni.fmi.mjt.chatty.server.repository;

import bg.sofia.uni.fmi.mjt.chatty.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.Entity;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class Repository<T extends Entity> implements RepositoryAPI<T> {

    protected final Collection<T> entities;

    public Repository() {
        entities = new LinkedHashSet<>();
    }

    public Repository(String path) {
        this();

        try (var reader = new ObjectInputStream(new FileInputStream(path))) {
            int size = reader.readInt();

            for (int i = 0; i < size; i++) {
                entities.add(Entity.loadFrom(reader));
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void add(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Value is null");
        }

        entities.add(value);
    }

    @Override
    public Collection<T> getAll() {
        return Collections.unmodifiableCollection(entities);
    }

    @Override
    public Collection<T> get(Predicate<T> criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("Criteria is null");
        }

        return entities.stream()
            .filter(criteria)
            .collect(Collectors.toSet());
    }

    @Override
    public boolean contains(Predicate<T> criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("Criteria is null");
        }

        return entities.stream().anyMatch(criteria);
    }

    @Override
    public boolean contains(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Element is null");
        }

        return entities.contains(value);
    }

    @Override
    public void remove(T value) throws ValueNotFoundException {
        if (value == null) {
            throw new IllegalArgumentException("Element is null");
        }

        if (!entities.contains(value)) {
            throw new ValueNotFoundException("Value not found");
        }

        entities.remove(value);
    }

    @Override
    public void remove(Predicate<T> criteria) throws ValueNotFoundException {
        if (!contains(criteria)) {
            throw new ValueNotFoundException("Value not found");
        }

        entities.removeAll(entities.stream().filter(criteria).toList());
    }

}
