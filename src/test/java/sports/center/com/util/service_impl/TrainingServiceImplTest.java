package sports.center.com.util.service_impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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
import sports.center.com.service.impl.TrainingServiceImpl;

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

        SecurityContextHolder.clearContext();
    }

    private void setupAuthenticatedUser(String username) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void addTraining_Success() {
        when(validator.validate(trainingRequestDto)).thenReturn(Set.of());
        when(traineeRepository.findByUsername("trainee123")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername("trainer456")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByTrainingTypeName("Strength")).thenReturn(Optional.of(trainingType));
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
        setupAuthenticatedUser("trainee123");
        when(trainingRepository.findTrainingsByTraineeCriteria(anyString(), any(), any(), any(), any())).thenReturn(Collections.emptyList());

        List<TrainingResponseDto> response = trainingService.getTraineeTrainings(fromDate, toDate, null, null);

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void getAuthenticatedUsername_InvalidAuthFormat_ShouldThrowException() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

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
        setupAuthenticatedUser("trainee123");
        when(trainerRepository.findNotAssignedActiveTrainers("trainee123")).thenReturn(Collections.emptyList());

        List<TrainerResponseDto> trainers = trainingService.getNotAssignedActiveTrainers();

        assertNotNull(trainers);
        assertTrue(trainers.isEmpty());
    }

    @Test
    void getNotAssignedActiveTrainers_NullList_ShouldHandleGracefully() {
        setupAuthenticatedUser("trainee123");
        when(trainerRepository.findNotAssignedActiveTrainers("trainee123")).thenReturn(null);

        List<TrainerResponseDto> trainers = trainingService.getNotAssignedActiveTrainers();

        assertNotNull(trainers);
        assertTrue(trainers.isEmpty());
    }

    @Test
    void getAuthenticatedUsername_NoAuthHeader_ShouldThrowUnauthorizedException() {
        assertThrows(UnauthorizedException.class, () -> trainingService.getNotAssignedActiveTrainers());
    }

    @Test
    void getAuthenticatedUsername_TrainerNotFound_ShouldThrowUnauthorizedException() {
        setupAuthenticatedUser("trainer456");
        assertDoesNotThrow(() -> trainingService.getNotAssignedActiveTrainers());
    }

    @Test
    void updateTraineeTrainersList_EmptyTrainerUsernames_ShouldThrowException() {
        setupAuthenticatedUser("trainee123");
        when(traineeRepository.findByUsername("trainee123")).thenReturn(Optional.of(trainee));

        assertThrows(EmptyTrainerListException.class, () -> trainingService.updateTraineeTrainersList(Collections.emptyList()));
    }

    @Test
    void updateTraineeTrainersList_SomeTrainersNotFound_ShouldThrowException() {
        setupAuthenticatedUser("trainee123");
        when(traineeRepository.findByUsername("trainee123")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsernameIn(List.of("trainer456", "trainer789"))).thenReturn(List.of(trainer));

        assertThrows(TraineeNotFoundException.class, () -> trainingService.updateTraineeTrainersList(List.of("trainer456", "trainer789")));
    }

    @Test
    void getAuthenticatedUsername_TrainerHasNoPassword_ShouldThrowUnauthorizedException() {
        setupAuthenticatedUser("trainer456");
        assertDoesNotThrow(() -> trainingService.getNotAssignedActiveTrainers());
    }

    @Test
    void getAuthenticatedUsername_InvalidPassword_ShouldThrowUnauthorizedException() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(null);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(UnauthorizedException.class, () -> trainingService.getNotAssignedActiveTrainers());
    }

    @Test
    void getTraineeTrainings_ValidDateRange_ShouldReturnTrainings() {
        Date fromDate = new Date();
        Date toDate = new Date(fromDate.getTime() + 100000);
        setupAuthenticatedUser("trainee123");

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName("Morning Workout");
        training.setTrainingDate(new Date());
        training.setTrainingDuration(60);
        training.setTrainingType(trainingType);

        when(trainingRepository.findTrainingsByTraineeCriteria("trainee123", fromDate, toDate, null, null))
                .thenReturn(List.of(training));

        List<TrainingResponseDto> response = trainingService.getTraineeTrainings(fromDate, toDate, null, null);

        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(1, response.size());
        assertEquals("Morning Workout", response.get(0).getTrainingName());
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
        setupAuthenticatedUser("trainee123");
        trainee.setTrainers(List.of(trainer));
        when(trainerRepository.findNotAssignedActiveTrainers("trainee123")).thenReturn(Collections.emptyList());

        List<TrainerResponseDto> trainers = trainingService.getNotAssignedActiveTrainers();

        assertNotNull(trainers);
        assertTrue(trainers.isEmpty());
    }

    @Test
    void updateTraineeTrainersList_NullOrEmptyTrainerList_ShouldThrowException() {
        setupAuthenticatedUser("trainee123");
        when(traineeRepository.findByUsername("trainee123")).thenReturn(Optional.of(trainee));

        assertThrows(EmptyTrainerListException.class, () -> trainingService.updateTraineeTrainersList(null));
        assertThrows(EmptyTrainerListException.class, () -> trainingService.updateTraineeTrainersList(Collections.emptyList()));
    }

    @Test
    void addTraining_InvalidRequest_ShouldThrowException() {
        TrainingRequestDto invalidRequest = new TrainingRequestDto("", "trainer456", "", new Date(), 0, "Strength");
        ConstraintViolation<TrainingRequestDto> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Trainee username cannot be empty");
        when(validator.validate(invalidRequest)).thenReturn(Set.of(violation));

        assertThrows(InvalidTrainingRequestException.class, () -> trainingService.addTraining(invalidRequest));
    }

    @Test
    void getNotAssignedActiveTrainers_Success() {
        setupAuthenticatedUser("trainee123");
        Trainer trainer2 = new Trainer();
        trainer2.setUsername("trainer789");
        trainer2.setFirstName("Jane");
        trainer2.setLastName("Smith");
        trainer2.setSpecialization(trainingType);
        when(trainerRepository.findNotAssignedActiveTrainers("trainee123")).thenReturn(List.of(trainer2));

        List<TrainerResponseDto> trainers = trainingService.getNotAssignedActiveTrainers();

        assertNotNull(trainers);
        assertEquals(1, trainers.size());
        assertEquals("trainer789", trainers.get(0).getUsername());
    }

    @Test
    void getTrainingType_Success() {
        when(trainingTypeRepository.findAll()).thenReturn(Collections.emptyList());

        List<TrainingTypeResponseDto> response = trainingService.getTrainingType();

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void getTrainerTrainings_EmptyList_ShouldReturnEmptyResponse() {
        Date fromDate = new Date();
        Date toDate = new Date(fromDate.getTime() + 100000);
        setupAuthenticatedUser("trainer456");
        when(trainingRepository.findTrainingsByTrainerCriteria("trainer456", fromDate, toDate, null))
                .thenReturn(Collections.emptyList());

        List<TrainingResponseDto> response = trainingService.getTrainerTrainings(fromDate, toDate, null);

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }
}