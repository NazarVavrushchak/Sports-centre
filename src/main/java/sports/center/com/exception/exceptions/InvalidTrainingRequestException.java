package sports.center.com.exception.exceptions;

import jakarta.validation.ConstraintViolation;

import java.util.Set;

public class InvalidTrainingRequestException extends BaseValidationException {
    public InvalidTrainingRequestException(String message, Set<ConstraintViolation<?>> violations) {
        super(message, violations);
    }
}