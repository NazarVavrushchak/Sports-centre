package sports.center.com.exception.exceptions;

import jakarta.validation.ConstraintViolation;

import java.util.HashSet;
import java.util.Set;

public class InvalidTraineeRequestException extends RuntimeException {
    private final Set<ConstraintViolation<?>> violations;

    public InvalidTraineeRequestException(String message, Set<ConstraintViolation<?>> violations) {
        super(message);
        this.violations = new HashSet<>(violations);
    }

    public Set<ConstraintViolation<?>> getViolations() {
        return violations;
    }
}