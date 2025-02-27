package sports.center.com.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sports.center.com.exception.exceptions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception e) {
        String transactionId = UUID.randomUUID().toString();
        log.error("[{}] Error occurred: {}", transactionId, e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error. Transaction ID: " + transactionId);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<String> handleSecurityException(SecurityException e) {
        String transactionId = UUID.randomUUID().toString();
        log.warn("[{}] Security exception: {}", transactionId, e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized. Transaction ID: " + transactionId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        log.warn("Validation failed: {}", errors);
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TraineeNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTraineeNotFoundException(TraineeNotFoundException ex) {
        log.warn("Trainee error: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPasswordException(InvalidPasswordException ex) {
        log.warn("Password validation error: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTraineeRequestException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTraineeRequestException(InvalidTraineeRequestException ex) {
        log.warn("Trainee request validation failed: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("errors", ex.getViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList()));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(InvalidTrainerRequestException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTrainerRequestException(InvalidTrainerRequestException ex) {
        log.warn("Trainer request validation failed: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("errors", ex.getViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList()));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTrainingRequestException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTrainingRequestException(InvalidTrainingRequestException ex) {
        log.warn("Training request validation failed: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("errors", ex.getViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList()));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TrainerNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTrainerNotFoundException(TrainerNotFoundException ex) {
        log.warn("Trainer not found error: {}", ex.getMessage());

        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TrainingTypeNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTrainingTypeNotFoundException(TrainingTypeNotFoundException ex) {
        log.warn("Training Type not found error: {}", ex.getMessage());

        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmptyTrainerListException.class)
    public ResponseEntity<Map<String, String>> handleEmptyTrainerListException(EmptyTrainerListException ex) {
        log.warn("Trainer list validation error: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SpecializationNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleSpecializationNotFoundException(SpecializationNotFoundException ex) {
        log.warn("Specialization not found: {}", ex.getMessage());

        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorizedException(UnauthorizedException ex) {
        log.warn("Unauthorized access: {}", ex.getMessage());

        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    private ResponseEntity<Map<String, String>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        return new ResponseEntity<>(errorResponse, status);
    }
}