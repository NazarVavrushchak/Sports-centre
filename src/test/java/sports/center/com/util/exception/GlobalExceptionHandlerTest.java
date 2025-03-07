package sports.center.com.util.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import sports.center.com.exception.GlobalExceptionHandler;
import sports.center.com.exception.exceptions.BaseValidationException;
import sports.center.com.exception.exceptions.EmptyTrainerListException;
import sports.center.com.exception.exceptions.TraineeNotFoundException;
import sports.center.com.exception.exceptions.UnauthorizedException;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleNotFoundExceptions() {
        TraineeNotFoundException exception = new TraineeNotFoundException("Trainee not found");
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleNotFoundExceptions(exception);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Trainee not found", response.getBody().get("error"));
    }

    @Test
    void shouldHandleBadRequestExceptions() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid input");
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleBadRequestExceptions(exception);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid input", response.getBody().get("error"));
    }

    @Test
    void shouldHandleValidationException() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(mock(org.springframework.validation.BindingResult.class));
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleValidationException(exception);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldHandleInvalidRequestExceptions() {
        BaseValidationException exception = mock(BaseValidationException.class);
        when(exception.getMessage()).thenReturn("Validation error");
        when(exception.getViolations()).thenReturn(Set.of(mock(ConstraintViolation.class)));
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleInvalidRequestExceptions(exception);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation error", response.getBody().get("message"));
    }

    @Test
    void shouldHandleUnauthorizedException() {
        UnauthorizedException exception = new UnauthorizedException("Unauthorized access");
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleUnauthorizedException(exception);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Unauthorized access", response.getBody().get("error"));
    }

    @Test
    void shouldHandleSecurityException() {
        SecurityException exception = new SecurityException("Security breach");
        ResponseEntity<String> response = globalExceptionHandler.handleSecurityException(exception);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void shouldHandleGenericException() {
        Exception exception = new Exception("Unexpected error");
        ResponseEntity<String> response = globalExceptionHandler.handleGenericException(exception);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void shouldHandleConstraintViolationException() {
        ConstraintViolationException exception = new ConstraintViolationException("Constraint violation", Set.of());
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleBadRequestExceptions(exception);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Constraint violation", response.getBody().get("error"));
    }

    @Test
    void shouldHandleEmptyTrainerListException() {
        EmptyTrainerListException exception = new EmptyTrainerListException("No trainers available");
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleBadRequestExceptions(exception);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("No trainers available", response.getBody().get("error"));
    }
}