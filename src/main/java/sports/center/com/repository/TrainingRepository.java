package sports.center.com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sports.center.com.model.Training;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long>, TrainingRepositoryCustom {
}
