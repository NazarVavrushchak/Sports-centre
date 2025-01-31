package sports.center.com.service;

import sports.center.com.model.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeService {
    void create(Trainee trainee);
    void updateTrainee(long id, Trainee trainee);
    void deleteTrainee(long id);
    Optional<Trainee> getById(long id);
    List<Trainee> getAll();
}