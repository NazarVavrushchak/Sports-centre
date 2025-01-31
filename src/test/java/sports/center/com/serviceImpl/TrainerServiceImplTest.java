package sports.center.com.serviceImpl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import sports.center.com.dao.GenericDao;
import sports.center.com.model.Trainer;
import sports.center.com.service.impl.TrainerServiceImpl;
import sports.center.com.util.PasswordUtil;
import sports.center.com.util.UsernameUtil;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        Trainer trainer = new Trainer();
        trainer.setFirstName("Jane");
        trainer.setLastName("Smith");

        Mockito.mockStatic(UsernameUtil.class);
        Mockito.mockStatic(PasswordUtil.class);

        when(trainerDao.findAll()).thenReturn(List.of());
        when(UsernameUtil.generateUsername(anyString(), anyString(), eq(trainerDao))).thenReturn("jane.smith");
        when(PasswordUtil.generatePassword()).thenReturn("securePassword123");

        trainerService.create(trainer);

        assertEquals("jane.smith", trainer.getUsername());
        assertEquals("securePassword123", trainer.getPassword());
        verify(trainerDao, times(1)).create(trainer);
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
    void deleteTrainer() {
        long trainerId = 1L;

        trainerService.deleteTrainer(trainerId);

        verify(trainerDao, times(1)).delete(trainerId);
    }
}