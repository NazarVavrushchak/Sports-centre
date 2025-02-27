package sports.center.com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sports.center.com.model.TrainingType;

import java.util.Optional;

public interface TrainingTypeRepository extends JpaRepository<TrainingType, Long> {
    Optional<TrainingType> findByTrainingTypeName(String trainingTypeName);
}
