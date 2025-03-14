package sports.center.com.service.impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import sports.center.com.security.JwtTool;
import sports.center.com.service.TrainerService;
import sports.center.com.service.UserService;
import sports.center.com.util.PasswordUtil;
import sports.center.com.util.UsernameUtil;

import java.util.ArrayList;
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
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final JwtTool jwtTool;

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
        trainer.setPassword(passwordEncoder.encode(password));
        trainer.setIsActive(true);
        trainer.setSpecialization(specialization);
        userService.initializeNewUser(trainer);

        trainerRepository.save(trainer);
        String token = jwtTool.generateToken(username);
        log.info("[{}] Trainer created successfully: {}", transactionId, trainer.getUsername());

        return TrainerResponseDto.builder()
                .username(username)
                .password(password)
                .token(token)
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
        trainer.setPassword(passwordEncoder.encode(newPassword)); // Hash the new password
        trainerRepository.save(trainer);

        log.info("[{}] Trainer password changed successfully: {}", transactionId, username);

        return true;
    }

    @Override
    public TrainerResponseDto updateTrainerProfile(TrainerRequestDto request) {
        String transactionId = MDC.get("transactionId");
        String username = getAuthenticatedUsername();
        log.info("[{}] Updating trainer profile: {}", transactionId, username);

        validateRequest(request); // Validate the request

        Trainer trainer = findTrainerByUsername(username);

        trainer.setFirstName(request.getFirstName());
        trainer.setLastName(request.getLastName());
        trainer.setIsActive(request.getIsActive());

        // Update specialization if provided
        if (request.getSpecializationId() != null) {
            TrainingType specialization = trainingTypeRepository.findById(request.getSpecializationId())
                    .orElseThrow(() -> new SpecializationNotFoundException(request.getSpecializationId()));
            trainer.setSpecialization(specialization);
        }

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
        Set<ConstraintViolation<TrainerRequestDto>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));

            log.warn("[{}] Validation failed: {}", transactionId, errors);

            Set<ConstraintViolation<?>> genericViolations = violations.stream()
                    .map(v -> (ConstraintViolation<?>) v)
                    .collect(Collectors.toSet());

            throw new InvalidTrainerRequestException("Validation failed: " + errors, genericViolations);
        }
    }

    private String getAuthenticatedUsername() {
        String transactionId = MDC.get("transactionId");
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if (username == null || username.isEmpty()) {
                log.warn("Transaction [{}] - No authenticated user found", transactionId);
                throw new UnauthorizedException("No authenticated user found");
            }
            log.info("Transaction [{}] - Authenticated user: {}", transactionId, username);
            return username;
        } catch (Exception e) {
            log.warn("Transaction [{}] - Unauthorized request: {}", transactionId, e.getMessage());
            throw new UnauthorizedException("Unauthorized request");
        }
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
        String transactionId = MDC.get("transactionId");
        return trainerRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("[{}] Trainer not found: {}", transactionId, username);
                    return new TrainerNotFoundException("Trainer not found: " + username);
                });
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
                .trainees(trainer.getTrainees() != null ? trainer.getTrainees().stream()
                        .map(trainee -> TraineeResponseDto.builder()
                                .username(trainee.getUsername())
                                .firstName(trainee.getFirstName())
                                .lastName(trainee.getLastName())
                                .build())
                        .collect(Collectors.toList()) : new ArrayList<>())
                .build();
    }
}