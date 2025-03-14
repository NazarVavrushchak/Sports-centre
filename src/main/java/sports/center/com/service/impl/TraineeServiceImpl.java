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
import sports.center.com.dto.trainee.TraineeRequestDto;
import sports.center.com.dto.trainee.TraineeResponseDto;
import sports.center.com.dto.trainer.TrainerResponseDto;
import sports.center.com.exception.exceptions.InvalidPasswordException;
import sports.center.com.exception.exceptions.InvalidTraineeRequestException;
import sports.center.com.exception.exceptions.TraineeNotFoundException;
import sports.center.com.exception.exceptions.UnauthorizedException;
import sports.center.com.model.Trainee;
import sports.center.com.repository.TraineeRepository;
import sports.center.com.security.JwtTool;
import sports.center.com.service.TraineeService;
import sports.center.com.service.UserService;
import sports.center.com.util.PasswordUtil;
import sports.center.com.util.UsernameUtil;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TraineeServiceImpl implements TraineeService {
    private final TraineeRepository traineeRepository;
    private final UsernameUtil usernameUtil;
    private final Validator validator;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final JwtTool jwtTool;

    @Override
    public TraineeResponseDto createTrainee(TraineeRequestDto traineeRequestDto) {
        String transactionId = MDC.get("transactionId");
        log.info("Transaction [{}] - Received request to create trainee: {} {}",
                transactionId, traineeRequestDto.getFirstName(), traineeRequestDto.getLastName());

        String username = usernameUtil.generateUsername(traineeRequestDto.getFirstName(), traineeRequestDto.getLastName());
        String password = PasswordUtil.generatePassword();

        Trainee trainee = new Trainee();
        trainee.setFirstName(traineeRequestDto.getFirstName());
        trainee.setLastName(traineeRequestDto.getLastName());
        trainee.setUsername(username);
        trainee.setPassword(passwordEncoder.encode(password));
        trainee.setIsActive(true);
        trainee.setDateOfBirth(traineeRequestDto.getDateOfBirth());
        trainee.setAddress(traineeRequestDto.getAddress());
        userService.initializeNewUser(trainee);

        traineeRepository.save(trainee);
        String token = jwtTool.generateToken(trainee.getUsername());
        log.info("Transaction [{}] - Trainee created successfully: {}", transactionId, trainee.getUsername());

        return TraineeResponseDto.builder()
                .username(username)
                .password(password)
                .token(token)
                .build();
    }

    @Override
    public TraineeResponseDto getTraineeProfile() {
        String transactionId = MDC.get("transactionId");
        String username = getAuthenticatedUsername();
        log.info("Transaction [{}] - Fetching trainee profile: {}", transactionId, username);

        return traineeRepository.findByUsername(username)
                .map(this::mapToResponseWithTrainers)
                .orElseThrow(() -> {
                    log.warn("Transaction [{}] - Trainee not found: {}", transactionId, username);
                    return new TraineeNotFoundException(username);
                });
    }

    @Override
    public boolean changeTraineePassword(String newPassword) {
        String transactionId = MDC.get("transactionId");
        String username = getAuthenticatedUsername();
        log.info("Transaction [{}] - Changing password for trainee: {}", transactionId, username);

        validatePassword(newPassword);

        Trainee trainee = getTraineeOrThrow(username);
        trainee.setPassword(passwordEncoder.encode(newPassword));
        traineeRepository.save(trainee);
        log.info("Transaction [{}] - Password changed successfully for trainee: {}", transactionId, username);

        return true;
    }

    @Override
    public TraineeResponseDto updateTraineeProfile(TraineeRequestDto request) {
        String transactionId = MDC.get("transactionId");
        String username = getAuthenticatedUsername();
        log.info("Transaction [{}] - Updating trainee profile: {}", transactionId, username);

        validateRequest(request);

        Trainee trainee = getTraineeOrThrow(username);

        Optional.ofNullable(request.getFirstName()).ifPresent(trainee::setFirstName);
        Optional.ofNullable(request.getLastName()).ifPresent(trainee::setLastName);
        Optional.ofNullable(request.getDateOfBirth()).ifPresent(trainee::setDateOfBirth);
        Optional.ofNullable(request.getAddress()).ifPresent(trainee::setAddress);

        if (request.getIsActive() != null) {
            trainee.setIsActive(request.getIsActive());
        }

        updateUsernameIfChanged(trainee, request);

        traineeRepository.save(trainee);
        log.info("Transaction [{}] - Trainee profile updated: {}", transactionId, trainee.getUsername());

        return mapToResponseWithUsername(trainee);
    }

    private void updateUsernameIfChanged(Trainee trainee, TraineeRequestDto request) {
        if (request.getFirstName() != null && request.getLastName() != null &&
                (!Objects.equals(trainee.getFirstName(), request.getFirstName()) ||
                        !Objects.equals(trainee.getLastName(), request.getLastName()))) {
            trainee.setUsername(usernameUtil.generateUsername(request.getFirstName(), request.getLastName()));
        }
    }

    @Override
    public boolean changeTraineeStatus() {
        String transactionId = MDC.get("transactionId");
        String username = getAuthenticatedUsername();
        Trainee trainee = getTraineeOrThrow(username);

        boolean newStatus = !trainee.getIsActive();
        trainee.setIsActive(newStatus);
        traineeRepository.save(trainee);

        log.info("Transaction [{}] - Trainee status toggled for {}: new status = {}", transactionId, username, newStatus);
        return newStatus;
    }

    @Override
    public boolean deleteTrainee() {
        String transactionId = MDC.get("transactionId");
        String username = getAuthenticatedUsername();
        log.info("Transaction [{}] - Deleting trainee: {}", transactionId, username);

        Trainee trainee = getTraineeOrThrow(username);
        traineeRepository.delete(trainee);

        log.info("Transaction [{}] - Trainee deleted successfully: {}", transactionId, username);
        return true;
    }

    private void validateRequest(TraineeRequestDto request) {
        String transactionId = MDC.get("transactionId");
        Set<ConstraintViolation<TraineeRequestDto>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            log.warn("Transaction [{}] - Validation failed: {}", transactionId, errors);

            Set<ConstraintViolation<?>> genericViolations = violations.stream()
                    .map(v -> (ConstraintViolation<?>) v)
                    .collect(Collectors.toSet());
            throw new InvalidTraineeRequestException("Validation failed: " + errors, genericViolations);
        }
    }

    private Trainee getTraineeOrThrow(String username) {
        String transactionId = MDC.get("transactionId");
        return traineeRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Transaction [{}] - Trainee not found: {}", transactionId, username);
                    return new TraineeNotFoundException("Trainee not found: " + username);
                });
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

    private TraineeResponseDto mapToResponseWithUsername(Trainee trainee) {
        return TraineeResponseDto.builder()
                .username(trainee.getUsername())
                .firstName(trainee.getFirstName())
                .lastName(trainee.getLastName())
                .dateOfBirth(trainee.getDateOfBirth())
                .address(trainee.getAddress())
                .isActive(trainee.getIsActive())
                .trainers(trainee.getTrainers().stream()
                        .map(trainer -> TrainerResponseDto.builder()
                                .username(trainer.getUsername())
                                .firstName(trainer.getFirstName())
                                .lastName(trainer.getLastName())
                                .specializationId(trainer.getSpecialization().getId())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private TraineeResponseDto mapToResponseWithTrainers(Trainee trainee) {
        return TraineeResponseDto.builder()
                .firstName(trainee.getFirstName())
                .lastName(trainee.getLastName())
                .dateOfBirth(trainee.getDateOfBirth())
                .address(trainee.getAddress())
                .isActive(trainee.getIsActive())
                .trainers(trainee.getTrainers().stream()
                        .map(trainer -> TrainerResponseDto.builder()
                                .username(trainer.getUsername())
                                .firstName(trainer.getFirstName())
                                .lastName(trainer.getLastName())
                                .specializationId(trainer.getSpecialization().getId())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}