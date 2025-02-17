package sports.center.com.util.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sports.center.com.model.Trainee;
import sports.center.com.model.Trainer;
import sports.center.com.model.Training;
import sports.center.com.model.TrainingType;
import sports.center.com.repository.TrainingRepositoryImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TrainingRepositoryImplTest {
    @Mock
    private EntityManager entityManager;
    @Mock
    private CriteriaBuilder criteriaBuilder;
    @Mock
    private CriteriaQuery<Training> criteriaQuery;
    @Mock
    private Root<Training> root;
    @Mock
    private TypedQuery<Training> typedQuery;
    @Mock
    private Path<Object> traineePath;
    @Mock
    private Path<Object> trainerPath;
    @Mock
    private Path<Object> trainingTypePath;
    @Mock
    private Path<Date> trainingDatePath;

    @InjectMocks
    private TrainingRepositoryImpl trainingRepository;

    private Training sampleTraining;
    private Date fromDate;
    private Date toDate;

    @BeforeEach
    void setUp() {
        fromDate = new Date(System.currentTimeMillis() - 86400000L);
        toDate = new Date();

        Trainee trainee = new Trainee();
        trainee.setUsername("john.doe");

        Trainer trainer = new Trainer();
        trainer.setUsername("trainer.john");

        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName("Gym");

        sampleTraining = new Training();
        sampleTraining.setTrainee(trainee);
        sampleTraining.setTrainer(trainer);
        sampleTraining.setTrainingType(trainingType);
        sampleTraining.setTrainingDate(toDate);

        doReturn(traineePath).when(root).get("trainee");
        doReturn(mock(Path.class)).when(traineePath).get("username");

        doReturn(trainerPath).when(root).get("trainer");
        doReturn(mock(Path.class)).when(trainerPath).get("username");

        doReturn(trainingTypePath).when(root).get("trainingType");
        doReturn(mock(Path.class)).when(trainingTypePath).get("trainingTypeName");

        doReturn(trainingDatePath).when(root).get("trainingDate");
    }

    @Test
    void findTrainingsByTraineeCriteria_AllFilters() {
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Training.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Training.class)).thenReturn(root);
        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Collections.singletonList(sampleTraining));

        List<Training> result = trainingRepository.findTrainingsByTraineeCriteria(
                "john.doe", fromDate, toDate, "trainer.john", "Gym");

        assertEquals(1, result.size());
        assertEquals("john.doe", result.get(0).getTrainee().getUsername());
        assertEquals("trainer.john", result.get(0).getTrainer().getUsername());
        assertEquals("Gym", result.get(0).getTrainingType().getTrainingTypeName());

        verify(entityManager, times(1)).createQuery(criteriaQuery);
        verify(typedQuery, times(1)).getResultList();
    }

    @Test
    void findTrainingsByTraineeCriteria_OnlyTrainee() {
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Training.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Training.class)).thenReturn(root);
        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(sampleTraining, sampleTraining));

        List<Training> result = trainingRepository.findTrainingsByTraineeCriteria(
                "john.doe", null, null, null, null);

        assertEquals(2, result.size());
        assertEquals("john.doe", result.get(0).getTrainee().getUsername());

        verify(entityManager, times(1)).createQuery(criteriaQuery);
    }

    @Test
    void findTrainingsByTrainerCriteria_AllFilters() {
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Training.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Training.class)).thenReturn(root);
        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Collections.singletonList(sampleTraining));

        List<Training> result = trainingRepository.findTrainingsByTrainerCriteria(
                "trainer.john", fromDate, toDate, "john.doe");

        assertEquals(1, result.size());
        assertEquals("trainer.john", result.get(0).getTrainer().getUsername());
        assertEquals("john.doe", result.get(0).getTrainee().getUsername());

        verify(entityManager, times(1)).createQuery(criteriaQuery);
        verify(typedQuery, times(1)).getResultList();
    }

    @Test
    void findTrainingsByTrainerCriteria_OnlyTrainer() {
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Training.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Training.class)).thenReturn(root);
        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(sampleTraining, sampleTraining));

        List<Training> result = trainingRepository.findTrainingsByTrainerCriteria(
                "trainer.john", null, null, null);

        assertEquals(2, result.size());
        assertEquals("trainer.john", result.get(0).getTrainer().getUsername());

        verify(entityManager, times(1)).createQuery(criteriaQuery);
    }
}