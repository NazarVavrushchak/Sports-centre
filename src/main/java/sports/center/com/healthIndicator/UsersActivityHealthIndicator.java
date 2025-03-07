package sports.center.com.healthIndicator;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import sports.center.com.repository.TraineeRepository;
import sports.center.com.repository.TrainerRepository;

@Component
@RequiredArgsConstructor
public class UsersActivityHealthIndicator implements HealthIndicator {
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    @Override
    public Health health() {
        long activeTrainees = traineeRepository.countByIsActiveTrue();
        long activeTrainers = trainerRepository.countByIsActiveTrue();

        if (activeTrainees == 0 && activeTrainers == 0) {
            return Health.down()
                    .withDetail("activeTrainees", "No active trainees")
                    .withDetail("activeTrainers", "No active trainers")
                    .build();
        }

        return Health.up()
                .withDetail("activeTrainees", activeTrainees)
                .withDetail("activeTrainers", activeTrainers)
                .build();
    }
}
