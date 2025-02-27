package sports.center.com.service;

import sports.center.com.dto.trainer.TrainerResponseDto;
import sports.center.com.dto.training.TrainingRequestDto;
import sports.center.com.dto.training.TrainingResponseDto;
import sports.center.com.dto.training.TrainingTypeResponseDto;

import java.util.Date;
import java.util.List;

public interface TrainingService {
    TrainingResponseDto addTraining(TrainingRequestDto trainingRequestDto);

    List<TrainingResponseDto> getTraineeTrainings(Date fromDate, Date toDate, String trainerName, String trainingType);

    List<TrainingResponseDto> getTrainerTrainings(Date fromDate, Date toDate, String traineeName);

    List<TrainerResponseDto> updateTraineeTrainersList(List<String> trainerUsernames);

    List<TrainerResponseDto> getNotAssignedActiveTrainers();

    List<TrainingTypeResponseDto> getTrainingType();
}