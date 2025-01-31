package sports.center.com.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sports.center.com.enumeration.TrainingType;
import sports.center.com.model.Training;
import sports.center.com.storage.InMemoryStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TrainingDaoTest {

    private TrainingDao trainingDao;
    private InMemoryStorage storage;

    @BeforeEach
    void setUp() {
        storage = new InMemoryStorage();
        trainingDao = new TrainingDao(storage);
    }

    @Test
    void createTraining() {
        Training training = new Training();
        training.setTraineeId(1);
        training.setTrainerId(1);
        training.setTrainingName("Yoga");
        training.setTrainingType(TrainingType.YOGA);
        training.setTrainingDate(LocalDate.now());
        training.setTrainingDuration(60);

        trainingDao.create(training);

        Optional<Training> retrievedTraining = trainingDao.findById(training.getId());
        assertTrue(retrievedTraining.isPresent(), "Training should be created and found");
        assertEquals("Yoga", retrievedTraining.get().getTrainingName(), "Training name should match");
    }

    @Test
    void findById() {
        Training training = new Training();
        training.setTraineeId(2);
        training.setTrainerId(2);
        training.setTrainingName("Cardio");
        training.setTrainingType(TrainingType.CARDIO);
        training.setTrainingDate(LocalDate.now());
        training.setTrainingDuration(30);

        trainingDao.create(training);

        Optional<Training> foundTraining = trainingDao.findById(training.getId());
        assertTrue(foundTraining.isPresent(), "Training should be found");
        assertEquals("Cardio", foundTraining.get().getTrainingName(), "Training name should match");
    }

    @Test
    void updateTraining() {
        Training training = new Training();
        training.setTraineeId(3);
        training.setTrainerId(3);
        training.setTrainingName("Strength");
        training.setTrainingType(TrainingType.STRENGTH);
        training.setTrainingDate(LocalDate.now());
        training.setTrainingDuration(45);

        trainingDao.create(training);

        training.setTrainingName("Advanced Strength");
        training.setTrainingDuration(90);
        trainingDao.update(training.getId(), training);

        Optional<Training> updatedTraining = trainingDao.findById(training.getId());
        assertTrue(updatedTraining.isPresent(), "Updated training should be found");
        assertEquals("Advanced Strength", updatedTraining.get().getTrainingName(), "Training name should be updated");
        assertEquals(90, updatedTraining.get().getTrainingDuration(), "Training duration should be updated");
    }

    @Test
    void deleteTraining() {
        Training training = new Training();
        training.setTraineeId(4);
        training.setTrainerId(4);
        training.setTrainingName("Crossfit");
        training.setTrainingType(TrainingType.CROSSFIT);
        training.setTrainingDate(LocalDate.now());
        training.setTrainingDuration(40);

        trainingDao.create(training);

        trainingDao.delete(training.getId());

        Optional<Training> deletedTraining = trainingDao.findById(training.getId());
        assertFalse(deletedTraining.isPresent(), "Deleted training should not be found");
    }
}