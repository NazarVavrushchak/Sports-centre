package sports.center.com.util.service_impl;

import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sports.center.com.dto.trainee.TraineeRequestDto;
import sports.center.com.dto.trainee.TraineeResponseDto;
import sports.center.com.model.Trainee;
import sports.center.com.repository.TraineeRepository;
import sports.center.com.service.AuthService;
import sports.center.com.service.impl.TraineeServiceImpl;
import sports.center.com.util.UsernameUtil;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private UsernameUtil usernameUtil;

    @Mock
    private AuthService authService;

    @InjectMocks
    private TraineeServiceImpl traineeService;

    @Mock
    private Validator validator;

    private Trainee trainee;
    private TraineeRequestDto traineeRequest;

    @BeforeEach
    void setUp() {
        trainee = new Trainee();
        trainee.setFirstName("John");
        trainee.setLastName("Doe");
        trainee.setUsername("john.doe");
        trainee.setPassword("password123");
        trainee.setIsActive(true);
        trainee.setDateOfBirth(new Date());
        trainee.setAddress("123 Main St");

        traineeRequest = new TraineeRequestDto("John", "Doe", new Date(), "123 Main St");
    }

    @Test
    void createTrainee() {
        when(validator.validate(any())).thenReturn(Set.of());
        when(usernameUtil.generateUsername("John", "Doe")).thenReturn("john.doe");
        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

        TraineeResponseDto response = traineeService.createTrainee(traineeRequest);

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("john.doe", response.getUsername());
        verify(traineeRepository, times(1)).save(any(Trainee.class));
    }

    @Test
    void authenticateTrainee() {
        when(authService.authenticateTrainee("john.doe", "password123")).thenReturn(true);

        boolean result = traineeService.authenticateTrainee("john.doe", "password123");

        assertTrue(result);
        verify(authService, times(1)).authenticateTrainee("john.doe", "password123");
    }

    @Test
    void getTraineeByUsername() {
        when(authService.authenticateTrainee("john.doe", "password123")).thenReturn(true);
        when(traineeRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainee));

        TraineeResponseDto response = traineeService.getTraineeByUsername("john.doe", "password123");

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        verify(traineeRepository, times(1)).findByUsername("john.doe");
    }

    @Test
    void getTraineeByUsername_NotFound() {
        when(authService.authenticateTrainee("john.doe", "password123")).thenReturn(true);
        when(traineeRepository.findByUsername("john.doe")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            traineeService.getTraineeByUsername("john.doe", "password123");
        });

        assertEquals("Trainee not found: john.doe", exception.getMessage());
    }

    @Test
    void changeTraineePassword() {
        when(authService.authenticateTrainee("john.doe", "password123")).thenReturn(true);
        when(traineeRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainee));

        boolean result = traineeService.changeTraineePassword("john.doe", "password123", "newPassword");

        assertTrue(result);
        assertEquals("newPassword", trainee.getPassword());
        verify(traineeRepository, times(1)).save(trainee);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void updateTrainee() {
        when(validator.validate(any())).thenReturn(Set.of());
        when(authService.authenticateTrainee("john.doe", "password12")).thenReturn(true);
        when(traineeRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainee));
        when(usernameUtil.generateUsername("John", "Doe")).thenReturn("john.doe");

        boolean result = traineeService.updateTrainee("john.doe", "password12", traineeRequest, "newPassword123");

        assertTrue(result);
        assertEquals("newPassword123", trainee.getPassword());
        assertEquals("John", trainee.getFirstName());
        assertEquals("Doe", trainee.getLastName());
        verify(traineeRepository, times(1)).save(trainee);
    }

    @Test
    void changeTraineeStatus() {
        when(authService.authenticateTrainee("john.doe", "password123")).thenReturn(true);
        when(traineeRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainee));

        boolean isActiveNow = traineeService.changeTraineeStatus("john.doe", "password123");

        assertFalse(trainee.getIsActive());
        assertEquals(isActiveNow, trainee.getIsActive());
        verify(traineeRepository, times(1)).save(trainee);
    }

    @Test
    void deleteTrainee() {
        when(authService.authenticateTrainee("john.doe", "password123")).thenReturn(true);
        when(traineeRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainee));

        boolean result = traineeService.deleteTrainee("john.doe", "password123");

        assertTrue(result);
        verify(traineeRepository, times(1)).delete(trainee);
    }
}