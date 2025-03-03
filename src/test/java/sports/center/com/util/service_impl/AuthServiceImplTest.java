package sports.center.com.util.service_impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sports.center.com.service.impl.AuthServiceImpl;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private EntityManagerFactory entityManagerFactory;

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Long> query;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        lenient().when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
        lenient().when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(query);
        lenient().when(query.setParameter(anyString(), any())).thenReturn(query);
    }

    @Test
    void authenticateTrainee_Success() {
        when(query.getSingleResult()).thenReturn(1L);

        boolean result = authService.authenticateTrainee("john.doe", "password123");

        assertTrue(result);
        verify(entityManager).close();
    }

    @Test
    void authenticateTrainee_Failure() {
        when(query.getSingleResult()).thenReturn(0L);

        boolean result = authService.authenticateTrainee("wrong.user", "wrongPass");

        assertFalse(result);
        verify(entityManager).close();
    }

    @Test
    void authenticateTrainer_Success() {
        when(query.getSingleResult()).thenReturn(1L);

        boolean result = authService.authenticateTrainer("trainer.john", "trainerPass");

        assertTrue(result);
        verify(entityManager).close();
    }

    @Test
    void authenticateTrainer_Failure() {
        when(query.getSingleResult()).thenReturn(0L);

        boolean result = authService.authenticateTrainer("wrong.trainer", "wrongPass");

        assertFalse(result);
        verify(entityManager).close();
    }

    @Test
    void authenticateRequest_InvalidAuthFormat_ShouldReturnFalse() {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");

        assertFalse(authService.authenticateRequest(request));
    }

    @Test
    void authenticateRequest_InvalidCredentialFormat_ShouldReturnFalse() {
        String encoded = Base64.getEncoder().encodeToString("invalidFormat".getBytes());
        when(request.getHeader("Authorization")).thenReturn("Basic " + encoded);

        assertFalse(authService.authenticateRequest(request));
    }

    @Test
    void authenticateRequest_ValidTrainee_ShouldReturnTrue() {
        String encoded = Base64.getEncoder().encodeToString("john.doe:password123".getBytes());
        when(request.getHeader("Authorization")).thenReturn("Basic " + encoded);
        when(query.getSingleResult()).thenReturn(1L);

        assertTrue(authService.authenticateRequest(request));
    }

    @Test
    void authenticateRequest_ValidTrainer_ShouldReturnTrue() {
        String encoded = Base64.getEncoder().encodeToString("trainer.john:trainerPass".getBytes());
        when(request.getHeader("Authorization")).thenReturn("Basic " + encoded);
        when(query.getSingleResult()).thenReturn(1L);

        assertTrue(authService.authenticateRequest(request));
    }

    @Test
    void authenticateRequest_WrongCredentials_ShouldReturnFalse() {
        String encoded = Base64.getEncoder().encodeToString("wrong.user:wrongPass".getBytes());
        when(request.getHeader("Authorization")).thenReturn("Basic " + encoded);
        when(query.getSingleResult()).thenReturn(0L);

        assertFalse(authService.authenticateRequest(request));
    }
}
