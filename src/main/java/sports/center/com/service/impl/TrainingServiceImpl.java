package sports.center.com.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sports.center.com.dto.trainer.TrainerResponseDto;
import sports.center.com.dto.training.TrainingRequestDto;
import sports.center.com.dto.training.TrainingResponseDto;
import sports.center.com.dto.training.TrainingTypeResponseDto;
import sports.center.com.exception.exceptions.*;
import sports.center.com.model.Trainee;
import sports.center.com.model.Trainer;
import sports.center.com.model.Training;
import sports.center.com.model.TrainingType;
import sports.center.com.repository.TraineeRepository;
import sports.center.com.repository.TrainerRepository;
import sports.center.com.repository.TrainingRepository;
import sports.center.com.repository.TrainingTypeRepository;
import sports.center.com.service.TrainingService;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TrainingServiceImpl implements TrainingService {
    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final Validator validator;
    private final HttpServletRequest request;

    @Override
    public TrainingResponseDto addTraining(TrainingRequestDto request) {
        if (request == null) {
            throw new InvalidTrainerRequestException("Training request cannot be null", Set.of());
        }
        String transactionId = MDC.get("transactionId");
        log.info("[Transaction ID: {}] Adding new training: Trainee={}, Trainer={}", transactionId, request.getTraineeUsername(), request.getTrainerUsername());

        validateTrainingRequest(request);

        Trainee trainee = findTraineeByUsername(request.getTraineeUsername());
        Trainer trainer = findTrainerByUsername(request.getTrainerUsername());

        assignTrainerIfNotAssigned(trainee, trainer);

        TrainingType trainingType = findTrainingTypeByName(request.getTrainingTypeName());

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName(request.getTrainingName());
        training.setTrainingDate(request.getTrainingDate());
        training.setTrainingDuration(request.getTrainingDuration());
        training.setTrainingType(trainingType);

        trainingRepository.save(training);

        log.info("[Transaction ID: {}] Training '{}' added successfully for trainee {}", transactionId, request.getTrainingName(), request.getTraineeUsername());

        return TrainingResponseDto.builder()
                .traineeUsername(trainee.getUsername())
                .trainerUsername(trainer.getUsername())
                .trainingName(training.getTrainingName())
                .trainingDate(training.getTrainingDate())
                .trainingDuration(training.getTrainingDuration())
                .trainingTypeName(training.getTrainingType().getTrainingTypeName())
                .build();
    }

    private Trainee findTraineeByUsername(String username) {
        return traineeRepository.findByUsername(username)
                .orElseThrow(() -> new TraineeNotFoundException("Trainee not found: " + username));
    }

    private Trainer findTrainerByUsername(String username) {
        return trainerRepository.findByUsername(username)
                .orElseThrow(() -> new TrainerNotFoundException("Trainer not found: " + username));
    }

    private TrainingType findTrainingTypeByName(String trainingTypeName) {
        return trainingTypeRepository.findByTrainingTypeName(trainingTypeName)
                .orElseThrow(() -> new TrainingTypeNotFoundException("Training type not found: " + trainingTypeName));
    }

    private void assignTrainerIfNotAssigned(Trainee trainee, Trainer trainer) {
        String transactionId = MDC.get("transactionId");
        if (trainee.getTrainers() == null) {
            trainee.setTrainers(new ArrayList<>());
        }
        if (!trainee.getTrainers().contains(trainer)) {
            trainee.getTrainers().add(trainer);
            traineeRepository.save(trainee);
            log.info("[Transaction ID: {}] Trainer {} assigned to trainee {}", transactionId, trainer.getUsername(), trainee.getUsername());
        }
    }

    @Override
    public List<TrainerResponseDto> getNotAssignedActiveTrainers() {
        String username = getAuthenticatedUsername();
        String transactionId = MDC.get("transactionId");
        log.info("[Transaction ID: {}] Fetching not assigned active trainers for trainee: {}", transactionId, username);
        List<Trainer> trainers = Optional.ofNullable(trainerRepository.findNotAssignedActiveTrainers(username))
                .orElse(Collections.emptyList());

        log.debug("[Transaction ID: {}] Found {} not assigned active trainers", transactionId, trainers.size());
        return trainers.stream()
                .map(trainer -> TrainerResponseDto.builder()
                        .username(trainer.getUsername())
                        .firstName(trainer.getFirstName())
                        .lastName(trainer.getLastName())
                        .specializationName(trainer.getSpecialization().getTrainingTypeName())
                        .build())
                .toList();
    }

    @Override
    public List<TrainingTypeResponseDto> getTrainingType() {
        List<TrainingType> trainingTypes = trainingTypeRepository.findAll();

        return trainingTypes.stream()
                .map(this::mapToResponseTrainingType)
                .collect(Collectors.toList());
    }

    @Override
    public List<TrainerResponseDto> updateTraineeTrainersList(List<String> trainerUsernames) {
        String transactionId = MDC.get("transactionId");
        String username = getAuthenticatedUsername();
        log.info("[Transaction ID: {}] Update trainers list for trainee: {}", transactionId, username);


        Trainee trainee = getTraineeOrThrow(username);
        List<Trainer> validTrainers = getValidTrainersOrThrow(trainerUsernames);

        log.debug("[Transaction ID: {}] Valid trainers found: {}", transactionId, validTrainers.size());
        updateTraineeTrainerList(trainee, validTrainers);

        log.info("[Transaction ID: {}] Trainee {}'s trainer list updated successfully", transactionId, username);
        return mapTrainersToResponse(trainee.getTrainers());
    }

    @Override
    public List<TrainingResponseDto> getTraineeTrainings(Date fromDate, Date toDate, String trainerName, String trainingType) {
        String transactionId = MDC.get("transactionId");
        String traineeUsername = getAuthenticatedUsername();
        log.info("[Transaction ID: {}] Fetching trainings for Trainee: {} from {} to {}, Trainer: {}, TrainingType: {}", transactionId, traineeUsername, fromDate, toDate, trainerName, trainingType);

        List<Training> trainings = trainingRepository.findTrainingsByTraineeCriteria(
                traineeUsername, fromDate, toDate, trainerName, trainingType);

        log.debug("[Transaction ID: {}] Found {} trainings for Trainee: {}", transactionId, trainings.size(), traineeUsername);
        return trainings.stream().map(this::mapToResponseTrainee).collect(Collectors.toList());
    }

    @Override
    public List<TrainingResponseDto> getTrainerTrainings(Date fromDate, Date toDate, String traineeName) {
        String transactionId = MDC.get("transactionId");
        String trainerUsername = getAuthenticatedUsername();
        log.info("[Transaction ID: {}] Fetching trainings for Trainer: {} from {} to {}, Trainee: {}", transactionId, trainerUsername, fromDate, toDate, traineeName);

        List<Training> trainings = trainingRepository.findTrainingsByTrainerCriteria(
                trainerUsername, fromDate, toDate, traineeName);

        log.debug("[Transaction ID: {}] Found {} trainings for Trainer: {}", transactionId, trainings.size(), trainerUsername);
        return trainings.stream().map(this::mapToResponseTrainer).collect(Collectors.toList());
    }

    private Trainee getTraineeOrThrow(String username) {
        return traineeRepository.findByUsername(username)
                .orElseThrow(() -> new TraineeNotFoundException("Trainee not found with username: " + username));
    }

    private List<Trainer> getValidTrainersOrThrow(List<String> trainerUsernames) {
        if (trainerUsernames == null || trainerUsernames.isEmpty()) {
            throw new EmptyTrainerListException("Trainer usernames list cannot be empty!");
        }

        List<Trainer> trainers = trainerRepository.findByUsernameIn(trainerUsernames);
        if (trainers.size() != trainerUsernames.size()) {
            throw new TraineeNotFoundException("Some trainers were not found in the database!");
        }
        return trainers;
    }

    private void validateTrainingRequest(TrainingRequestDto request) {
        String transactionId = MDC.get("transactionId");
        Set<jakarta.validation.ConstraintViolation<TrainingRequestDto>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(jakarta.validation.ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));

            log.warn("[Transaction ID: {}] Validation failed: {}", transactionId, errors);

            Set<jakarta.validation.ConstraintViolation<?>> genericViolations = violations.stream()
                    .map(v -> (jakarta.validation.ConstraintViolation<?>) v)
                    .collect(Collectors.toSet());

            throw new InvalidTrainingRequestException("Validation failed: " + errors, genericViolations);
        }
    }

    private void updateTraineeTrainerList(Trainee trainee, List<Trainer> validTrainers) {
        String transactionId = MDC.get("transactionId");
        List<Trainer> uniqueTrainers = new ArrayList<>(new HashSet<>(validTrainers));
        trainee.setTrainers(uniqueTrainers);
        traineeRepository.save(trainee);
        log.info("[Transaction ID: {}] Updated trainers list for Trainee: {}", transactionId, trainee.getUsername());
    }

    private String getAuthenticatedUsername() {
        String transactionId = MDC.get("transactionId");
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            log.warn("[Transaction ID: {}] Unauthorized request", transactionId);
            throw new UnauthorizedException("Unauthorized request");
        }

        String base64Credentials = authHeader.substring("Basic ".length());
        String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        String[] values = credentials.split(":", 2);

        if (values.length != 2) {
            log.warn("[Transaction ID: {}] Invalid authentication format", transactionId);
            throw new UnauthorizedException("Invalid authentication format");
        }

        String username = values[0];
        String password = values[1];

        log.info("[Transaction ID: {}] Attempting authentication for username: {}", transactionId, username);

        Optional<Trainee> traineeOptional = traineeRepository.findByUsername(username);
        if (traineeOptional.isPresent()) {
            return validatePasswordAndReturnUsername(traineeOptional.get(), password, username, "trainee");
        }

        Optional<Trainer> trainerOptional = trainerRepository.findByUsername(username);
        if (trainerOptional.isPresent()) {
            return validatePasswordAndReturnUsername(trainerOptional.get(), password, username, "trainer");
        }

        log.warn("[Transaction ID: {}] Authentication failed: username {} not found", transactionId, username);
        throw new UnauthorizedException("Invalid username or password");
    }

    private String validatePasswordAndReturnUsername(Object user, String password, String username, String userType) {
        String transactionId = MDC.get("transactionId");
        String userPassword = (user instanceof Trainee) ? ((Trainee) user).getPassword() : ((Trainer) user).getPassword();

        if (userPassword == null || !userPassword.equals(password)) {
            log.warn("[Transaction ID: {}] Authentication failed: incorrect or missing password for {} {}", transactionId, userType, username);
            throw new UnauthorizedException("Invalid username or password");
        }

        log.info("[Transaction ID: {}] Authentication successful for {}: {}", transactionId, userType, username);
        return username;
    }

    private List<TrainerResponseDto> mapTrainersToResponse(List<Trainer> trainers) {
        return trainers.stream()
                .map(trainer -> TrainerResponseDto.builder()
                        .firstName(trainer.getFirstName())
                        .lastName(trainer.getLastName())
                        .username(trainer.getUsername())
                        .specializationId(trainer.getSpecialization().getId())
                        .trainees(List.of())
                        .build())
                .toList();
    }

    private TrainingTypeResponseDto mapToResponseTrainingType(TrainingType trainingType) {
        return TrainingTypeResponseDto.builder()
                .id(trainingType.getId())
                .trainingTypeName(trainingType.getTrainingTypeName())
                .build();
    }

    private TrainingResponseDto mapToResponseTrainee(Training training) {
        return TrainingResponseDto.builder()
                .trainingName(training.getTrainingName())
                .trainingDate(training.getTrainingDate())
                .trainingDuration(training.getTrainingDuration())
                .trainingTypeName(training.getTrainingType().getTrainingTypeName())
                .trainerUsername(training.getTrainer().getUsername())
                .build();
    }

    private TrainingResponseDto mapToResponseTrainer(Training training) {
        return TrainingResponseDto.builder()
                .trainingName(training.getTrainingName())
                .trainingDate(training.getTrainingDate())
                .trainingDuration(training.getTrainingDuration())
                .trainingTypeName(training.getTrainingType().getTrainingTypeName())
                .traineeUsername(training.getTrainee().getUsername())
                .build();
    }
}