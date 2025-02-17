package sports.center.com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sports.center.com.model.TrainingType;

public interface TrainingTypeRepository extends JpaRepository<TrainingType, Long> {
}
