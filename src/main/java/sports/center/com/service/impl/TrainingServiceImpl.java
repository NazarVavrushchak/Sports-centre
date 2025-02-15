package sports.center.com.service.impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sports.center.com.dto.trainer.TrainerResponseDto;
import sports.center.com.dto.training.TrainingRequestDto;
import sports.center.com.dto.training.TrainingResponseDto;
import sports.center.com.model.Trainee;
import sports.center.com.model.Trainer;
import sports.center.com.model.Training;
import sports.center.com.model.TrainingType;
import sports.center.com.repository.TraineeRepository;
import sports.center.com.repository.TrainerRepository;
import sports.center.com.repository.TrainingRepository;
import sports.center.com.repository.TrainingTypeRepository;
import sports.center.com.service.AuthService;
import sports.center.com.service.TrainingService;

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
    private final AuthService authService;
    private final Validator validator;

    @Override
    public TrainingResponseDto addTraining(TrainingRequestDto request) {
        log.info("Adding new training: Trainee ID={}, Trainer ID={}, TrainingType ID={}",
                request.getTraineeId(), request.getTrainerId(), request.getTrainingTypeId());

        validateTrainingRequest(request);

        Trainee trainee = getEntityByIdOrThrow(traineeRepository, request.getTraineeId(), "Trainee");
        Trainer trainer = getEntityByIdOrThrow(trainerRepository, request.getTrainerId(), "Trainer");
        TrainingType trainingType = getEntityByIdOrThrow(trainingTypeRepository, request.getTrainingTypeId(), "Training Type");

        assignTrainer(trainee, trainer);

        Training training = createTraining(trainee, trainer, trainingType, request);
        trainingRepository.save(training);

        log.info("Training created successfully: {}", training.getTrainingName());
        return mapToResponse(training);
    }

    private <T> T getEntityByIdOrThrow(JpaRepository<T, Long> repository, Long id, String entityName) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(entityName + " not found with ID: " + id));
    }

    private void assignTrainer(Trainee trainee, Trainer trainer) {
        if (!trainee.getTrainers().contains(trainer)) {
            trainee.getTrainers().add(trainer);
            traineeRepository.save(trainee);
            log.info("Trainer {} assigned to trainee {}", trainer.getUsername(), trainee.getUsername());
        }
    }

    private Training createTraining(Trainee trainee, Trainer trainer, TrainingType trainingType, TrainingRequestDto request) {
        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(trainingType);
        training.setTrainingName(request.getTrainingName());
        training.setTrainingDate(request.getTrainingDate());
        training.setTrainingDuration(request.getTrainingDuration());
        return training;
    }

    @Override
    public List<TrainingResponseDto> getTraineeTrainings(String traineeUsername, String password, Date fromDate, Date toDate, String trainerName, String trainingType) {
        authenticateTraineeOrThrow(traineeUsername, password);
        log.info("Fetching trainings for Trainee: {} from {} to {}, Trainer: {}, TrainingType: {}",
                traineeUsername, fromDate, toDate, trainerName, trainingType);

        List<Training> trainings = trainingRepository.findTrainingsByTraineeCriteria(traineeUsername, fromDate, toDate, trainerName, trainingType);
        return trainings.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<TrainingResponseDto> getTrainerTrainings(String trainerUsername, String password, Date fromDate, Date toDate, String traineeName) {
        authenticateTrainerOrThrow(trainerUsername, password);
        log.info("Fetching trainings for Trainer: {} from {} to {}, Trainee: {}",
                trainerUsername, fromDate, toDate, traineeName);

        List<Training> trainings = trainingRepository.findTrainingsByTrainerCriteria(trainerUsername, fromDate, toDate, traineeName);
        return trainings.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<TrainerResponseDto> getUnassignedTrainers(String traineeUsername, String password) {
        authenticateTraineeOrThrow(traineeUsername, password);
        log.info("Fetching unassigned trainers for Trainee: {}", traineeUsername);

        List<Trainer> unassignedTrainers = trainerRepository.findUnassignedTrainers(traineeUsername);

        log.info("Unassigned trainers: {}", unassignedTrainers.stream()
                .map(Trainer::getUsername)
                .collect(Collectors.joining(", ")));

        return unassignedTrainers.stream()
                .map(trainer -> new TrainerResponseDto(
                        trainer.getFirstName(),
                        trainer.getLastName(),
                        trainer.getUsername(),
                        null,
                        trainer.getIsActive(),
                        trainer.getSpecialization().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<TrainerResponseDto> updateTraineeTrainers(String traineeUsername, String password, List<String> trainerUsernames) {
        authenticateTraineeOrThrow(traineeUsername, password);
        log.info("Updating trainers for Trainee: {}", traineeUsername);

        Trainee trainee = getTraineeOrThrow(traineeUsername);
        List<Trainer> validTrainers = getValidTrainersOrThrow(trainerUsernames);

        updateTraineeTrainerList(trainee, validTrainers);

        return mapTrainersToResponse(trainee.getTrainers());
    }

    private Trainee getTraineeOrThrow(String username) {
        return traineeRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found with username: " + username));
    }

    private List<Trainer> getValidTrainersOrThrow(List<String> trainerUsernames) {
        List<Trainer> trainers = trainerRepository.findByUsernameIn(trainerUsernames);
        if (trainers.size() != trainerUsernames.size()) {
            throw new IllegalArgumentException("Some trainers were not found in the database!");
        }
        return trainers;
    }

    private void updateTraineeTrainerList(Trainee trainee, List<Trainer> newTrainers) {
        Set<Trainer> updatedTrainers = new HashSet<>(trainee.getTrainers());
        updatedTrainers.addAll(newTrainers);
        trainee.setTrainers(new ArrayList<>(updatedTrainers));

        traineeRepository.save(trainee);
        log.info("Updated trainers list for Trainee: {}", trainee.getUsername());
    }

    private List<TrainerResponseDto> mapTrainersToResponse(List<Trainer> trainers) {
        return trainers.stream()
                .map(trainer -> new TrainerResponseDto(
                        trainer.getFirstName(),
                        trainer.getLastName(),
                        trainer.getUsername(),
                        null,
                        trainer.getIsActive(),
                        trainer.getSpecialization().getId()))
                .collect(Collectors.toList());
    }

    private void validateTrainingRequest(TrainingRequestDto request) {
        Set<ConstraintViolation<TrainingRequestDto>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            log.warn("Validation failed: {}", errorMessage);
            throw new IllegalArgumentException("Validation failed: " + errorMessage);
        }
    }

    private TrainingResponseDto mapToResponse(Training training) {
        return new TrainingResponseDto(
                training.getTrainee().getUsername(),
                training.getTrainer().getUsername(),
                training.getTrainingType().getTrainingTypeName(),
                training.getTrainingName(),
                training.getTrainingDate(),
                training.getTrainingDuration()
        );
    }

    private void authenticateTraineeOrThrow(String username, String password) {
        if (!authService.authenticateTrainee(username, password)) {
            log.warn("Authentication failed for {}", username);
            throw new SecurityException("Invalid username or password.");
        }
    }

    private void authenticateTrainerOrThrow(String username, String password) {
        if (!authService.authenticateTrainer(username, password)) {
            log.warn("Authentication failed for {}", username);
            throw new SecurityException("Invalid username or password.");
        }
    }
}