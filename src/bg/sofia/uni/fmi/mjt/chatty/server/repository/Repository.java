package bg.sofia.uni.fmi.mjt.chatty.server.repository;

import bg.sofia.uni.fmi.mjt.chatty.server.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.Entity;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class Repository<T extends Entity> implements RepositoryAPI<T> {

    private static final String BASE_PATH = "./src/bg/sofia/uni/fmi/mjt/chatty/server/db/";
    private String dbPath;

    protected Collection<T> entities;

    private Repository() {
        entities = new LinkedHashSet<>();
    }

    public Repository(String path) {
        this();

        dbPath = BASE_PATH + path;

        try (var fileStream = new FileInputStream(dbPath)) {
            readEntities(fileStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Repository(InputStream stream) {
        this();

        readEntities(stream);
    }

    @Override
    public void add(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Value is null");
        }

        entities.add(value);

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

        saveEntities();
    }

    @Override
    public void remove(Predicate<T> criteria) throws ValueNotFoundException {
        if (!contains(criteria)) {
            throw new ValueNotFoundException("Value not found");
        }

        entities.removeAll(entities.stream().filter(criteria).toList());

        saveEntities();
    }

    public void saveEntities() {
        if (dbPath == null) {
            return;
        }

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

    private void readEntities(InputStream stream) {
        try (var reader = new ObjectInputStream(stream)) {
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

}
