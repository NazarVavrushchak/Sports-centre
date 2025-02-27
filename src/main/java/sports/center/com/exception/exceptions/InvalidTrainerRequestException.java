package sports.center.com.exception.exceptions;

import jakarta.validation.ConstraintViolation;
import java.util.HashSet;
import java.util.Set;

public class InvalidTrainerRequestException extends RuntimeException {
    private final Set<? extends ConstraintViolation<?>> violations;

    public InvalidTrainerRequestException(String message, Set<ConstraintViolation<?>> violations) {
        super(message);
        this.violations = new HashSet<>(violations);
    }

    public Set<? extends ConstraintViolation<?>> getViolations() {
        return violations;
    }
}