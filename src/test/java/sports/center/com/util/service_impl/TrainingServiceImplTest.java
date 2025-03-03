package sports.center.com.util.service_impl;

import jakarta.servlet.http.HttpServletRequest;
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
import sports.center.com.exception.exceptions.*;
import sports.center.com.model.Trainee;
import sports.center.com.model.Trainer;
import sports.center.com.model.Training;
import sports.center.com.model.TrainingType;
import sports.center.com.repository.TraineeRepository;
import sports.center.com.repository.TrainerRepository;
import sports.center.com.repository.TrainingRepository;
import sports.center.com.repository.TrainingTypeRepository;
import sports.center.com.service.impl.TrainingServiceImpl;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private Validator validator;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    private Trainee trainee;
    private Trainer trainer;
    private TrainingType trainingType;
    private TrainingRequestDto trainingRequestDto;

    @BeforeEach
    void setUp() {
        trainee = new Trainee();
        trainee.setUsername("trainee123");
        trainee.setPassword("password123");
        trainee.setTrainers(new ArrayList<>());

        trainer = new Trainer();
        trainer.setUsername("trainer456");
        trainer.setPassword("password123");
        trainer.setTrainees(new ArrayList<>());

        trainingType = new TrainingType();
        trainingType.setId(1L);
        trainingType.setTrainingTypeName("Strength");

        trainingRequestDto = new TrainingRequestDto("trainee123", "trainer456", "Morning Workout", new Date(), 60, "Strength");
    }

    @Test
    void addTraining_Success() {
        when(traineeRepository.findByUsername(anyString())).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername(anyString())).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByTrainingTypeName(anyString())).thenReturn(Optional.of(trainingType));
        when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TrainingResponseDto response = trainingService.addTraining(trainingRequestDto);

        assertNotNull(response);
        assertEquals("trainee123", response.getTraineeUsername());
        assertEquals("trainer456", response.getTrainerUsername());
        assertEquals("Strength", response.getTrainingTypeName());
        verify(trainingRepository).save(any(Training.class));
    }

    @Test
    void addTraining_TraineeNotFound_ShouldThrowException() {
        when(traineeRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        assertThrows(TraineeNotFoundException.class, () -> trainingService.addTraining(trainingRequestDto));
    }

    @Test
    void addTraining_TrainerNotFound_ShouldThrowException() {
        when(traineeRepository.findByUsername(anyString())).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        assertThrows(TrainerNotFoundException.class, () -> trainingService.addTraining(trainingRequestDto));
    }

    @Test
    void addTraining_TrainingTypeNotFound_ShouldThrowException() {
        when(traineeRepository.findByUsername(anyString())).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername(anyString())).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByTrainingTypeName(anyString())).thenReturn(Optional.empty());
        assertThrows(TrainingTypeNotFoundException.class, () -> trainingService.addTraining(trainingRequestDto));
    }

    @Test
    void getTraineeTrainings_InvalidDateRange_ShouldReturnEmptyList() {
        Date toDate = new Date();
        Date fromDate = new Date(toDate.getTime() + 10000);

        String encodedCredentials = Base64.getEncoder().encodeToString("trainee:password123".getBytes(StandardCharsets.UTF_8));
        when(request.getHeader("Authorization")).thenReturn("Basic " + encodedCredentials);

        Trainee trainee = new Trainee();
        trainee.setUsername("trainee");
        trainee.setPassword("password123");

        when(traineeRepository.findByUsername("trainee")).thenReturn(Optional.of(trainee));
        when(trainingRepository.findTrainingsByTraineeCriteria(any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());

        List<TrainingResponseDto> response = trainingService.getTraineeTrainings(fromDate, toDate, null, null);

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void getAuthenticatedUsername_InvalidAuthFormat_ShouldThrowException() {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidToken");
        assertThrows(UnauthorizedException.class, () -> trainingService.getNotAssignedActiveTrainers());
    }

    @Test
    void addTraining_NullTrainingRequest_ShouldThrowException() {
        assertThrows(InvalidTrainerRequestException.class, () -> trainingService.addTraining(null));
    }

    @Test
    void addTraining_TraineeHasNoTrainers_ShouldAssignTrainer() {
        trainee.setTrainers(null);
        when(traineeRepository.findByUsername(anyString())).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername(anyString())).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByTrainingTypeName(anyString())).thenReturn(Optional.of(trainingType));
        when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TrainingResponseDto response = trainingService.addTraining(trainingRequestDto);

        assertNotNull(response);
        assertEquals("trainer456", trainee.getTrainers().get(0).getUsername());
    }

    @Test
    void getNotAssignedActiveTrainers_EmptyList_ShouldReturnEmptyResponse() {
        when(request.getHeader("Authorization")).thenReturn("Basic dHJhaW5lZTpwYXNzd29yZDEyMw==");

        trainee.setPassword("password123");
        doReturn(Optional.of(trainee)).when(traineeRepository).findByUsername(anyString());

        doReturn(new ArrayList<>()).when(trainerRepository).findNotAssignedActiveTrainers(anyString());

        List<TrainerResponseDto> trainers = trainingService.getNotAssignedActiveTrainers();

        assertNotNull(trainers);
        assertTrue(trainers.isEmpty());
    }

    @Test
    void getNotAssignedActiveTrainers_NullList_ShouldHandleGracefully() {
        when(request.getHeader("Authorization")).thenReturn("Basic dHJhaW5lZTpwYXNzd29yZDEyMw==");

        trainee.setPassword("password123");
        doReturn(Optional.of(trainee)).when(traineeRepository).findByUsername(anyString());

        doReturn(Collections.emptyList()).when(trainerRepository).findNotAssignedActiveTrainers(anyString());

        List<TrainerResponseDto> trainers = trainingService.getNotAssignedActiveTrainers();

        assertNotNull(trainers);
        assertTrue(trainers.isEmpty());
    }


    @Test
    void getAuthenticatedUsername_NoAuthHeader_ShouldThrowUnauthorizedException() {
        when(request.getHeader("Authorization")).thenReturn(null);
        assertThrows(UnauthorizedException.class, () -> trainingService.getNotAssignedActiveTrainers());
    }

    @Test
    void getAuthenticatedUsername_TrainerNotFound_ShouldThrowUnauthorizedException() {
        when(request.getHeader("Authorization")).thenReturn("Basic dHJhaW5lZToxMjM0");
        when(trainerRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> trainingService.getNotAssignedActiveTrainers());
    }

    @Test
    void updateTraineeTrainersList_EmptyTrainerUsernames_ShouldThrowException() {
        when(request.getHeader("Authorization")).thenReturn("Basic dHJhaW5lZTpwYXNzd29yZDEyMw==");
        trainee.setPassword("password123");
        doReturn(Optional.of(trainee)).when(traineeRepository).findByUsername(anyString());
        assertThrows(EmptyTrainerListException.class, () -> trainingService.updateTraineeTrainersList(new ArrayList<>()));
    }

    @Test
    void updateTraineeTrainersList_SomeTrainersNotFound_ShouldThrowException() {
        when(request.getHeader("Authorization")).thenReturn("Basic dHJhaW5lZTpwYXNzd29yZDEyMw==");
        trainee.setPassword("password123");
        doReturn(Optional.of(trainee)).when(traineeRepository).findByUsername(anyString());

        when(trainerRepository.findByUsernameIn(any())).thenReturn(List.of(trainer));

        assertThrows(TraineeNotFoundException.class, () -> trainingService.updateTraineeTrainersList(List.of("trainer456", "trainer789")));
    }

    @Test
    void getAuthenticatedUsername_TrainerHasNoPassword_ShouldThrowUnauthorizedException() {
        when(request.getHeader("Authorization")).thenReturn("Basic dHJhaW5lZTpwYXNzd29yZDEyMw==");
        trainer.setPassword(null);
        when(trainerRepository.findByUsername(anyString())).thenReturn(Optional.of(trainer));

        assertThrows(UnauthorizedException.class, () -> trainingService.getNotAssignedActiveTrainers());
    }

    @Test
    void updateTraineeTrainersList_WithValidData_ShouldUpdateSuccessfully() {
        when(request.getHeader("Authorization")).thenReturn("Basic dHJhaW5lZTpwYXNzd29yZDEyMw==");
        trainee.setTrainers(new ArrayList<>());
        when(traineeRepository.findByUsername(anyString())).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsernameIn(any())).thenReturn(List.of(trainer));
        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

        trainer.setSpecialization(new TrainingType());
        trainer.getSpecialization().setId(1L);

        List<TrainerResponseDto> response = trainingService.updateTraineeTrainersList(List.of("trainer456"));
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("trainer456", response.get(0).getUsername());
    }

    @Test
    void getAuthenticatedUsername_InvalidPassword_ShouldThrowUnauthorizedException() {
        String encodedCredentials = Base64.getEncoder().encodeToString("trainee:wrongPassword".getBytes(StandardCharsets.UTF_8));
        when(request.getHeader("Authorization")).thenReturn("Basic " + encodedCredentials);

        trainee.setPassword("password123");
        when(traineeRepository.findByUsername(anyString())).thenReturn(Optional.of(trainee));

        assertThrows(UnauthorizedException.class, () -> trainingService.getNotAssignedActiveTrainers());
    }

    @Test
    void getTraineeTrainings_ValidDateRange_ShouldReturnTrainings() {
        Date fromDate = new Date();
        Date toDate = new Date(fromDate.getTime() + 100000);

        when(request.getHeader("Authorization")).thenReturn("Basic dHJhaW5lZTpwYXNzd29yZDEyMw==");
        when(traineeRepository.findByUsername(anyString())).thenReturn(Optional.of(trainee));

        Training training = new Training();
        training.setId(1L);
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(trainingType);

        when(trainingRepository.findTrainingsByTraineeCriteria(any(), any(), any(), any(), any()))
                .thenReturn(List.of(training));

        List<TrainingResponseDto> response = trainingService.getTraineeTrainings(fromDate, toDate, null, null);

        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(1, response.size());
    }

    @Test
    void addTraining_ShouldSaveTraining() {
        when(traineeRepository.findByUsername(anyString())).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername(anyString())).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByTrainingTypeName(anyString())).thenReturn(Optional.of(trainingType));
        when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TrainingResponseDto response = trainingService.addTraining(trainingRequestDto);

        assertNotNull(response);
        verify(trainingRepository, times(1)).save(any(Training.class));
    }

    @Test
    void getNotAssignedActiveTrainers_UserHasAllTrainers_ShouldReturnEmptyList() {
        when(request.getHeader("Authorization")).thenReturn("Basic dHJhaW5lZTpwYXNzd29yZDEyMw==");

        trainee.setPassword("password123");

        Trainer trainer1 = new Trainer();
        trainer1.setUsername("trainer1");

        Trainer trainer2 = new Trainer();
        trainer2.setUsername("trainer2");

        List<Trainer> assignedTrainers = List.of(trainer1, trainer2);
        trainee.setTrainers(assignedTrainers);

        doReturn(Optional.of(trainee)).when(traineeRepository).findByUsername(anyString());
        doReturn(new ArrayList<>()).when(trainerRepository).findNotAssignedActiveTrainers(anyString());

        List<TrainerResponseDto> trainers = trainingService.getNotAssignedActiveTrainers();

        assertNotNull(trainers);
        assertTrue(trainers.isEmpty());
    }

    @Test
    void updateTraineeTrainersList_NullOrEmptyTrainerList_ShouldThrowException() {
        when(request.getHeader("Authorization")).thenReturn("Basic dHJhaW5lZTpwYXNzd29yZDEyMw==");
        when(traineeRepository.findByUsername(anyString())).thenReturn(Optional.of(trainee));

        assertThrows(EmptyTrainerListException.class, () -> trainingService.updateTraineeTrainersList(null));
        assertThrows(EmptyTrainerListException.class, () -> trainingService.updateTraineeTrainersList(Collections.emptyList()));
    }
}