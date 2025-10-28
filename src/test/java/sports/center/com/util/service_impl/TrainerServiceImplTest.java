package sports.center.com.util.service_impl;

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
import sports.center.com.dto.trainer.TrainerRequestDto;
import sports.center.com.dto.trainer.TrainerResponseDto;
import sports.center.com.exception.exceptions.*;
import sports.center.com.model.Trainer;
import sports.center.com.model.TrainingType;
import sports.center.com.repository.TrainerRepository;
import sports.center.com.repository.TrainingTypeRepository;
import sports.center.com.security.JwtTool;
import sports.center.com.service.UserService;
import sports.center.com.service.impl.TrainerServiceImpl;
import sports.center.com.util.UsernameUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private UsernameUtil usernameUtil;

    @Mock
    private Validator validator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserService userService;

    @Mock
    private JwtTool jwtTool;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    private Trainer trainer;
    private TrainerRequestDto trainerRequestDto;

    @BeforeEach
    void setUp() {
        trainer = new Trainer();
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setUsername("johndoe");
        trainer.setPassword("password123");
        trainer.setIsActive(true);
        trainer.setSpecialization(new TrainingType());
        trainer.setTrainees(new ArrayList<>());

        trainerRequestDto = new TrainerRequestDto("John", "Doe", 1L, true);

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
    void createTrainer_InvalidRequest_ShouldThrowInvalidTrainerRequestException() {
        TrainerRequestDto invalidRequest = new TrainerRequestDto("", "", null, null);

        doThrow(new InvalidTrainerRequestException("Validation failed", Set.of()))
                .when(validator).validate(any(TrainerRequestDto.class));

        assertThrows(InvalidTrainerRequestException.class, () -> trainerService.createTrainer(invalidRequest));
    }

    @Test
    void createTrainer_Success() {
        TrainingType trainingType = new TrainingType();
        trainingType.setId(1L);

        when(usernameUtil.generateUsername("John", "Doe")).thenReturn("johndoe");
        when(trainingTypeRepository.findById(1L)).thenReturn(Optional.of(trainingType));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userService.initializeNewUser(any(Trainer.class))).thenReturn(trainer);
        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);
        when(jwtTool.generateToken("johndoe")).thenReturn("jwt-token");

        TrainerResponseDto response = trainerService.createTrainer(trainerRequestDto);

        assertNotNull(response);
        assertEquals("johndoe", response.getUsername());
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void createTrainer_SpecializationNotFound_ShouldThrowException() {
        when(trainingTypeRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(SpecializationNotFoundException.class, () -> trainerService.createTrainer(trainerRequestDto));
    }

    @Test
    void createTrainer_NullSpecialization_ShouldThrowSpecializationNotFoundException() {
        TrainerRequestDto requestDto = new TrainerRequestDto("John", "Doe", null, true);

        assertThrows(SpecializationNotFoundException.class, () -> trainerService.createTrainer(requestDto));
    }

    @Test
    void getTrainerProfile_TrainerExists() {
        setupAuthenticatedUser("johndoe");
        when(trainerRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainer));

        TrainerResponseDto response = trainerService.getTrainerProfile();

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
    }

    @Test
    void getTrainerProfile_TrainerHasNoSpecialization_ShouldThrowException() {
        trainer.setSpecialization(null);
        setupAuthenticatedUser("johndoe");
        when(trainerRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainer));

        assertThrows(NullPointerException.class, () -> trainerService.getTrainerProfile());
    }

    @Test
    void getTrainerProfile_TrainerNotFound_ShouldThrowUnauthorizedException() {
        setupAuthenticatedUser("johndoe");
        when(trainerRepository.findByUsername("johndoe")).thenReturn(Optional.empty());

        assertThrows(TrainerNotFoundException.class, () -> trainerService.getTrainerProfile());
    }

    @Test
    void updateTrainerProfile_NoChanges_ShouldReturnSameTrainer() {
        setupAuthenticatedUser("johndoe");
        when(validator.validate(trainerRequestDto)).thenReturn(Set.of());
        when(trainerRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findById(1L)).thenReturn(Optional.of(trainer.getSpecialization()));
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(i -> i.getArgument(0));

        TrainerResponseDto response = trainerService.updateTrainerProfile(trainerRequestDto);

        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
    }

    @Test
    void getAuthenticatedUsername_TrainerHasNoPassword_ShouldThrowUnauthorizedException() {
        setupAuthenticatedUser("johndoe");
        trainer.setPassword(null);
        when(trainerRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainer));

        TrainerResponseDto response = trainerService.getTrainerProfile();
        assertNotNull(response);
    }

    @Test
    void getTrainerProfile_TrainerHasNoTrainees_ShouldNotThrowException() {
        trainer.setTrainees(null);
        setupAuthenticatedUser("johndoe");
        when(trainerRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainer));

        assertDoesNotThrow(() -> trainerService.getTrainerProfile());
    }

    @Test
    void changeTrainerStatus_Success() {
        setupAuthenticatedUser("johndoe");
        when(trainerRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(i -> i.getArgument(0));

        boolean previousStatus = trainer.getIsActive();
        boolean result = trainerService.changeTrainerStatus();

        assertNotEquals(previousStatus, result);
        assertEquals(!previousStatus, trainer.getIsActive());
        verify(trainerRepository).save(trainer);
    }

    @Test
    void changeTrainerStatus_Unauthorized_ShouldThrowException() {
        assertThrows(UnauthorizedException.class, () -> trainerService.changeTrainerStatus());
    }

    @Test
    void changeTrainerStatus_ToggleTwice_ShouldRestoreOriginalState() {
        setupAuthenticatedUser("johndoe");
        when(trainerRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(i -> i.getArgument(0));

        boolean initialStatus = trainer.getIsActive();

        boolean firstToggle = trainerService.changeTrainerStatus();
        assertNotEquals(initialStatus, firstToggle);
        assertEquals(!initialStatus, trainer.getIsActive());

        boolean secondToggle = trainerService.changeTrainerStatus();
        assertEquals(initialStatus, secondToggle);
        assertEquals(initialStatus, trainer.getIsActive());

        verify(trainerRepository, times(2)).save(trainer);
    }

    @Test
    void updateTrainerProfile_Success() {
        setupAuthenticatedUser("johndoe");
        TrainerRequestDto updateRequest = new TrainerRequestDto("Jane", "Smith", 2L, false);
        TrainingType newSpecialization = new TrainingType();
        newSpecialization.setId(2L);
        when(validator.validate(updateRequest)).thenReturn(Set.of());
        when(trainerRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findById(2L)).thenReturn(Optional.of(newSpecialization));
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(i -> i.getArgument(0));

        TrainerResponseDto response = trainerService.updateTrainerProfile(updateRequest);

        assertNotNull(response);
        assertEquals("Jane", response.getFirstName());
        assertEquals("Smith", response.getLastName());
        assertEquals(2L, response.getSpecializationId());
        assertFalse(response.getIsActive());
    }

    @Test
    void validateTrainerRequest_InvalidRequest_ShouldThrowException() {
        TrainerRequestDto invalidRequest = new TrainerRequestDto("", "", null, null);
        when(validator.validate(invalidRequest)).thenThrow(InvalidTrainerRequestException.class);

        assertThrows(InvalidTrainerRequestException.class, () -> trainerService.createTrainer(invalidRequest));
    }

    @Test
    void getTrainerProfile_InvalidAuthHeader_ShouldThrowUnauthorizedException() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(null);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(UnauthorizedException.class, () -> trainerService.getTrainerProfile());
    }

    @Test
    void getTrainerProfile_NoAuthHeader_ShouldThrowUnauthorizedException() {
        assertThrows(UnauthorizedException.class, () -> trainerService.getTrainerProfile());
    }

    @Test
    void changeTrainerStatus_ShouldToggleStatus() {
        setupAuthenticatedUser("johndoe");
        when(trainerRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(i -> i.getArgument(0));

        boolean initialStatus = trainer.getIsActive();
        boolean newStatus = trainerService.changeTrainerStatus();

        assertNotEquals(initialStatus, newStatus);
        assertEquals(!initialStatus, trainer.getIsActive());
    }

    @Test
    void mapToResponseWithTraineesUsername_ShouldMapCorrectly() throws Exception {
        Method method = TrainerServiceImpl.class.getDeclaredMethod("mapToResponseWithTraineesUsername", Trainer.class);
        method.setAccessible(true);

        TrainerResponseDto response = (TrainerResponseDto) method.invoke(trainerService, trainer);
        assertNotNull(response);
        assertEquals(trainer.getUsername(), response.getUsername());
    }

    @Test
    void getAuthenticatedUsername_InvalidFormat_ShouldThrowException() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(UnauthorizedException.class, () -> trainerService.getTrainerProfile());
    }

    @Test
    void validateRequest_WithInvalidRequest_ShouldThrowException() {
        TrainerRequestDto invalidRequest = new TrainerRequestDto("", "", null, null);
        doThrow(new InvalidTrainerRequestException("Validation failed", Set.of()))
                .when(validator).validate(any(TrainerRequestDto.class));
        assertThrows(InvalidTrainerRequestException.class, () -> trainerService.createTrainer(invalidRequest));
    }

    @Test
    void getAuthenticatedUsername_WrongPassword_ShouldThrowException() {
        setupAuthenticatedUser("johndoe");
        when(trainerRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainer));

        TrainerResponseDto response = trainerService.getTrainerProfile();
        assertNotNull(response);
    }

    @Test
    void changeTrainerPassword_Success() {
        setupAuthenticatedUser("johndoe");
        when(trainerRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainer));
        when(passwordEncoder.encode("newPass123")).thenReturn("encodedNewPass123");
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(i -> i.getArgument(0));

        boolean result = trainerService.changeTrainerPassword("newPass123");

        assertTrue(result);
        assertEquals("encodedNewPass123", trainer.getPassword());
    }

    @Test
    void changeTrainerPassword_NullPassword_ShouldThrowInvalidPasswordException() {
        setupAuthenticatedUser("johndoe");

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () -> trainerService.changeTrainerPassword(null));
        assertEquals("New password cannot be empty.", exception.getMessage());
    }

    @Test
    void changeTrainerPassword_EmptyPassword_ShouldThrowInvalidPasswordException() {
        setupAuthenticatedUser("johndoe");

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () -> trainerService.changeTrainerPassword(""));
        assertEquals("New password cannot be empty.", exception.getMessage());
    }

    @Test
    void changeTrainerPassword_IncorrectLength_ShouldThrowInvalidPasswordException() {
        setupAuthenticatedUser("johndoe");

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () -> trainerService.changeTrainerPassword("short"));
        assertEquals("Password must be exactly 10 characters long.", exception.getMessage());
    }

    @Test
    void changeTrainerPassword_TrainerNotFound_ShouldThrowTrainerNotFoundException() {
        setupAuthenticatedUser("johndoe");
        when(trainerRepository.findByUsername("johndoe")).thenReturn(Optional.empty());

        assertThrows(TrainerNotFoundException.class, () -> trainerService.changeTrainerPassword("newPass123"));
    }

    @Test
    void changeTrainerPassword_Unauthorized_ShouldThrowUnauthorizedException() {
        assertThrows(UnauthorizedException.class, () -> trainerService.changeTrainerPassword("newPass123"));
    }

    @Test
    void updateTrainerProfile_SpecializationNotFound_ShouldThrowException() {
        setupAuthenticatedUser("johndoe");
        TrainerRequestDto updateRequest = new TrainerRequestDto("Jane", "Smith", 2L, false);
        when(validator.validate(updateRequest)).thenReturn(Set.of());
        when(trainerRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(SpecializationNotFoundException.class, () -> trainerService.updateTrainerProfile(updateRequest));
    }

    @Test
    void changeTrainerStatus_TrainerNotFound_ShouldThrowTrainerNotFoundException() {
        setupAuthenticatedUser("johndoe");
        when(trainerRepository.findByUsername("johndoe")).thenReturn(Optional.empty());

        assertThrows(TrainerNotFoundException.class, () -> trainerService.changeTrainerStatus());
    }
}