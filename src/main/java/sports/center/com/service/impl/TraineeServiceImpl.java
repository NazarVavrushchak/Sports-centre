package sports.center.com.service.impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sports.center.com.dto.trainee.TraineeRequestDto;
import sports.center.com.dto.trainee.TraineeResponseDto;
import sports.center.com.model.Trainee;
import sports.center.com.repository.TraineeRepository;
import sports.center.com.service.AuthService;
import sports.center.com.service.TraineeService;
import sports.center.com.util.PasswordUtil;
import sports.center.com.util.UsernameUtil;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TraineeServiceImpl implements TraineeService {
    private final TraineeRepository traineeRepository;
    private final UsernameUtil usernameUtil;
    private final AuthService authService;
    private final Validator validator;

    @Override
    public TraineeResponseDto createTrainee(TraineeRequestDto traineeRequestDto) {
        validateRequest(traineeRequestDto);
        log.info("Creating new trainee: {} {}", traineeRequestDto.getFirstName(), traineeRequestDto.getLastName());

        String username = usernameUtil.generateUsername(traineeRequestDto.getFirstName(), traineeRequestDto.getLastName());
        String password = PasswordUtil.generatePassword();

        Trainee trainee = new Trainee();
        trainee.setFirstName(traineeRequestDto.getFirstName());
        trainee.setLastName(traineeRequestDto.getLastName());
        trainee.setUsername(username);
        trainee.setPassword(password);
        trainee.setIsActive(true);
        trainee.setDateOfBirth(traineeRequestDto.getDateOfBirth());
        trainee.setAddress(traineeRequestDto.getAddress());

        traineeRepository.save(trainee);
        log.info("Trainee created successfully: {}", trainee.getUsername());

        return mapToResponse(trainee);
    }

    @Override
    public boolean authenticateTrainee(String username, String password) {
        log.info("Authenticating trainee: {}", username);
        boolean isAuthenticated = authService.authenticateTrainee(username, password);
        log.info("Authentication result for {}: {}", username, isAuthenticated);
        return isAuthenticated;
    }

    @Override
    public TraineeResponseDto getTraineeByUsername(String username, String password) {
        log.info("Fetching trainee profile: {}", username);
        authenticateOrThrow(username, password);

        return traineeRepository.findByUsername(username)
                .map(this::mapToResponse)
                .orElseThrow(() -> {
                    log.warn("Trainee not found: {}", username);
                    return new IllegalArgumentException("Trainee not found: " + username);
                });
    }

    @Override
    public boolean changeTraineePassword(String username, String oldPassword, String newPassword) {
        log.info("Changing password for trainee: {}", username);
        authenticateOrThrow(username, oldPassword);
        validatePassword(newPassword);

        Trainee trainee = getTraineeOrThrow(username);
        trainee.setPassword(newPassword);
        traineeRepository.save(trainee);

        log.info("Password changed successfully for trainee: {}", username);
        return true;
    }

    @Override
    public boolean updateTrainee(String username, String password, TraineeRequestDto request, String newPassword) {
        log.info("Updating trainee profile: {}", username);
        validateRequest(request);
        authenticateOrThrow(username, password);

        Trainee trainee = getTraineeOrThrow(username);

        if (!trainee.getFirstName().equals(request.getFirstName()) ||
                !trainee.getLastName().equals(request.getLastName())) {
            trainee.setUsername(usernameUtil.generateUsername(request.getFirstName(), request.getLastName()));
        }

        if (newPassword != null && !newPassword.isEmpty()) {
            validatePassword(newPassword);
            trainee.setPassword(newPassword);
        }

        trainee.setFirstName(request.getFirstName());
        trainee.setLastName(request.getLastName());
        trainee.setDateOfBirth(request.getDateOfBirth());
        trainee.setAddress(request.getAddress());

        traineeRepository.save(trainee);
        log.info("Trainee profile updated: {}", trainee.getUsername());
        return true;
    }

    @Override
    public boolean changeTraineeStatus(String username, String password) {
        authenticateOrThrow(username, password);

        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));

        boolean newStatus = !trainee.getIsActive();
        trainee.setIsActive(newStatus);
        traineeRepository.save(trainee);

        log.info("Trainee status updated for {}: {}", username, newStatus ? "Active" : "Inactive");

        return newStatus;
    }

    @Override
    public boolean deleteTrainee(String username, String password) {
        log.info("Deleting trainee: {}", username);
        authenticateOrThrow(username, password);

        Trainee trainee = getTraineeOrThrow(username);
        traineeRepository.delete(trainee);

        log.info("Trainee deleted successfully: {}", username);
        return true;
    }

    private void authenticateOrThrow(String username, String password) {
        if (!authService.authenticateTrainee(username, password)) {
            log.warn("Authentication failed for {}", username);
            throw new SecurityException("Invalid username or password.");
        }
    }

    private void validateRequest(TraineeRequestDto request) {
        Set<ConstraintViolation<TraineeRequestDto>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            log.warn("Validation failed: {}", errors);
            throw new IllegalArgumentException("Validation failed: " + errors);
        }
    }

    private Trainee getTraineeOrThrow(String username) {
        return traineeRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));
    }

    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be empty.");
        }
        if (password.length() != 10) {
            throw new IllegalArgumentException("Password must be exactly 10 characters long.");
        }
    }

    private TraineeResponseDto mapToResponse(Trainee trainee) {
        return new TraineeResponseDto(
                trainee.getFirstName(),
                trainee.getLastName(),
                trainee.getUsername(),
                trainee.getPassword(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.getIsActive()
        );
    }
}