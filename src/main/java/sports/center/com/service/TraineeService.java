package sports.center.com.service;

import sports.center.com.dto.trainee.TraineeRequestDto;
import sports.center.com.dto.trainee.TraineeResponseDto;

public interface TraineeService {
    TraineeResponseDto createTrainee(TraineeRequestDto traineeRequestDto);

    boolean authenticateTrainee(String username, String password);

    boolean updateTrainee(String username, String password, TraineeRequestDto request, String newPassword);

    boolean changeTraineeStatus(String username, String password);

    boolean deleteTrainee(String username, String password);

    TraineeResponseDto getTraineeByUsername(String username, String password);

    boolean changeTraineePassword(String username, String oldPassword, String newPassword);
}