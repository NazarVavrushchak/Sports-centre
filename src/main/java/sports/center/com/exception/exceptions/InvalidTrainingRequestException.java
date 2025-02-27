package sports.center.com.exception.exceptions;

import jakarta.validation.ConstraintViolation;
import java.util.HashSet;
import java.util.Set;

public class InvalidTrainingRequestException extends RuntimeException {
    private final Set<ConstraintViolation<?>> violations;

    public InvalidTrainingRequestException(String message, Set<ConstraintViolation<?>> violations) {
        super(message);
        this.violations = new HashSet<>(violations);
    }

    public Set<ConstraintViolation<?>> getViolations() {
        return violations;
    }
}