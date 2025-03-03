package sports.center.com.exception.exceptions;

import jakarta.validation.ConstraintViolation;

import java.util.Set;

public class InvalidTrainerRequestException extends BaseValidationException {
    public InvalidTrainerRequestException(String message, Set<ConstraintViolation<?>> violations) {
        super(message, violations);
    }
}