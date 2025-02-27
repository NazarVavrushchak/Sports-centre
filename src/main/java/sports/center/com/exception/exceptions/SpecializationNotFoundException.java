package sports.center.com.exception.exceptions;

public class SpecializationNotFoundException extends RuntimeException {
    public SpecializationNotFoundException(Long specializationId) {
        super("Specialization not found with ID: " + specializationId);
    }
}