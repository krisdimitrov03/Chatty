package bg.sofia.uni.fmi.mjt.chatty.server.repository;

import bg.sofia.uni.fmi.mjt.chatty.server.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.Entity;

import java.util.Collection;
import java.util.function.Predicate;

public interface RepositoryAPI<T extends Entity> {

    void add(T value);

    Collection<T> getAll();

    Collection<T> get(Predicate<T> criteria);

    boolean contains(Predicate<T> criteria);

    boolean contains(T value);

    void remove(T value) throws ValueNotFoundException;

    void remove(Predicate<T> criteria) throws ValueNotFoundException;

}