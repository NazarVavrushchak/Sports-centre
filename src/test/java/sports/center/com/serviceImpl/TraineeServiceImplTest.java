package sports.center.com.serviceImpl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import sports.center.com.dao.GenericDao;
import sports.center.com.model.Trainee;
import sports.center.com.service.impl.TraineeServiceImpl;
import sports.center.com.util.PasswordUtil;
import sports.center.com.util.UsernameUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TraineeServiceImplTest {
    @Mock
    private GenericDao<Trainee> traineeDao;

    @InjectMocks
    private TraineeServiceImpl traineeService;

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
        Trainee trainee = new Trainee("John", "Doe", "john.doe", "sss", true, LocalDate.of(1990, 1, 1), "123 Main St");

        Mockito.mockStatic(UsernameUtil.class);
        Mockito.mockStatic(PasswordUtil.class);

        when(UsernameUtil.generateUsername(eq("John"), eq("Doe"), any())).thenReturn("john.doe");
        when(PasswordUtil.generatePassword()).thenReturn("securePassword");

        traineeService.create(trainee);

        ArgumentCaptor<Trainee> traineeCaptor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeDao).create(traineeCaptor.capture());

        Trainee capturedTrainee = traineeCaptor.getValue();
        assertEquals("john.doe", capturedTrainee.getUsername());
        assertEquals("securePassword", capturedTrainee.getPassword());
    }

    @Test
    void update() {
        when(traineeDao.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                traineeService.updateTrainee(1L, new Trainee()));

        assertEquals("Trainee not found with ID: 1", exception.getMessage());
    }

    @Test
    void updateTrainee_NotFound() {
        when(traineeDao.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                traineeService.updateTrainee(1L, new Trainee()));

        assertEquals("Trainee not found with ID: 1", exception.getMessage());
    }

    @Test
    void deleteTrainee() {
        traineeService.deleteTrainee(1L);

        verify(traineeDao).delete(1L);
    }

    @Test
    void getById() {
        Trainee trainee = new Trainee("John", "Doe", "john.doe", "password", true, LocalDate.of(1990, 1, 1), "123 Main St");

        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = traineeService.getById(1L);

        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
    }

    @Test
    void getAll() {
        Trainee trainee1 = new Trainee("John", "Doe", "john.doe", "password", true, LocalDate.of(1990, 1, 1), "123 Main St");
        Trainee trainee2 = new Trainee("Jane", "Doe", "jane.doe", "password", true, LocalDate.of(1992, 2, 2), "456 Main St");

        when(traineeDao.findAll()).thenReturn(List.of(trainee1, trainee2));

        List<Trainee> result = traineeService.getAll();

        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Jane", result.get(1).getFirstName());
    }
}