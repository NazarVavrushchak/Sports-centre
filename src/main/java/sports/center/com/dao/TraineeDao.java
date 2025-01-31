package sports.center.com.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import sports.center.com.model.Trainee;
import sports.center.com.storage.InMemoryStorage;

import java.util.*;

@Repository
public class TraineeDao implements GenericDao<Trainee> {
    private static final String NAMESPACE = "trainee";
    private final InMemoryStorage storage;

    @Autowired
    public TraineeDao(InMemoryStorage storage) {
        this.storage = storage;
    }

    @Override
    public void create(Trainee entity) {
        Map<Long, Trainee> namespace = storage.getNamespace(NAMESPACE);

        long newId = namespace.keySet().stream()
                .mapToLong(value -> value)
                .max()
                .orElse(0L) + 1;

        entity.setId(newId);
        namespace.put(newId, entity);
    }

    @Override
    public void update(long id, Trainee entity) {
        Map<Long, Trainee> namespace = storage.getNamespace(NAMESPACE);
        if (!namespace.containsKey(id)) {
            throw new NoSuchElementException("Trainee with id " + id + " not found.");
        }
        namespace.put(id, entity);
    }

    @Override
    public void delete(long id) {
        Map<Long, Trainee> namespace = storage.getNamespace(NAMESPACE);
        namespace.remove(id);
    }

    @Override
    public Optional<Trainee> findById(long id) {
        Map<Long, Trainee> namespace = storage.getNamespace(NAMESPACE);
        return Optional.ofNullable(namespace.get(id));
    }

    @Override
    public List<Trainee> findAll() {
        Map<Long, Trainee> namespace = storage.getNamespace(NAMESPACE);
        return new ArrayList<>(namespace.values());
    }
}