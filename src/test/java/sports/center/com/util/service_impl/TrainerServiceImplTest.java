package sports.center.com.util.service_impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sports.center.com.dto.trainer.TrainerRequestDto;
import sports.center.com.dto.trainer.TrainerResponseDto;
import sports.center.com.exception.exceptions.InvalidTrainerRequestException;
import sports.center.com.exception.exceptions.SpecializationNotFoundException;
import sports.center.com.exception.exceptions.UnauthorizedException;
import sports.center.com.model.Trainer;
import sports.center.com.model.TrainingType;
import sports.center.com.repository.TrainerRepository;
import sports.center.com.repository.TrainingTypeRepository;
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
    private HttpServletRequest request;

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

        when(usernameUtil.generateUsername(any(), any())).thenReturn("johndoe");
        when(trainingTypeRepository.findById(1L)).thenReturn(Optional.of(trainingType));
        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);

        TrainerResponseDto response = trainerService.createTrainer(trainerRequestDto);

        assertNotNull(response);
        assertEquals("johndoe", response.getUsername());
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
        when(request.getHeader("Authorization")).thenReturn("Basic am9obmRvZTpwYXNzd29yZDEyMw==");
        when(trainerRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainer));

        TrainerResponseDto response = trainerService.getTrainerProfile();

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
    }

    @Test
    void getTrainerProfile_TrainerHasNoSpecialization_ShouldThrowException() {
        trainer.setSpecialization(null);

        when(request.getHeader("Authorization")).thenReturn("Basic am9obmRvZTpwYXNzd29yZDEyMw==");
        when(trainerRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainer));

        assertThrows(NullPointerException.class, () -> trainerService.getTrainerProfile());
    }

    @Test
    void getTrainerProfile_TrainerNotFound_ShouldThrowUnauthorizedException() {
        when(request.getHeader("Authorization")).thenReturn("Basic am9obmRvZTpwYXNzd29yZDEyMw==");
        when(trainerRepository.findByUsername("johndoe")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> trainerService.getTrainerProfile());
    }

    @Test
    void updateTrainerProfile_NoChanges_ShouldReturnSameTrainer() {
        when(request.getHeader("Authorization")).thenReturn("Basic am9obmRvZTpwYXNzd29yZDEyMw==");
        when(trainerRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);

        TrainerResponseDto response = trainerService.updateTrainerProfile(trainerRequestDto);

        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
    }

    @Test
    void getAuthenticatedUsername_TrainerHasNoPassword_ShouldThrowUnauthorizedException() {
        trainer.setPassword(null);

        when(request.getHeader("Authorization")).thenReturn("Basic am9obmRvZTpwYXNzd29yZDEyMw==");
        when(trainerRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainer));

        assertThrows(UnauthorizedException.class, () -> trainerService.getTrainerProfile());
    }

    @Test
    void getTrainerProfile_TrainerHasNoTrainees_ShouldNotThrowException() {
        trainer.setTrainees(null);

        when(request.getHeader("Authorization")).thenReturn("Basic am9obmRvZTpwYXNzd29yZDEyMw==");
        when(trainerRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainer));

        assertDoesNotThrow(() -> trainerService.getTrainerProfile());
    }

    @Test
    void changeTrainerStatus_Success() {
        when(request.getHeader("Authorization")).thenReturn("Basic am9obmRvZTpwYXNzd29yZDEyMw==");
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
        when(request.getHeader("Authorization")).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> trainerService.changeTrainerStatus());
    }

    @Test
    void changeTrainerStatus_ToggleTwice_ShouldRestoreOriginalState() {
        when(request.getHeader("Authorization")).thenReturn("Basic am9obmRvZTpwYXNzd29yZDEyMw==");
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
        when(request.getHeader("Authorization")).thenReturn("Basic am9obmRvZTpwYXNzd29yZDEyMw==");
        when(trainerRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);

        TrainerResponseDto response = trainerService.updateTrainerProfile(trainerRequestDto);

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
    }

    @Test
    void validateTrainerRequest_InvalidRequest_ShouldThrowException() {
        TrainerRequestDto invalidRequest = new TrainerRequestDto("", "", null, null);
        when(validator.validate(invalidRequest)).thenThrow(InvalidTrainerRequestException.class);

        assertThrows(InvalidTrainerRequestException.class, () -> trainerService.createTrainer(invalidRequest));
    }

    @Test
    void getTrainerProfile_InvalidAuthHeader_ShouldThrowUnauthorizedException() {
        when(request.getHeader("Authorization")).thenReturn("Bearer sometoken");

        assertThrows(UnauthorizedException.class, () -> trainerService.getTrainerProfile());
    }

    @Test
    void getTrainerProfile_NoAuthHeader_ShouldThrowUnauthorizedException() {
        when(request.getHeader("Authorization")).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> trainerService.getTrainerProfile());
    }

    @Test
    void changeTrainerStatus_ShouldToggleStatus() {
        when(request.getHeader("Authorization")).thenReturn("Basic am9obmRvZTpwYXNzd29yZDEyMw==");
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
    void getTrainerProfile_Unauthorized_ShouldThrowException() {
        when(request.getHeader("Authorization")).thenReturn(null);
        assertThrows(UnauthorizedException.class, () -> trainerService.getTrainerProfile());
    }

    @Test
    void getAuthenticatedUsername_InvalidFormat_ShouldThrowException() {
        when(request.getHeader("Authorization")).thenReturn("InvalidFormatToken");
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
        trainer.setPassword("correctPassword");
        when(request.getHeader("Authorization")).thenReturn("Basic am9obmRvZTp3cm9uZ3Bhc3N3b3Jk");
        when(trainerRepository.findByUsername("johndoe")).thenReturn(Optional.of(trainer));
        assertThrows(UnauthorizedException.class, () -> trainerService.getTrainerProfile());
    }
}