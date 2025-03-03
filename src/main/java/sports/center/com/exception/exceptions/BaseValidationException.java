package sports.center.com.exception.exceptions;

import jakarta.validation.ConstraintViolation;
import java.util.Set;

public class BaseValidationException extends RuntimeException {
    private final Set<ConstraintViolation<?>> violations;

    public BaseValidationException(String message, Set<ConstraintViolation<?>> violations) {
        super(message);
        this.violations = violations;
    }

    public Set<ConstraintViolation<?>> getViolations() {
        return violations;
    }
}