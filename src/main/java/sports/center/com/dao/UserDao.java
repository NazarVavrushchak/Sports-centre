package sports.center.com.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import sports.center.com.model.User;
import sports.center.com.storage.InMemoryStorage;

import java.util.*;

@Repository
public class UserDao implements GenericDao<User> {
    private static final String NAMESPACE = "user";
    private final InMemoryStorage storage;

    @Autowired
    public UserDao(InMemoryStorage storage) {
        this.storage = storage;
    }

    @Override
    public void create(User entity) {
        Map<Long , User> namespace = storage.getNamespace(NAMESPACE);

        long newId = namespace.keySet().stream()
                .mapToLong(value -> value)
                .max()
                .orElse(0L) + 1;
        entity.setId(newId);
        namespace.put(newId, entity);
    }

    @Override
    public void update(long id, User entity) {
        Map<Long , User> namespace = storage .getNamespace(NAMESPACE);
        if (!namespace.containsKey(id)) {
            throw new NoSuchElementException("User with id " + id + " not found.");
        }
        namespace.put(id , entity);
    }

    @Override
    public void delete(long id) {
        Map<Long, User> namespace = storage.getNamespace(NAMESPACE);
        namespace.remove(id);
    }

    @Override
    public Optional<User> findById(long id) {
        Map<Long, User> namespace = storage.getNamespace(NAMESPACE);
        return Optional.ofNullable(namespace.get(id));
    }

    @Override
    public List<User> findAll() {
        Map<Long, User> namespace = storage.getNamespace(NAMESPACE);
        return new ArrayList<>(namespace.values());
    }
}