package sports.center.com.service;

import sports.center.com.dto.trainer.TrainerResponseDto;
import sports.center.com.dto.training.TrainingRequestDto;
import sports.center.com.dto.training.TrainingResponseDto;

import java.util.Date;
import java.util.List;

public interface TrainingService {
    TrainingResponseDto addTraining(TrainingRequestDto trainingRequestDto);

    List<TrainingResponseDto> getTraineeTrainings(String traineeUsername, String password, Date fromDate, Date toDate, String trainerName, String trainingType);

    List<TrainingResponseDto> getTrainerTrainings(String trainerUsername, String password, Date fromDate, Date toDate, String traineeName);

    List<TrainerResponseDto> getUnassignedTrainers(String traineeUsername, String password);

    List<TrainerResponseDto> updateTraineeTrainers(String traineeUsername, String password, List<String> trainerUsernames);
}