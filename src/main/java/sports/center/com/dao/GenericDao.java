package sports.center.com.dao;

import java.util.List;
import java.util.Optional;

public interface GenericDao<T> {
    void create(T entity);
    void update(long id, T entity);
    void delete(long id);
    Optional<T> findById(long id);
    List<T> findAll();
}