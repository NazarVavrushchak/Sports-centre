package sports.center.com.util.service_impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sports.center.com.service.impl.AuthServiceImpl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private EntityManagerFactory entityManagerFactory;

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Long> query;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
    }

    @Test
    void authenticateTrainee() {
        when(entityManager.createQuery(
                "SELECT COUNT(t) FROM Trainee t WHERE t.username = :username AND t.password = :password", Long.class)
        ).thenReturn(query);
        when(query.setParameter("username", "john.doe")).thenReturn(query);
        when(query.setParameter("password", "password123")).thenReturn(query);
        when(query.getSingleResult()).thenReturn(1L);

        boolean result = authService.authenticateTrainee("john.doe", "password123");

        assertTrue(result);
        verify(entityManager).close();
    }

    @Test
    void authenticateTrainee_Failure() {
        when(entityManager.createQuery(
                "SELECT COUNT(t) FROM Trainee t WHERE t.username = :username AND t.password = :password", Long.class)
        ).thenReturn(query);
        when(query.setParameter("username", "wrong.user")).thenReturn(query);
        when(query.setParameter("password", "wrongPass")).thenReturn(query);
        when(query.getSingleResult()).thenReturn(0L);

        boolean result = authService.authenticateTrainee("wrong.user", "wrongPass");

        assertFalse(result);
        verify(entityManager).close();
    }

    @Test
    void authenticateTrainer() {
        when(entityManager.createQuery(
                "SELECT COUNT(t) FROM Trainer t WHERE t.username = :username AND t.password = :password", Long.class)
        ).thenReturn(query);
        when(query.setParameter("username", "trainer.john")).thenReturn(query);
        when(query.setParameter("password", "trainerPass")).thenReturn(query);
        when(query.getSingleResult()).thenReturn(1L);

        boolean result = authService.authenticateTrainer("trainer.john", "trainerPass");

        assertTrue(result);
        verify(entityManager).close();
    }

    @Test
    void authenticateTrainer_Failure() {
        when(entityManager.createQuery(
                "SELECT COUNT(t) FROM Trainer t WHERE t.username = :username AND t.password = :password", Long.class)
        ).thenReturn(query);
        when(query.setParameter("username", "wrong.trainer")).thenReturn(query);
        when(query.setParameter("password", "wrongPass")).thenReturn(query);
        when(query.getSingleResult()).thenReturn(0L);

        boolean result = authService.authenticateTrainer("wrong.trainer", "wrongPass");

        assertFalse(result);
        verify(entityManager).close();
    }
}
