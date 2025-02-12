package sports.center.com.util.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sports.center.com.model.Trainee;
import sports.center.com.model.Trainer;
import sports.center.com.repository.TrainerRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerRepositoryTest {

    @Mock
    private TrainerRepository trainerRepository;

    private Trainer trainer1, trainer2;
    private Trainee trainee;

    @BeforeEach
    void setUp() {
        trainer1 = new Trainer();
        trainer1.setId(1L);
        trainer1.setUsername("trainer.one");

        trainer2 = new Trainer();
        trainer2.setId(2L);
        trainer2.setUsername("trainer.two");

        trainee = new Trainee();
        trainee.setId(100L);
        trainee.setUsername("trainee.john");
    }

    @Test
    void findByUsername() {
        when(trainerRepository.findByUsername("trainer.one")).thenReturn(Optional.of(trainer1));

        Optional<Trainer> foundTrainer = trainerRepository.findByUsername("trainer.one");

        assertTrue(foundTrainer.isPresent());
        assertEquals("trainer.one", foundTrainer.get().getUsername());

        verify(trainerRepository, times(1)).findByUsername("trainer.one");
    }

    @Test
    void findByUsername_NotFound() {
        when(trainerRepository.findByUsername("trainer.unknown")).thenReturn(Optional.empty());

        Optional<Trainer> foundTrainer = trainerRepository.findByUsername("trainer.unknown");

        assertFalse(foundTrainer.isPresent());

        verify(trainerRepository, times(1)).findByUsername("trainer.unknown");
    }

    @Test
    void findTrainersByTraineeUsername() {
        when(trainerRepository.findTrainersByTraineeUsername("trainee.john"))
                .thenReturn(Arrays.asList(trainer1, trainer2));

        List<Trainer> trainers = trainerRepository.findTrainersByTraineeUsername("trainee.john");

        assertEquals(2, trainers.size());
        assertEquals("trainer.one", trainers.get(0).getUsername());
        assertEquals("trainer.two", trainers.get(1).getUsername());

        verify(trainerRepository, times(1)).findTrainersByTraineeUsername("trainee.john");
    }

    @Test
    void findTrainersByTraineeUsername_Empty() {
        when(trainerRepository.findTrainersByTraineeUsername("trainee.unknown"))
                .thenReturn(List.of());

        List<Trainer> trainers = trainerRepository.findTrainersByTraineeUsername("trainee.unknown");

        assertTrue(trainers.isEmpty());

        verify(trainerRepository, times(1)).findTrainersByTraineeUsername("trainee.unknown");
    }

    @Test
    void findUnassignedTrainers() {
        when(trainerRepository.findUnassignedTrainers("trainee.john"))
                .thenReturn(List.of(trainer1));

        List<Trainer> unassignedTrainers = trainerRepository.findUnassignedTrainers("trainee.john");

        assertEquals(1, unassignedTrainers.size());
        assertEquals("trainer.one", unassignedTrainers.get(0).getUsername());

        verify(trainerRepository, times(1)).findUnassignedTrainers("trainee.john");
    }

    @Test
    void findUnassignedTrainers_Empty() {
        when(trainerRepository.findUnassignedTrainers("trainee.unknown"))
                .thenReturn(List.of());

        List<Trainer> unassignedTrainers = trainerRepository.findUnassignedTrainers("trainee.unknown");

        assertTrue(unassignedTrainers.isEmpty());

        verify(trainerRepository, times(1)).findUnassignedTrainers("trainee.unknown");
    }

    @Test
    void findByUsernameIn() {
        List<String> usernames = Arrays.asList("trainer.one", "trainer.two");

        when(trainerRepository.findByUsernameIn(usernames)).thenReturn(Arrays.asList(trainer1, trainer2));

        List<Trainer> foundTrainers = trainerRepository.findByUsernameIn(usernames);

        assertEquals(2, foundTrainers.size());
        assertEquals("trainer.one", foundTrainers.get(0).getUsername());
        assertEquals("trainer.two", foundTrainers.get(1).getUsername());

        verify(trainerRepository, times(1)).findByUsernameIn(usernames);
    }

    @Test
    void findByUsernameIn_Empty() {
        List<String> usernames = List.of("trainer.unknown");

        when(trainerRepository.findByUsernameIn(usernames)).thenReturn(List.of());

        List<Trainer> foundTrainers = trainerRepository.findByUsernameIn(usernames);

        assertTrue(foundTrainers.isEmpty());

        verify(trainerRepository, times(1)).findByUsernameIn(usernames);
    }
}