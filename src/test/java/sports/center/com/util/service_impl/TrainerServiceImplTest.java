package sports.center.com.util.service_impl;

import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sports.center.com.dto.trainer.TrainerRequestDto;
import sports.center.com.dto.trainer.TrainerResponseDto;
import sports.center.com.model.Trainer;
import sports.center.com.model.TrainingType;
import sports.center.com.repository.TrainerRepository;
import sports.center.com.repository.TrainingTypeRepository;
import sports.center.com.service.AuthService;
import sports.center.com.service.impl.TrainerServiceImpl;
import sports.center.com.util.UsernameUtil;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    @InjectMocks
    private TrainerServiceImpl trainerService;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private AuthService authService;

    @Mock
    private UsernameUtil usernameUtil;

    @Mock
    private Validator validator;

    private Trainer trainer;
    private TrainingType trainingType;
    private TrainerRequestDto trainerRequest;

    @BeforeEach
    void setUp() {
        trainingType = new TrainingType(1L, "Fitness", null, null);
        trainer = new Trainer("John", "Doe", "john.doe", "password12", true, trainingType);
        trainerRequest = new TrainerRequestDto("John", "Doe", 1L);
    }

    @Test
    void createTrainer() {
        when(validator.validate(any())).thenReturn(Set.of());
        when(usernameUtil.generateUsername("John", "Doe")).thenReturn("john.doe");
        when(trainingTypeRepository.findById(1L)).thenReturn(Optional.of(trainingType));
        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);

        TrainerResponseDto response = trainerService.createTrainer(trainerRequest);

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("john.doe", response.getUsername());
        verify(trainerRepository, times(1)).save(any(Trainer.class));
    }

    @Test
    void createTrainer_ThrowsException_WhenSpecializationNotFound() {
        when(trainingTypeRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            trainerService.createTrainer(trainerRequest);
        });

        assertEquals("Specialization not found", exception.getMessage());
    }

    @Test
    void authenticateTrainer() {
        when(authService.authenticateTrainer("john.doe", "password123")).thenReturn(true);

        boolean result = trainerService.authenticateTrainer("john.doe", "password123");

        assertTrue(result);
        verify(authService, times(1)).authenticateTrainer("john.doe", "password123");
    }

    @Test
    void getTrainerByUsername() {
        when(authService.authenticateTrainer("john.doe", "password123")).thenReturn(true);
        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainer));

        TrainerResponseDto response = trainerService.getTrainerByUsername("john.doe", "password123");

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
    }

    @Test
    void getTrainerByUsername_ThrowsException_WhenTrainerNotFound() {
        when(authService.authenticateTrainer("john.doe", "password123")).thenReturn(true);
        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            trainerService.getTrainerByUsername("john.doe", "password123");
        });

        assertEquals("Trainer not found: john.doe", exception.getMessage());
    }

    @Test
    void changeTrainerPassword() {
        when(authService.authenticateTrainer("john.doe", "oldPass")).thenReturn(true);
        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainer));

        // Пароль виправлено (тепер 13 символів)
        boolean result = trainerService.changeTrainerPassword("john.doe", "oldPass", "newPassword123");

        assertTrue(result);
        assertEquals("newPassword123", trainer.getPassword());
        verify(trainerRepository, times(1)).save(trainer);
    }

    @Test
    void updateTrainer() {
        when(validator.validate(any())).thenReturn(Set.of());
        when(authService.authenticateTrainer("john.doe", "password123")).thenReturn(true);
        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findById(1L)).thenReturn(Optional.of(trainingType));

        // Пароль виправлено (тепер 13 символів)
        boolean result = trainerService.updateTrainer("john.doe", "password123", trainerRequest, "newPassword123");

        assertTrue(result);
        assertEquals("newPassword123", trainer.getPassword());
        assertEquals("John", trainer.getFirstName());
        assertEquals("Doe", trainer.getLastName());
        verify(trainerRepository, times(1)).save(trainer);
    }


    @Test
    void updateTrainer_ThrowsException_WhenTrainerNotFound() {
        when(authService.authenticateTrainer("john.doe", "password123")).thenReturn(true);
        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            trainerService.updateTrainer("john.doe", "password123", trainerRequest, "newPass");
        });

        assertEquals("Trainer not found: john.doe", exception.getMessage());
    }

    @Test
    void changeTrainerStatus() {
        when(authService.authenticateTrainer("john.doe", "password123")).thenReturn(true);
        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainer));

        boolean result = trainerService.changeTrainerStatus("john.doe", "password123");

        assertFalse(trainer.getIsActive());
        assertFalse(result);
        verify(trainerRepository, times(1)).save(trainer);
    }

    @Test
    void deleteTrainer() {
        when(authService.authenticateTrainer("john.doe", "password123")).thenReturn(true);
        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainer));

        boolean result = trainerService.deleteTrainer("john.doe", "password123");

        assertTrue(result);
        verify(trainerRepository, times(1)).delete(trainer);
    }

    @Test
    void deleteTrainer_ThrowsException_WhenTrainerNotFound() {
        when(authService.authenticateTrainer("john.doe", "password123")).thenReturn(true);
        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            trainerService.deleteTrainer("john.doe", "password123");
        });

        assertEquals("Trainer not found: john.doe", exception.getMessage());
    }
}