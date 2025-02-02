package sports.center.com.service;

import sports.center.com.model.Training;

import java.util.List;
import java.util.Optional;

public interface TrainingService {
    void create(Training training);

    Optional<Training> getById(long id);

    List<Training> getAll();
}