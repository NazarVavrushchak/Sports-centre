package sports.center.com.serviceImpl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import sports.center.com.dao.GenericDao;
import sports.center.com.model.Trainer;
import sports.center.com.service.impl.TrainerServiceImpl;
import sports.center.com.util.PasswordUtil;
import sports.center.com.util.UsernameUtil;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainerServiceImplTest {

    @Mock
    private GenericDao<Trainer> trainerDao;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() {
        Mockito.framework().clearInlineMocks();
    }

    @Test
    void create() {
        Trainer trainer = new Trainer("Jane", "Smith", null, null, true, "Fitness");

        try (MockedStatic<UsernameUtil> mockedUsernameUtil = Mockito.mockStatic(UsernameUtil.class);
             MockedStatic<PasswordUtil> mockedPasswordUtil = Mockito.mockStatic(PasswordUtil.class)) {

            when(UsernameUtil.generateUsername(eq("Jane"), eq("Smith"), any())).thenReturn("jane.smith");
            when(PasswordUtil.generatePassword()).thenReturn("securePassword");

            trainerService.create(trainer);

            ArgumentCaptor<Trainer> trainerCaptor = ArgumentCaptor.forClass(Trainer.class);
            verify(trainerDao).create(trainerCaptor.capture());

            Trainer capturedTrainer = trainerCaptor.getValue();
            assertEquals("jane.smith", capturedTrainer.getUsername());
            assertEquals("securePassword", capturedTrainer.getPassword());
        }
    }

    @Test
    void update() {
        long trainerId = 1L;
        Trainer existingTrainer = new Trainer();
        existingTrainer.setId(trainerId);
        existingTrainer.setFirstName("John");
        existingTrainer.setLastName("Doe");

        Trainer newTrainerData = new Trainer();
        newTrainerData.setFirstName("Jane");
        newTrainerData.setLastName("Smith");

        when(trainerDao.findById(trainerId)).thenReturn(Optional.of(existingTrainer));

        trainerService.update(trainerId, newTrainerData);

        assertEquals("Jane", existingTrainer.getFirstName());
        assertEquals("Smith", existingTrainer.getLastName());
        verify(trainerDao, times(1)).update(eq(trainerId), eq(existingTrainer));
    }

    @Test
    void getById() {
        long trainerId = 1L;
        Trainer trainer = new Trainer();
        trainer.setId(trainerId);

        when(trainerDao.findById(trainerId)).thenReturn(Optional.of(trainer));

        Optional<Trainer> fetchedTrainer = trainerService.getById(trainerId);

        assertTrue(fetchedTrainer.isPresent());
        assertEquals(trainerId, fetchedTrainer.get().getId());
        verify(trainerDao, times(1)).findById(trainerId);
    }

    @Test
    void getAll() {
        Trainer trainer1 = new Trainer();
        Trainer trainer2 = new Trainer();

        when(trainerDao.findAll()).thenReturn(List.of(trainer1, trainer2));

        List<Trainer> trainers = trainerService.getAll();

        assertEquals(2, trainers.size());
        verify(trainerDao, times(1)).findAll();
    }

    @Test
    void delete() {
        long trainerId = 1L;

        trainerService.delete(trainerId);

        verify(trainerDao, times(1)).delete(trainerId);
    }

    @Test
    void deleteNonExistentTrainerThrowsException() {
        long nonexistentId = 999L;
        doThrow(new NoSuchElementException("Trainer not found with ID: " + nonexistentId)).when(trainerDao).delete(nonexistentId);

        Exception exception = assertThrows(NoSuchElementException.class, () -> trainerService.delete(nonexistentId));

        assertEquals("Trainer not found with ID: " + nonexistentId, exception.getMessage());
        verify(trainerDao, times(1)).delete(nonexistentId);
    }
}