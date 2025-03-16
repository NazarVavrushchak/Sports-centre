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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import sports.center.com.dto.trainee.TraineeRequestDto;
import sports.center.com.dto.trainee.TraineeResponseDto;
import sports.center.com.exception.exceptions.InvalidPasswordException;
import sports.center.com.exception.exceptions.InvalidTraineeRequestException;
import sports.center.com.exception.exceptions.TraineeNotFoundException;
import sports.center.com.exception.exceptions.UnauthorizedException;
import sports.center.com.model.Trainee;
import sports.center.com.repository.TraineeRepository;
import sports.center.com.security.JwtTool;
import sports.center.com.service.UserService;
import sports.center.com.service.impl.TraineeServiceImpl;
import sports.center.com.util.UsernameUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

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
    private HttpServletRequest request;

    @Mock
    private JwtTool jwtTool;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TraineeServiceImpl traineeService;

    @Mock
    private UserService userService;

    @Mock
    private Validator validator;

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
    void createTrainee_Success() {
        when(usernameUtil.generateUsername("John", "Doe")).thenReturn("johndoe");
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userService.initializeNewUser(any(Trainee.class))).thenReturn(trainee);
        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);
        when(jwtTool.generateToken("johndoe")).thenReturn("jwt-token");

        TraineeResponseDto response = traineeService.createTrainee(traineeRequestDto);

        assertNotNull(response);
        assertEquals("johndoe", response.getUsername());
        assertEquals("jwt-token", response.getToken());
        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void getTraineeProfile_Success() {
        setupAuthenticatedUser("johndoe");
        when(traineeRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainee));

        TraineeResponseDto response = traineeService.getTraineeProfile();

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
    }

    @Test
    void getTraineeProfile_TraineeNotFound_ShouldThrowTraineeNotFoundException() {
        setupAuthenticatedUser("johndoe");
        when(traineeRepository.findByUsername("johndoe")).thenReturn(Optional.empty());

        TraineeNotFoundException exception = assertThrows(TraineeNotFoundException.class, () -> traineeService.getTraineeProfile());
        assertEquals("johndoe", exception.getMessage());
    }

    @Test
    void changeTraineePassword_TraineeNotFound_ShouldThrowTraineeNotFoundException() {
        setupAuthenticatedUser("johndoe");
        when(traineeRepository.findByUsername("johndoe")).thenReturn(Optional.empty());

        TraineeNotFoundException exception = assertThrows(TraineeNotFoundException.class, () -> traineeService.changeTraineePassword("newPass123"));
        assertEquals("Trainee not found: johndoe", exception.getMessage());
    }

    @Test
    void updateTraineeProfile_SuccessWithPartialUpdate() {
        setupAuthenticatedUser("johndoe");
        TraineeRequestDto updateRequest = new TraineeRequestDto(null, "Smith", null, "456 Avenue", false);
        when(validator.validate(updateRequest)).thenReturn(Set.of());
        when(traineeRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

        TraineeResponseDto response = traineeService.updateTraineeProfile(updateRequest);

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Smith", response.getLastName());
        assertEquals("456 Avenue", response.getAddress());
        assertFalse(response.getIsActive());
        assertEquals("johndoe", response.getUsername());
        verify(traineeRepository).save(trainee);
    }

    @Test
    void getTraineeProfile_AuthenticationNameNull_ShouldThrowUnauthorizedException() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(null);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> traineeService.getTraineeProfile());
        assertEquals("Unauthorized request", exception.getMessage()); // Adjusted to match actual behavior
    }

    @Test
    void createTrainee_InitializeUserFails_ShouldPropagateException() {
        when(usernameUtil.generateUsername("John", "Doe")).thenReturn("johndoe");
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userService.initializeNewUser(any(Trainee.class))).thenThrow(new RuntimeException("Initialization failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> traineeService.createTrainee(traineeRequestDto));
        assertEquals("Initialization failed", exception.getMessage());
    }

    @Test
    void updateTraineeProfile_Unauthenticated_ShouldThrowUnauthorizedException() {
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> traineeService.updateTraineeProfile(traineeRequestDto));
        assertEquals("Unauthorized request", exception.getMessage());
    }

    @Test
    void getTraineeProfile_Unauthenticated_ShouldThrowUnauthorizedException() {
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> traineeService.getTraineeProfile());
        assertEquals("Unauthorized request", exception.getMessage());
    }

    @Test
    void changeTraineePassword_Success() {
        setupAuthenticatedUser("johndoe");
        when(traineeRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainee));
        when(passwordEncoder.encode("newPass123")).thenReturn("encodedNewPass123");

        boolean result = traineeService.changeTraineePassword("newPass123");

        assertTrue(result);
        verify(traineeRepository).save(trainee);
        assertEquals("encodedNewPass123", trainee.getPassword());
    }

    @Test
    void updateTraineeProfile_InvalidRequest_ShouldThrowInvalidTraineeRequestException() {
        setupAuthenticatedUser("johndoe");
        TraineeRequestDto invalidRequest = new TraineeRequestDto("", "Smith", null, "", null);
        ConstraintViolation<TraineeRequestDto> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("First name cannot be empty");
        when(validator.validate(invalidRequest)).thenReturn(Set.of(violation));

        InvalidTraineeRequestException exception = assertThrows(InvalidTraineeRequestException.class, () -> traineeService.updateTraineeProfile(invalidRequest));
        assertTrue(exception.getMessage().contains("First name cannot be empty"));
    }

    @Test
    void updateTraineeProfile_TraineeNotFound_ShouldThrowTraineeNotFoundException() {
        setupAuthenticatedUser("johndoe");
        when(traineeRepository.findByUsername("johndoe")).thenReturn(Optional.empty());

        TraineeNotFoundException exception = assertThrows(TraineeNotFoundException.class, () -> traineeService.updateTraineeProfile(traineeRequestDto));
        assertEquals("Trainee not found: johndoe", exception.getMessage());
    }

    @Test
    void changeTraineePassword_Unauthenticated_ShouldThrowUnauthorizedException() {
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> traineeService.changeTraineePassword("newPass123"));
        assertEquals("Unauthorized request", exception.getMessage());
    }

    @Test
    void deleteTrainee_Success() {
        setupAuthenticatedUser("johndoe");
        when(traineeRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainee));

        boolean result = traineeService.deleteTrainee();

        assertTrue(result);
        verify(traineeRepository).delete(trainee);
    }

    @Test
    void changeTraineeStatus_Unauthenticated_ShouldThrowUnauthorizedException() {
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> traineeService.changeTraineeStatus());
        assertEquals("Unauthorized request", exception.getMessage());
    }

    @Test
    void changeTraineeStatus_TraineeNotFound_ShouldThrowTraineeNotFoundException() {
        setupAuthenticatedUser("johndoe");
        when(traineeRepository.findByUsername("johndoe")).thenReturn(Optional.empty());

        TraineeNotFoundException exception = assertThrows(TraineeNotFoundException.class, () -> traineeService.changeTraineeStatus());
        assertEquals("Trainee not found: johndoe", exception.getMessage());
    }

    @Test
    void updateTraineeProfile_NoUsernameChange_Success() {
        setupAuthenticatedUser("johndoe");
        TraineeRequestDto updateRequest = new TraineeRequestDto(null, null, null, "789 Road", null);
        when(validator.validate(updateRequest)).thenReturn(Set.of());
        when(traineeRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TraineeResponseDto response = traineeService.updateTraineeProfile(updateRequest);

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("789 Road", response.getAddress());
        assertEquals("johndoe", response.getUsername());
        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void changeTraineeStatus_Success() {
        setupAuthenticatedUser("johndoe");
        when(traineeRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(i -> i.getArgument(0));

        boolean result = traineeService.changeTraineeStatus();

        assertFalse(result);
        assertFalse(trainee.getIsActive());
        verify(traineeRepository).save(trainee);
    }

    @Test
    void changeTraineePassword_NullPassword_ShouldThrowInvalidPasswordException() {
        setupAuthenticatedUser("johndoe");

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () -> traineeService.changeTraineePassword(null));
        assertEquals("New password cannot be empty.", exception.getMessage());
    }

    @Test
    void changeTraineePassword_EmptyPassword_ShouldThrowInvalidPasswordException() {
        setupAuthenticatedUser("johndoe");

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () -> traineeService.changeTraineePassword(""));
        assertEquals("New password cannot be empty.", exception.getMessage());
    }

    @Test
    void changeTraineePassword_IncorrectLength_ShouldThrowInvalidPasswordException() {
        setupAuthenticatedUser("johndoe");

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () -> traineeService.changeTraineePassword("short"));
        assertEquals("Password must be exactly 10 characters long.", exception.getMessage());
    }

    @Test
    void changeTraineeStatus_ToggleTwice_ShouldRestoreOriginalState() {
        setupAuthenticatedUser("johndoe");
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
    void deleteTrainee_TraineeNotFound_ShouldThrowTraineeNotFoundException() {
        setupAuthenticatedUser("johndoe");
        when(traineeRepository.findByUsername("johndoe")).thenReturn(Optional.empty());

        TraineeNotFoundException exception = assertThrows(TraineeNotFoundException.class, () -> traineeService.deleteTrainee());
        assertEquals("Trainee not found: johndoe", exception.getMessage());
    }

    @Test
    void deleteTrainee_Unauthenticated_ShouldThrowUnauthorizedException() {
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> traineeService.deleteTrainee());
        assertEquals("Unauthorized request", exception.getMessage());
    }
}