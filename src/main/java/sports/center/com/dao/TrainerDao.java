package sports.center.com.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import sports.center.com.model.Trainer;
import sports.center.com.storage.InMemoryStorage;

import java.util.*;

@Repository
public class TrainerDao implements GenericDao<Trainer> {
    private static final String NAMESPACE = "trainer";
    private final InMemoryStorage storage;

    @Autowired
    public TrainerDao(InMemoryStorage storage) {
        this.storage = storage;
    }

    @Override
    public void create(Trainer entity) {
        Map<Long, Trainer> namespace = storage.getNamespace(NAMESPACE);

        long newId = namespace.keySet().stream()
                .mapToLong(value -> value)
                .max()
                .orElse(0L) + 1;

        entity.setId(newId);
        namespace.put(newId, entity);
    }

    @Override
    public void update(long id, Trainer entity) {
        Map<Long, Trainer> namespace = storage.getNamespace(NAMESPACE);
        if (!namespace.containsKey(id)) {
            throw new NoSuchElementException("Trainer with id " + id + " not found.");
        }
        namespace.put(id, entity);
    }

    @Override
    public void delete(long id) {
        Map<Long, Trainer> namespace = storage.getNamespace(NAMESPACE);
        namespace.remove(id);
    }

    @Override
    public Optional<Trainer> findById(long id) {
        Map<Long, Trainer> namespace = storage.getNamespace(NAMESPACE);
        return Optional.ofNullable(namespace.get(id));
    }

    @Override
    public List<Trainer> findAll() {
        Map<Long, Trainer> namespace = storage.getNamespace(NAMESPACE);
        return new ArrayList<>(namespace.values());
    }
}