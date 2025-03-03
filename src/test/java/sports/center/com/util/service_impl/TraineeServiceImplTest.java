package sports.center.com.util.service_impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import sports.center.com.dto.trainee.TraineeRequestDto;
import sports.center.com.dto.trainee.TraineeResponseDto;
import sports.center.com.exception.exceptions.InvalidPasswordException;
import sports.center.com.exception.exceptions.InvalidTraineeRequestException;
import sports.center.com.exception.exceptions.UnauthorizedException;
import sports.center.com.model.Trainee;
import sports.center.com.repository.TraineeRepository;
import sports.center.com.service.impl.TraineeServiceImpl;
import sports.center.com.util.UsernameUtil;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private UsernameUtil usernameUtil;

    @Mock
    private Validator validator;

    @Mock
    private HttpServletRequest request;

    @Mock
    private PasswordEncoder passwordEncoder;


    @InjectMocks
    private TraineeServiceImpl traineeService;

    private Trainee trainee;
    private TraineeRequestDto traineeRequestDto;

    @BeforeEach
    void setUp() {
        trainee = new Trainee();
        trainee.setFirstName("John");
        trainee.setLastName("Doe");
        trainee.setUsername("johndoe");
        trainee.setPassword("password123");
        trainee.setIsActive(true);
        trainee.setTrainers(new ArrayList<>());

        traineeRequestDto = new TraineeRequestDto("John", "Doe", new Date(), "123 Street", true);
    }

    @Test
    void createTrainee_Success() {
        when(usernameUtil.generateUsername(any(), any())).thenReturn("johndoe");
        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

        TraineeResponseDto response = traineeService.createTrainee(traineeRequestDto);

        assertNotNull(response);
        assertEquals("johndoe", response.getUsername());
    }

    @Test
    void getTraineeProfile_TraineeExists() {
        when(request.getHeader("Authorization")).thenReturn("Basic am9obmRvZTpwYXNzd29yZDEyMw==");
        when(traineeRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainee));

        TraineeResponseDto response = traineeService.getTraineeProfile();

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
    }


    @Test
    void getTraineeProfile_TraineeNotFound() {
        when(request.getHeader("Authorization")).thenReturn("Basic am9obmRvZTpwYXNzd29yZDEyMw==");
        when(traineeRepository.findByUsername("johndoe")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> traineeService.getTraineeProfile());
    }

    @Test
    void getAuthenticatedUsername_MissingAuthHeader_ShouldThrowUnauthorizedException() {
        when(request.getHeader("Authorization")).thenReturn(null);
        assertThrows(UnauthorizedException.class, () -> traineeService.getTraineeProfile());
    }

    @Test
    void changeTraineePassword_SameAsOld_ShouldThrowException() {
        when(request.getHeader("Authorization")).thenReturn("Basic am9obmRvZTpwYXNzd29yZDEyMw==");
        when(traineeRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainee));
        assertThrows(InvalidPasswordException.class, () -> traineeService.changeTraineePassword("password123"));
    }

    @Test
    void changeTraineePassword_TooShort_ShouldThrowException() {
        when(request.getHeader("Authorization")).thenReturn("Basic am9obmRvZTpwYXNzd29yZDEyMw==");
        when(traineeRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainee));
        assertThrows(InvalidPasswordException.class, () -> traineeService.changeTraineePassword("short"));
    }

    @Test
    void deleteTrainee_NotAuthenticated_ShouldThrowUnauthorizedException() {
        when(request.getHeader("Authorization")).thenReturn(null);
        assertThrows(UnauthorizedException.class, () -> traineeService.deleteTrainee());
    }

    @Test
    void changeTraineePassword_Success() {
        when(request.getHeader("Authorization")).thenReturn("Basic am9obmRvZTpwYXNzd29yZDEyMw==");
        when(traineeRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainee));

        assertTrue(traineeService.changeTraineePassword("newPass123"));
        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void changeTraineePassword_InvalidPassword() {
        when(request.getHeader("Authorization")).thenReturn("Basic am9obmRvZTpwYXNzd29yZDEyMw==");
        when(traineeRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainee));

        assertThrows(InvalidPasswordException.class, () -> traineeService.changeTraineePassword("short"));
    }

    @Test
    void deleteTrainee_Success() {
        when(request.getHeader("Authorization")).thenReturn("Basic am9obmRvZTpwYXNzd29yZDEyMw==");
        when(traineeRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainee));

        assertTrue(traineeService.deleteTrainee());
        verify(traineeRepository).delete(any(Trainee.class));
    }

    @Test
    void getAuthenticatedUsername_Unauthorized() {
        when(request.getHeader("Authorization")).thenReturn(null);
        assertThrows(UnauthorizedException.class, () -> traineeService.getTraineeProfile());
    }

    @Test
    void updateTraineeProfile_Success() {
        when(request.getHeader("Authorization")).thenReturn("Basic am9obmRvZTpwYXNzd29yZDEyMw==");
        when(traineeRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

        TraineeResponseDto response = traineeService.updateTraineeProfile(traineeRequestDto);

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
    }

    @Test
    void changeTraineeStatus_Success() {
        when(request.getHeader("Authorization")).thenReturn("Basic am9obmRvZTpwYXNzd29yZDEyMw==");
        when(traineeRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(i -> i.getArgument(0));

        boolean previousStatus = trainee.getIsActive();
        boolean result = traineeService.changeTraineeStatus();

        assertNotEquals(previousStatus, result);
        assertEquals(!previousStatus, trainee.getIsActive());
        verify(traineeRepository).save(trainee);
    }

    @Test
    void changeTraineeStatus_TraineeNotFound_ShouldThrowUnauthorizedException() {
        when(request.getHeader("Authorization")).thenReturn("Basic am9obmRvZTpwYXNzd29yZDEyMw==");
        when(traineeRepository.findByUsername("johndoe")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> traineeService.changeTraineeStatus());
    }

    @Test
    void changeTraineeStatus_InvalidAuthFormat_ShouldThrowUnauthorizedException() {
        when(request.getHeader("Authorization")).thenReturn("Basic aW52YWxpZF9iYXNlNjQ=");

        assertThrows(UnauthorizedException.class, () -> traineeService.changeTraineeStatus());
    }

    @Test
    void changeTraineeStatus_InvalidAuthHeader_ShouldThrowUnauthorizedException() {
        when(request.getHeader("Authorization")).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> traineeService.changeTraineeStatus());
    }

    @Test
    void changeTraineeStatus_WrongPassword_ShouldThrowUnauthorizedException() {
        when(request.getHeader("Authorization")).thenReturn("Basic am9obmRvZTppbmNvcnJlY3RwYXNzd29yZA==");
        when(traineeRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainee));

        assertThrows(UnauthorizedException.class, () -> traineeService.changeTraineeStatus());
    }

    @Test
    void changeTraineeStatus_ToggleTwice_ShouldRestoreOriginalState() {
        when(request.getHeader("Authorization")).thenReturn("Basic am9obmRvZTpwYXNzd29yZDEyMw==");
        when(traineeRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(i -> i.getArgument(0));

        boolean initialStatus = trainee.getIsActive();

        boolean firstToggle = traineeService.changeTraineeStatus();
        assertNotEquals(initialStatus, firstToggle);
        assertEquals(!initialStatus, trainee.getIsActive());

        boolean secondToggle = traineeService.changeTraineeStatus();
        assertEquals(initialStatus, secondToggle);
        assertEquals(initialStatus, trainee.getIsActive());

        verify(traineeRepository, times(2)).save(trainee);
    }

    @Test
    void getTraineeProfile_InvalidToken_ShouldThrowUnauthorizedException() {
        when(request.getHeader("Authorization")).thenReturn("InvalidToken");

        assertThrows(UnauthorizedException.class, () -> traineeService.getTraineeProfile());
    }

    @Test
    void changeTraineePassword_WeakPassword_ShouldThrowException() {
        when(request.getHeader("Authorization")).thenReturn("Basic am9obmRvZTpwYXNzd29yZDEyMw==");
        when(traineeRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainee));

        assertThrows(InvalidPasswordException.class, () -> traineeService.changeTraineePassword("12345678"));
    }

    @Test
    void deleteTrainee_UserDoesNotExist_ShouldThrowUnauthorizedException() {
        when(request.getHeader("Authorization")).thenReturn("Basic am9obmRvZTpwYXNzd29yZDEyMw==");
        when(traineeRepository.findByUsername("johndoe")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> traineeService.deleteTrainee());
    }

    @Test
    void deleteTrainee_Unauthenticated_ShouldThrowUnauthorizedException() {
        when(request.getHeader("Authorization")).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> traineeService.deleteTrainee());
    }
}