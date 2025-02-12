package sports.center.com.util.service_impl;

import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import sports.center.com.service.impl.TrainingServiceImpl;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    @InjectMocks
    private TrainingServiceImpl trainingService;

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private AuthService authService;

    @Mock
    private Validator validator;

    private TrainingRequestDto trainingRequest;
    private Training training;
    private Trainee trainee;
    private Trainer trainer;
    private TrainingType trainingType;

    @BeforeEach
    void setUp() {
        trainee = new Trainee();
        trainee.setId(1L);
        trainee.setUsername("trainee1");
        trainee.setTrainers(new ArrayList<>());

        trainer = new Trainer();
        trainer.setId(2L);
        trainer.setUsername("trainer1");

        trainingType = new TrainingType();
        trainingType.setId(3L);
        trainingType.setTrainingTypeName("Yoga");
        trainer.setSpecialization(trainingType);

        trainingRequest = new TrainingRequestDto(
                1L, 2L, 3L, "Morning Yoga", new Date(), 60
        );

        training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(trainingType);
        training.setTrainingName("Morning Yoga");
        training.setTrainingDate(new Date());
        training.setTrainingDuration(60);
    }

    @Test
    void addTraining() {
        when(validator.validate(any())).thenReturn(Set.of());
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findById(3L)).thenReturn(Optional.of(trainingType));
        when(trainingRepository.save(any(Training.class))).thenReturn(training);

        TrainingResponseDto response = trainingService.addTraining(trainingRequest);

        assertNotNull(response);
        assertEquals("trainee1", response.getTraineeName());
        assertEquals("trainer1", response.getTrainerName());
        assertEquals("Yoga", response.getTrainingType());
        assertEquals("Morning Yoga", response.getTrainingName());

        verify(trainingRepository, times(1)).save(any(Training.class));
    }


    @Test
    void addTraining_TraineeNotFound() {
        when(traineeRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            trainingService.addTraining(trainingRequest);
        });

        assertEquals("Trainee not found with ID: 1", exception.getMessage());
    }

    @Test
    void addTraining_TrainerNotFound() {
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findById(2L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            trainingService.addTraining(trainingRequest);
        });

        assertEquals("Trainer not found with ID: 2", exception.getMessage());
    }

    @Test
    void addTraining_TrainingTypeNotFound() {
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findById(3L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            trainingService.addTraining(trainingRequest);
        });

        assertEquals("Training Type not found with ID: 3", exception.getMessage());
    }

    @Test
    void getTraineeTrainings() {
        when(authService.authenticateTrainee("trainee1", "password123")).thenReturn(true);
        when(trainingRepository.findTrainingsByTraineeCriteria("trainee1", null, null, null, null))
                .thenReturn(List.of(training));

        List<TrainingResponseDto> response = trainingService.getTraineeTrainings("trainee1", "password123", null, null, null, null);

        assertEquals(1, response.size());
        assertEquals("Morning Yoga", response.get(0).getTrainingName());
    }

    @Test
    void getTraineeTrainings_Fail_InvalidPassword() {
        when(authService.authenticateTrainee("trainee1", "wrongPassword")).thenReturn(false);

        Exception exception = assertThrows(SecurityException.class, () ->
                trainingService.getTraineeTrainings("trainee1", "wrongPassword", null, null, null, null));

        assertEquals("Invalid username or password.", exception.getMessage());
    }

    @Test
    void getUnassignedTrainers_Success() {
        when(authService.authenticateTrainee("trainee1", "password123")).thenReturn(true);
        when(trainerRepository.findUnassignedTrainers("trainee1")).thenReturn(List.of(trainer));

        List<TrainerResponseDto> response = trainingService.getUnassignedTrainers("trainee1", "password123");

        assertEquals(1, response.size());
        assertEquals("trainer1", response.get(0).getUsername());
    }

    @Test
    void getUnassignedTrainers_Fail_InvalidPassword() {
        when(authService.authenticateTrainee("trainee1", "wrongPassword")).thenReturn(false);

        Exception exception = assertThrows(SecurityException.class, () ->
                trainingService.getUnassignedTrainers("trainee1", "wrongPassword"));

        assertEquals("Invalid username or password.", exception.getMessage());
    }

    @Test
    void updateTraineeTrainers_Success() {
        when(authService.authenticateTrainee("trainee1", "password123")).thenReturn(true);
        when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsernameIn(List.of("trainer1"))).thenReturn(List.of(trainer));

        List<TrainerResponseDto> response = trainingService.updateTraineeTrainers("trainee1", "password123", List.of("trainer1"));

        assertEquals(1, response.size());
        assertEquals("trainer1", response.get(0).getUsername());
        verify(traineeRepository, times(1)).save(trainee);
    }

    @Test
    void updateTraineeTrainers_Fail_TraineeNotFound() {
        when(authService.authenticateTrainee("trainee1", "password123")).thenReturn(true);
        when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                trainingService.updateTraineeTrainers("trainee1", "password123", List.of("trainer1")));

        assertEquals("Trainee not found with username: trainee1", exception.getMessage());
    }
}