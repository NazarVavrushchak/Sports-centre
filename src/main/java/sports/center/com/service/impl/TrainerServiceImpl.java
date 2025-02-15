package sports.center.com.service.impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sports.center.com.dto.trainer.TrainerRequestDto;
import sports.center.com.dto.trainer.TrainerResponseDto;
import sports.center.com.model.Trainer;
import sports.center.com.model.TrainingType;
import sports.center.com.repository.TrainerRepository;
import sports.center.com.repository.TrainingTypeRepository;
import sports.center.com.service.AuthService;
import sports.center.com.service.TrainerService;
import sports.center.com.util.PasswordUtil;
import sports.center.com.util.UsernameUtil;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TrainerServiceImpl implements TrainerService {

    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UsernameUtil usernameUtil;
    private final AuthService authService;
    private final Validator validator;

    @Override
    public TrainerResponseDto createTrainer(TrainerRequestDto trainerRequestDto) {
        validateRequest(trainerRequestDto);
        log.info("Creating new trainer: {} {}", trainerRequestDto.getFirstName(), trainerRequestDto.getLastName());

        String username = usernameUtil.generateUsername(trainerRequestDto.getFirstName(), trainerRequestDto.getLastName());
        String password = PasswordUtil.generatePassword();

        TrainingType specialization = trainingTypeRepository.findById(trainerRequestDto.getSpecializationId())
                .orElseThrow(() -> new IllegalArgumentException("Specialization not found"));

        Trainer trainer = new Trainer();
        trainer.setFirstName(trainerRequestDto.getFirstName());
        trainer.setLastName(trainerRequestDto.getLastName());
        trainer.setUsername(username);
        trainer.setPassword(password);
        trainer.setIsActive(true);
        trainer.setSpecialization(specialization);

        trainerRepository.save(trainer);
        log.info("Trainer created successfully: {}", trainer.getUsername());

        return mapToResponse(trainer);
    }

    @Override
    public boolean authenticateTrainer(String username, String password) {
        log.info("Authenticating trainer: {}", username);
        boolean isAuthenticated = authService.authenticateTrainer(username, password);
        log.info("Authentication result for {}: {}", username, isAuthenticated);
        return isAuthenticated;
    }

    @Override
    public TrainerResponseDto getTrainerByUsername(String username, String password) {
        log.info("Fetching trainer profile: {}", username);
        authenticateOrThrow(username, password);

        return trainerRepository.findByUsername(username)
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + username));
    }

    @Override
    public boolean changeTrainerPassword(String username, String oldPassword, String newPassword) {
        log.info("Changing password for trainer: {}", username);
        authenticateOrThrow(username, oldPassword);
        validatePassword(newPassword);

        Trainer trainer = getTrainerOrThrow(username);
        trainer.setPassword(newPassword);
        trainerRepository.save(trainer);

        log.info("Password changed successfully for trainer: {}", username);
        return true;
    }

    @Override
    public boolean updateTrainer(String username, String password, TrainerRequestDto request, String newPassword) {
        validateRequest(request);
        log.info("Updating trainer profile: {}", username);
        authenticateOrThrow(username, password);

        Trainer trainer = getTrainerOrThrow(username);
        TrainingType specialization = getSpecializationOrThrow(request.getSpecializationId());

        updatePasswordIfProvided(trainer, newPassword);
        updateUsernameIfChanged(trainer, request);

        trainer.setFirstName(request.getFirstName());
        trainer.setLastName(request.getLastName());
        trainer.setSpecialization(specialization);

        trainerRepository.save(trainer);
        log.info("Trainer profile updated: {}", username);
        return true;
    }

    private TrainingType getSpecializationOrThrow(Long specializationId) {
        return trainingTypeRepository.findById(specializationId)
                .orElseThrow(() -> new IllegalArgumentException("Specialization not found"));
    }

    private void updatePasswordIfProvided(Trainer trainer, String newPassword) {
        Optional.ofNullable(newPassword)
                .filter(pwd -> !pwd.isEmpty())
                .ifPresent(pwd -> {
                    validatePassword(pwd);
                    trainer.setPassword(pwd);
                });
    }

    private void updateUsernameIfChanged(Trainer trainer, TrainerRequestDto request) {
        if (!trainer.getFirstName().equals(request.getFirstName())) {
            trainer.setUsername(usernameUtil.generateUsername(request.getFirstName(), request.getLastName()));
        }
    }

    @Override
    public boolean changeTrainerStatus(String username, String password) {
        log.info("Changing trainer status: {}", username);
        authenticateOrThrow(username, password);

        Trainer trainer = getTrainerOrThrow(username);
        trainer.setIsActive(!trainer.getIsActive());
        trainerRepository.save(trainer);

        log.info("Trainer status updated for {}: {}", username, trainer.getIsActive());
        return trainer.getIsActive();
    }

    @Override
    public boolean deleteTrainer(String username, String password) {
        log.info("Deleting trainer: {}", username);
        authenticateOrThrow(username, password);

        Trainer trainer = getTrainerOrThrow(username);
        trainerRepository.delete(trainer);

        log.info("Trainer deleted successfully: {}", username);
        return true;
    }

    private void authenticateOrThrow(String username, String password) {
        if (!authService.authenticateTrainer(username, password)) {
            log.warn("Authentication failed for {}", username);
            throw new SecurityException("Invalid username or password.");
        }
    }

    private Trainer getTrainerOrThrow(String username) {
        return trainerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + username));
    }

    private void validateRequest(TrainerRequestDto request) {
        Set<ConstraintViolation<TrainerRequestDto>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            log.warn("Validation failed: {}", errors);
            throw new IllegalArgumentException("Validation failed: " + errors);
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be empty.");
        }
        if (password.length() != 10) {
            throw new IllegalArgumentException("Password must be exactly 10 characters long.");
        }
    }

    private TrainerResponseDto mapToResponse(Trainer trainer) {
        return new TrainerResponseDto(
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.getUsername(),
                trainer.getPassword(),
                trainer.getIsActive(),
                trainer.getSpecialization().getId()
        );
    }
}