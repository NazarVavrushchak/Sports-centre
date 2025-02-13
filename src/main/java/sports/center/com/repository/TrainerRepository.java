package sports.center.com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sports.center.com.model.Trainer;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    Optional<Trainer> findByUsername(String username);

    @Query("SELECT t FROM Trainer t JOIN t.trainees tr WHERE tr.username = :traineeUsername")
    List<Trainer> findTrainersByTraineeUsername(@Param("traineeUsername") String traineeUsername);

    @Query("""
                SELECT t FROM Trainer t
                WHERE t.id NOT IN (
                    SELECT assignedTrainers.id FROM Trainee tr
                    JOIN tr.trainers assignedTrainers
                    WHERE tr.username = :traineeUsername
                )
            """)
    List<Trainer> findUnassignedTrainers(@Param("traineeUsername") String traineeUsername);

    List<Trainer> findByUsernameIn(List<String> usernames);
}