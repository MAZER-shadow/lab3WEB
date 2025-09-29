package se.ifmo.lab3web.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<T> {
    T save(T entity);

    void delete (Long id);

    void update (T entity);

    Optional<T> findById(Long id);

    List<T> findAll();

    List<T> findAllByUserId(String userId);

    int deleteAllByUserId(String userId);
}
