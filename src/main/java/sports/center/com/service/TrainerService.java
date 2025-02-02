package sports.center.com.service;

import sports.center.com.model.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerService {
    void create(Trainer trainer);

    void update(long id, Trainer trainer);

    Optional<Trainer> getById(long id);

    List<Trainer> getAll();

    void delete(long id);
}