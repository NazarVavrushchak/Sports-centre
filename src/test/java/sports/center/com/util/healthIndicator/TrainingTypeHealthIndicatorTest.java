package sports.center.com.util.healthIndicator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.actuate.health.Health;
import sports.center.com.healthIndicator.TrainingTypeHealthIndicator;
import sports.center.com.repository.TrainingTypeRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrainingTypeHealthIndicatorTest {

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    private TrainingTypeHealthIndicator trainingTypeHealthIndicator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        trainingTypeHealthIndicator = new TrainingTypeHealthIndicator(trainingTypeRepository);
    }

    @Test
    void testHealth_TrainingTypesAvailable() {
        when(trainingTypeRepository.count()).thenReturn(5L);

        Health health = trainingTypeHealthIndicator.health();

        assertEquals("UP", health.getStatus().getCode());
        assertEquals("Available", health.getDetails().get("trainingTypes"));

        verify(trainingTypeRepository).count();
    }

    @Test
    void testHealth_TrainingTypesNotFound() {
        when(trainingTypeRepository.count()).thenReturn(0L);

        Health health = trainingTypeHealthIndicator.health();

        assertEquals("DOWN", health.getStatus().getCode());
        assertEquals("Not Found", health.getDetails().get("trainingTypes"));

        verify(trainingTypeRepository).count();
    }
}