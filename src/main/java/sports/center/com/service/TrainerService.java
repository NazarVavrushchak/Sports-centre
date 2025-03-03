package sports.center.com.service;

import sports.center.com.dto.trainer.TrainerRequestDto;
import sports.center.com.dto.trainer.TrainerResponseDto;

public interface TrainerService {
    TrainerResponseDto createTrainer(TrainerRequestDto trainerRequestDto);

    TrainerResponseDto getTrainerProfile();

    boolean changeTrainerPassword(String newPassword);

    TrainerResponseDto updateTrainerProfile(TrainerRequestDto request);

    boolean changeTrainerStatus();
}