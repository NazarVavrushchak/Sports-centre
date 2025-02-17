package sports.center.com.repository;

import sports.center.com.model.Training;

import java.util.Date;
import java.util.List;

public interface TrainingRepositoryCustom {
    List<Training> findTrainingsByTraineeCriteria(
            String traineeUsername, Date fromDate, Date toDate, String trainerName, String trainingType);

    List<Training> findTrainingsByTrainerCriteria(
            String trainerUsername, Date fromDate, Date toDate, String traineeName);
}