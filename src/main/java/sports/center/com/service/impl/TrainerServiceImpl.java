package sports.center.com.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sports.center.com.dto.trainee.TraineeResponseDto;
import sports.center.com.dto.trainer.TrainerRequestDto;
import sports.center.com.dto.trainer.TrainerResponseDto;
import sports.center.com.exception.exceptions.*;
import sports.center.com.model.Trainer;
import sports.center.com.model.TrainingType;
import sports.center.com.repository.TrainerRepository;
import sports.center.com.repository.TrainingTypeRepository;
import sports.center.com.service.TrainerService;
import sports.center.com.util.PasswordUtil;
import sports.center.com.util.UsernameUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
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
    private final Validator validator;
    private final HttpServletRequest request;

    @Override
    public TrainerResponseDto createTrainer(TrainerRequestDto trainerRequestDto) {
        String transactionId = MDC.get("transactionId");
        validateRequest(trainerRequestDto);
        log.info("[{}] Creating new trainer: {} {}", transactionId, trainerRequestDto.getFirstName(), trainerRequestDto.getLastName());

        String username = usernameUtil.generateUsername(trainerRequestDto.getFirstName(), trainerRequestDto.getLastName());
        String password = PasswordUtil.generatePassword();

        Long specializationId = trainerRequestDto.getSpecializationId();

        TrainingType specialization = trainingTypeRepository.findById(specializationId)
                .orElseThrow(() -> new SpecializationNotFoundException(specializationId));

        Trainer trainer = new Trainer();
        trainer.setFirstName(trainerRequestDto.getFirstName());
        trainer.setLastName(trainerRequestDto.getLastName());
        trainer.setUsername(username);
        trainer.setPassword(password);
        trainer.setIsActive(true);
        trainer.setSpecialization(specialization);

        trainerRepository.save(trainer);
        log.info("[{}] Trainer created successfully: {}", transactionId, trainer.getUsername());

        return TrainerResponseDto.builder()
                .username(username)
                .password(password)
                .build();
    }

    @Override
    public TrainerResponseDto getTrainerProfile() {
        String transactionId = MDC.get("transactionId");
        String username = getAuthenticatedUsername();
        log.info("[{}] Fetching trainer profile: {}", transactionId, username);

        Trainer trainer = findTrainerByUsername(username);

        log.info("[{}] Trainer profile fetched successfully: {}", transactionId, username);
        return mapToResponseWithTrainees(trainer);
    }

    @Override
    public boolean changeTrainerPassword(String newPassword) {
        String transactionId = MDC.get("transactionId");
        String username = getAuthenticatedUsername();
        log.info("Transaction [{}] - Changing password for trainer: {}", transactionId, username);

        validatePassword(newPassword);

        Trainer trainer = getTrainerOrThrow(username);
        trainer.setPassword(newPassword);

        trainerRepository.save(trainer);

        log.info("[{}] Trainer password changed successfully: {}", transactionId, username);

        return true;
    }

    @Override
    public TrainerResponseDto updateTrainerProfile(TrainerRequestDto request) {
        String transactionId = MDC.get("transactionId");
        String username = getAuthenticatedUsername();
        log.info("[{}] Updating trainer profile: {}", transactionId, username);

        Trainer trainer = findTrainerByUsername(username);

        trainer.setFirstName(request.getFirstName());
        trainer.setLastName(request.getLastName());
        trainer.setIsActive(request.getIsActive());

        trainerRepository.save(trainer);
        log.info("[{}] Trainer profile updated successfully: {}", transactionId, username);

        return mapToResponseWithTraineesUsername(trainer);
    }

    @Override
    public boolean changeTrainerStatus() {
        String transactionId = MDC.get("transactionId");
        String username = getAuthenticatedUsername();
        log.info("[{}] Toggling trainer status for {}", transactionId, username);

        Trainer trainer = getTrainerOrThrow(username);

        boolean newStatus = !trainer.getIsActive();
        trainer.setIsActive(newStatus);
        trainerRepository.save(trainer);

        log.info("[{}] Trainer status toggled for {}: new status = {}", transactionId, username, newStatus);
        return newStatus;
    }

    private Trainer getTrainerOrThrow(String username) {
        String transactionId = MDC.get("transactionId");
        return trainerRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Transaction [{}] - Trainer not found: {}", transactionId, username);
                    return new TrainerNotFoundException("Trainer not found: " + username);
                });
    }

    private void validateRequest(TrainerRequestDto request) {
        String transactionId = MDC.get("transactionId");
        Set<jakarta.validation.ConstraintViolation<TrainerRequestDto>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(jakarta.validation.ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));

            log.warn("[{}] Validation failed: {}", transactionId, errors);

            Set<jakarta.validation.ConstraintViolation<?>> genericViolations = violations.stream()
                    .map(v -> (jakarta.validation.ConstraintViolation<?>) v)
                    .collect(Collectors.toSet());

            throw new InvalidTrainerRequestException("Validation failed: " + errors, genericViolations);
        }
    }

    private String getAuthenticatedUsername() {
        String transactionId = MDC.get("transactionId");
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Basic ")) {
            String base64Credentials = authHeader.substring("Basic ".length());
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
            String[] values = credentials.split(":", 2);

            if (values.length != 2) {
                throw new UnauthorizedException("Invalid authentication format");
            }

            String username = values[0];
            String password = values[1];

            log.info("[{}] Attempting authentication for username: {}", transactionId, username);

            Optional<Trainer> trainerOptional = trainerRepository.findByUsername(username);
            if (trainerOptional.isEmpty()) {
                log.warn("[{}] Authentication failed: username {} not found", transactionId, username);
                throw new UnauthorizedException("Invalid username or password");
            }

            Trainer trainer = trainerOptional.get();

            if (trainer.getPassword() == null || !trainer.getPassword().equals(password)) {
                log.warn("[{}] Authentication failed: incorrect or missing password for user {}", transactionId, username);
                throw new UnauthorizedException("Invalid username or password");
            }

            log.info("[{}] Authentication successful for user: {}", transactionId, username);
            return username;
        }

        throw new UnauthorizedException("Unauthorized request");
    }

    private void validatePassword(String password) {
        String transactionId = MDC.get("transactionId");
        if (password == null || password.trim().isEmpty()) {
            log.warn("Transaction [{}] - Password validation failed: empty password", transactionId);
            throw new InvalidPasswordException("New password cannot be empty.");
        }
        if (password.length() != 10) {
            log.warn("Transaction [{}] - Password validation failed: incorrect length", transactionId);
            throw new InvalidPasswordException("Password must be exactly 10 characters long.");
        }
    }

    private Trainer findTrainerByUsername(String username) {
        return trainerRepository.findByUsername(username)
                .orElseThrow(() -> new TrainerNotFoundException("Trainer not found: " + username));
    }

    private TrainerResponseDto mapToResponseWithTrainees(Trainer trainer) {
        return TrainerResponseDto.builder()
                .firstName(trainer.getFirstName())
                .lastName(trainer.getLastName())
                .specializationId(trainer.getSpecialization().getId())
                .isActive(trainer.getIsActive())
                .trainees(trainer.getTrainees() != null ? trainer.getTrainees().stream()
                        .map(trainee -> TraineeResponseDto.builder()
                                .username(trainee.getUsername())
                                .firstName(trainee.getFirstName())
                                .lastName(trainee.getLastName())
                                .build())
                        .collect(Collectors.toList()) : new ArrayList<>())
                .build();
    }

    private TrainerResponseDto mapToResponseWithTraineesUsername(Trainer trainer) {
        return TrainerResponseDto.builder()
                .username(trainer.getUsername())
                .firstName(trainer.getFirstName())
                .lastName(trainer.getLastName())
                .specializationId(trainer.getSpecialization().getId())
                .isActive(trainer.getIsActive())
                .trainees(trainer.getTrainees().stream()
                        .map(trainee -> TraineeResponseDto.builder()
                                .username(trainee.getUsername())
                                .firstName(trainee.getFirstName())
                                .lastName(trainee.getLastName())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}