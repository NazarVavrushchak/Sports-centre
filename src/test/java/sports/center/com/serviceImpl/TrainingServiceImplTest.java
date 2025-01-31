package sports.center.com.serviceImpl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import sports.center.com.dao.GenericDao;
import sports.center.com.model.Training;
import sports.center.com.service.impl.TrainingServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class TrainingServiceImplTest {
    @Mock
    private GenericDao<Training> trainingDao;

    @InjectMocks
    private TrainingServiceImpl trainingService;

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
        Training training = new Training();
        training.setId(1);

        doNothing().when(trainingDao).create(training);

        trainingService.create(training);

        verify(trainingDao).create(training);
    }

    @Test
    void getTrainingById() {
        long trainingId = 1L;
        Training training = new Training();
        training.setId(trainingId);
        when(trainingDao.findById(trainingId)).thenReturn(Optional.of(training));

        Optional<Training> result = trainingService.getById(trainingId);

        assertTrue(result.isPresent());
        assertEquals(trainingId, result.get().getId());
        verify(trainingDao).findById(trainingId);
    }

    @Test
    void getAllTrainings() {
        Training training1 = new Training();
        Training training2 = new Training();
        when(trainingDao.findAll()).thenReturn(List.of(training1, training2));

        List<Training> result = trainingService.getAll();

        assertEquals(2, result.size());
        verify(trainingDao).findAll();
    }
}
