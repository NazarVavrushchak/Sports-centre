package sports.center.com.service;

import sports.center.com.model.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeService {
    void create(Trainee trainee);

    void update(long id, Trainee trainee);

    void delete(long id);

    Optional<Trainee> getById(long id);

    List<Trainee> getAll();
}