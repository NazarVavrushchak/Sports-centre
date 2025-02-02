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
import java.util.Set;
import java.util.stream.Collectors;

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

        Trainer existingTrainer1 = new Trainer();
        existingTrainer1.setUsername("john.doe");
        Trainer existingTrainer2 = new Trainer();
        existingTrainer2.setUsername("jane.doe");

        when(trainerDao.findAll()).thenReturn(List.of(existingTrainer1, existingTrainer2));

        Set<String> usernames = trainerDao.findAll().stream()
                .map(Trainer::getUsername)
                .collect(Collectors.toSet());

        when(UsernameUtil.generateUsername(anyString(), anyString(), eq(usernames))).thenReturn("jane.smith");
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
    void delete() {
        long trainerId = 1L;

        trainerService.delete(trainerId);

        verify(trainerDao, times(1)).delete(trainerId);
    }
}