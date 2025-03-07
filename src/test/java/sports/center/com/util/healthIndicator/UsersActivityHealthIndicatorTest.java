package sports.center.com.util.healthIndicator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.actuate.health.Health;
import sports.center.com.healthIndicator.UsersActivityHealthIndicator;
import sports.center.com.repository.TraineeRepository;
import sports.center.com.repository.TrainerRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UsersActivityHealthIndicatorTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    private UsersActivityHealthIndicator usersActivityHealthIndicator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        usersActivityHealthIndicator = new UsersActivityHealthIndicator(traineeRepository, trainerRepository);
    }

    @Test
    void testHealth_ActiveUsersPresent() {
        when(traineeRepository.countByIsActiveTrue()).thenReturn(5L);
        when(trainerRepository.countByIsActiveTrue()).thenReturn(3L);

        Health health = usersActivityHealthIndicator.health();

        assertEquals("UP", health.getStatus().getCode());
        assertEquals(5L, health.getDetails().get("activeTrainees"));
        assertEquals(3L, health.getDetails().get("activeTrainers"));

        verify(traineeRepository).countByIsActiveTrue();
        verify(trainerRepository).countByIsActiveTrue();
    }

    @Test
    void testHealth_NoActiveUsers() {
        when(traineeRepository.countByIsActiveTrue()).thenReturn(0L);
        when(trainerRepository.countByIsActiveTrue()).thenReturn(0L);

        Health health = usersActivityHealthIndicator.health();

        assertEquals("DOWN", health.getStatus().getCode());
        assertEquals("No active trainees", health.getDetails().get("activeTrainees"));
        assertEquals("No active trainers", health.getDetails().get("activeTrainers"));

        verify(traineeRepository).countByIsActiveTrue();
        verify(trainerRepository).countByIsActiveTrue();
    }

    @Test
    void testHealth_OnlyTraineesActive() {
        when(traineeRepository.countByIsActiveTrue()).thenReturn(4L);
        when(trainerRepository.countByIsActiveTrue()).thenReturn(0L);

        Health health = usersActivityHealthIndicator.health();

        assertEquals("UP", health.getStatus().getCode());
        assertEquals(4L, health.getDetails().get("activeTrainees"));
        assertEquals(0L, health.getDetails().get("activeTrainers"));

        verify(traineeRepository).countByIsActiveTrue();
        verify(trainerRepository).countByIsActiveTrue();
    }

    @Test
    void testHealth_OnlyTrainersActive() {
        when(traineeRepository.countByIsActiveTrue()).thenReturn(0L);
        when(trainerRepository.countByIsActiveTrue()).thenReturn(7L);

        Health health = usersActivityHealthIndicator.health();

        assertEquals("UP", health.getStatus().getCode());
        assertEquals(0L, health.getDetails().get("activeTrainees"));
        assertEquals(7L, health.getDetails().get("activeTrainers"));

        verify(traineeRepository).countByIsActiveTrue();
        verify(trainerRepository).countByIsActiveTrue();
    }
}