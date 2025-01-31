package sports.center.com.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sports.center.com.model.Trainer;
import sports.center.com.storage.InMemoryStorage;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TrainerDaoTest {

    private TrainerDao trainerDao;
    private InMemoryStorage fakeStorage;

    @BeforeEach
    void setUp() {
        fakeStorage = new InMemoryStorage();
        trainerDao = new TrainerDao(fakeStorage);
    }

    @Test
    void create() {
        Trainer trainer = new Trainer("John", "Doe", "john.doe"
                , "password", true, "Fitness");

        trainerDao.create(trainer);

        assertNotNull(trainer.getId());
        assertEquals(1L, trainer.getId());

        Optional<Trainer> storedTrainer = fakeStorage.getNamespace("trainer").values().stream()
                .map(obj -> (Trainer) obj)
                .findFirst();

        assertTrue(storedTrainer.isPresent());
        assertEquals("John", storedTrainer.get().getFirstName());
        assertEquals("Fitness", storedTrainer.get().getSpecialization());
    }

    @Test
    void update() {
        Trainer trainer = new Trainer("John", "Doe", "john.doe", "password", true, "Fitness");
        trainerDao.create(trainer);

        trainer.setSpecialization("Yoga");
        trainerDao.update(trainer.getId(), trainer);

        Optional<Trainer> updatedTrainer = trainerDao.findById(trainer.getId());
        assertTrue(updatedTrainer.isPresent());
        assertEquals("Yoga", updatedTrainer.get().getSpecialization());
    }

    @Test
    void delete() {
        Trainer trainer = new Trainer("John", "Doe", "john.doe"
                , "password", true, "Fitness");
        trainerDao.create(trainer);

        trainerDao.delete(trainer.getId());

        Optional<Trainer> deletedTrainer = trainerDao.findById(trainer.getId());
        assertFalse(deletedTrainer.isPresent());
    }

    @Test
    void findById() {
        Trainer trainer = new Trainer("John", "Doe", "john.doe", "password", true, "Fitness");
        trainerDao.create(trainer);

        Optional<Trainer> foundTrainer = trainerDao.findById(trainer.getId());
        assertTrue(foundTrainer.isPresent());
        assertEquals("John", foundTrainer.get().getFirstName());
    }

    @Test
    void findAll() {
        Trainer trainer1 = new Trainer("John", "Doe", "john.doe"
                , "password", true, "Fitness");
        Trainer trainer2 = new Trainer("Jane", "Smith"
                , "jane.smith", "password", true, "Yoga");

        trainerDao.create(trainer1);
        trainerDao.create(trainer2);

        List<Trainer> trainers = trainerDao.findAll();
        assertEquals(2, trainers.size());
        assertTrue(trainers.stream().anyMatch(t -> t.getFirstName().equals("John")));
        assertTrue(trainers.stream().anyMatch(t -> t.getFirstName().equals("Jane")));
    }
}