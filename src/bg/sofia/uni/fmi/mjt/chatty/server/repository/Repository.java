package bg.sofia.uni.fmi.mjt.chatty.server.repository;

import bg.sofia.uni.fmi.mjt.chatty.server.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.Entity;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class Repository<T extends Entity> implements RepositoryAPI<T> {

    private static final String BASE_PATH = "./src/bg/sofia/uni/fmi/mjt/chatty/server/db/";

    private final String dbPath;

    protected final Collection<T> entities;

    public Repository(String path) {
        entities = new LinkedHashSet<>();
        dbPath = BASE_PATH + path;

        try (var reader = new ObjectInputStream(new FileInputStream(dbPath))) {
            if (reader.available() > 0) {
                int size = reader.readInt();

                for (int i = 0; i < size; i++) {
                    entities.add(Entity.loadFrom(reader));
                }
            }
        } catch (EOFException ignored) {
            // if the db files are empty
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Incorrect data format");
        }
    }

    @Override
    public void add(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Value is null");
        }

        synchronized (entities) {
            entities.add(value);
        }

        saveEntities();
    }

    @Override
    public synchronized Collection<T> getAll() {
        return Collections.unmodifiableCollection(entities);
    }

    @Override
    public Collection<T> get(Predicate<T> criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("Criteria is null");
        }

        synchronized (entities) {
            return entities.stream()
                    .filter(criteria)
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public boolean contains(Predicate<T> criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("Criteria is null");
        }

        synchronized (entities) {
            return entities.stream().anyMatch(criteria);
        }
    }

    @Override
    public boolean contains(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Element is null");
        }

        synchronized (entities) {
            return entities.contains(value);
        }
    }

    @Override
    public void remove(T value) throws ValueNotFoundException {
        if (value == null) {
            throw new IllegalArgumentException("Element is null");
        }

        synchronized (entities) {
            if (!entities.contains(value)) {
                throw new ValueNotFoundException("Value not found");
            }

            entities.remove(value);
        }

        saveEntities();
    }

    @Override
    public void remove(Predicate<T> criteria) throws ValueNotFoundException {
        if (!contains(criteria)) {
            throw new ValueNotFoundException("Value not found");
        }

        synchronized (entities) {
            entities.removeAll(entities.stream().filter(criteria).toList());
        }

        saveEntities();
    }

    public void saveEntities() {
        try (var stream = new ObjectOutputStream(new FileOutputStream(dbPath))) {
            if (entities.isEmpty()) {
                stream.flush();
            }

            stream.writeInt(entities.size());
            entities.forEach(e -> e.saveTo(stream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
