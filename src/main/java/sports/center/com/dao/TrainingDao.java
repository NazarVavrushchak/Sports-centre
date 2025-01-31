package sports.center.com.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import sports.center.com.model.Training;
import sports.center.com.storage.InMemoryStorage;

import java.util.*;

@Repository
public class TrainingDao implements GenericDao<Training> {
    private static final String NAMESPACE = "training";
    private final InMemoryStorage storage;

    @Autowired
    public TrainingDao(InMemoryStorage storage) {
        this.storage = storage;
    }

    @Override
    public void create(Training entity) {
        Map<Long, Training> namespace = storage.getNamespace(NAMESPACE);
        long newId = namespace.keySet().stream()
                .mapToLong(value -> value)
                .max()
                .orElse(0L) + 1;

        entity.setId(newId);
        namespace.put(newId, entity);
    }

    @Override
    public void update(long id, Training entity) {
        Map<Long, Training> namespace = storage.getNamespace(NAMESPACE);
        if (!namespace.containsKey(id)) {
            throw new NoSuchElementException("Training with id " + id + " not found.");
        }
        namespace.put(id, entity);
    }

    @Override
    public void delete(long id) {
        Map<Long, Training> namespace = storage.getNamespace(NAMESPACE);
        namespace.remove(id);
    }

    @Override
    public Optional<Training> findById(long id) {
        Map<Long, Training> namespace = storage.getNamespace(NAMESPACE);
        return Optional.ofNullable(namespace.get(id));
    }

    @Override
    public List<Training> findAll() {
        Map<Long, Training> namespace = storage.getNamespace(NAMESPACE);
        return new ArrayList<>(namespace.values());
    }
}