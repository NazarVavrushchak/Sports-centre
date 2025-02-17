package sports.center.com.service;

import sports.center.com.dto.trainer.TrainerRequestDto;
import sports.center.com.dto.trainer.TrainerResponseDto;

public interface TrainerService {
    TrainerResponseDto createTrainer(TrainerRequestDto trainerRequestDto);

    boolean authenticateTrainer(String username, String password);

    TrainerResponseDto getTrainerByUsername(String username, String password);

    boolean changeTrainerPassword(String username, String oldPassword, String newPassword);

    boolean updateTrainer(String username, String password, TrainerRequestDto request, String newPassword);

    boolean changeTrainerStatus(String username, String password);

    boolean deleteTrainer(String username, String password);
}