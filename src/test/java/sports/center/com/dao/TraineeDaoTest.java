package sports.center.com.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sports.center.com.model.Trainee;
import sports.center.com.storage.InMemoryStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TraineeDaoTest {
    private TraineeDao traineeDao;
    private InMemoryStorage fakeStorage;

    @BeforeEach
    void setUp() {
        fakeStorage = new InMemoryStorage();
        traineeDao = new TraineeDao(fakeStorage);
    }

    @Test
    void create() {
        Trainee trainee = new Trainee("John", "Doe", "john.doe", "ssssssss"
                , true, LocalDate.of(1990, 1, 1), "Shevchenka 370");

        traineeDao.create(trainee);

        assertNotNull(trainee.getId(), "ID should be generated");
        assertEquals(1L, trainee.getId());

        Optional<Trainee> storedTrainee = fakeStorage.getNamespace("trainee").values().stream()
                .map(obj -> (Trainee) obj)
                .findFirst();
        assertTrue(storedTrainee.isPresent());
        assertEquals("John", storedTrainee.get().getFirstName());
    }

    @Test
    void findById() {
        Trainee trainee = new Trainee("John", "Doe", "john.doe", "ssssssss"
                , true, LocalDate.of(1990, 1, 1), "Shevchenka 370");
        fakeStorage.getNamespace("trainee").put(1L, trainee);

        Optional<Trainee> foundTrainee = traineeDao.findById(1L);

        assertTrue(foundTrainee.isPresent());
        assertEquals("John", foundTrainee.get().getFirstName());
    }

    @Test
    void findAll() {
        Trainee trainee1 = new Trainee("John", "Doe", null, null, true, LocalDate.of(1990, 1, 1), "123 Street");
        Trainee trainee2 = new Trainee("Jane", "Smith", null, null, true, LocalDate.of(1992, 5, 15), "456 Avenue");
        fakeStorage.getNamespace("trainee").put(1L, trainee1);
        fakeStorage.getNamespace("trainee").put(2L, trainee2);

        List<Trainee> trainees = traineeDao.findAll();

        assertEquals(2, trainees.size());
        assertTrue(trainees.stream().anyMatch(t -> t.getFirstName().equals("John")));
        assertTrue(trainees.stream().anyMatch(t -> t.getFirstName().equals("Jane")));
    }

    @Test
    void update() {
        Trainee trainee = new Trainee("John", "Doe", "john.doe", "ssssssss"
                , true, LocalDate.of(1990, 1, 1), "Shevchenka 370");
        fakeStorage.getNamespace("trainee").put(1L, trainee);

        Trainee updatedTrainee = new Trainee("John", "Doe", "newUsername"
                , "newPassword", true, LocalDate.of(1990, 1, 1)
                , "789 Boulevard");
        traineeDao.update(1L, updatedTrainee);

        Optional<Trainee> storedTrainee = fakeStorage.getNamespace("trainee").values().stream()
                .map(obj -> (Trainee) obj)
                .findFirst();
        assertTrue(storedTrainee.isPresent());
        assertEquals("newUsername", storedTrainee.get().getUsername());
        assertEquals("789 Boulevard", storedTrainee.get().getAddress());
    }

    @Test
    void delete() {
        Trainee trainee = new Trainee("John", "Doe", "john.doe", "ssssssss"
                , true, LocalDate.of(1990, 1, 1), "Shevchenka 370");;
        fakeStorage.getNamespace("trainee").put(1L, trainee);

        traineeDao.delete(1L);

        assertFalse(fakeStorage.getNamespace("trainee").containsKey(1L));
    }
}