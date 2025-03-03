package sports.center.com.service;

import sports.center.com.dto.trainee.TraineeRequestDto;
import sports.center.com.dto.trainee.TraineeResponseDto;

public interface TraineeService {
    TraineeResponseDto createTrainee(TraineeRequestDto traineeRequestDto);

    TraineeResponseDto updateTraineeProfile(TraineeRequestDto request);

    boolean changeTraineeStatus();

    boolean deleteTrainee();

    TraineeResponseDto getTraineeProfile();

    boolean changeTraineePassword(String newPassword);
}