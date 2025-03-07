package sports.center.com.healthIndicator;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import sports.center.com.repository.TrainingTypeRepository;

@Component
@RequiredArgsConstructor
public class TrainingTypeHealthIndicator implements HealthIndicator {
    private final TrainingTypeRepository trainingTypeRepository;

    @Override
    public Health health() {
        long count = trainingTypeRepository.count();
        if (count > 0) {
            return Health.up().withDetail("trainingTypes", "Available").build();
        } else {
            return Health.down().withDetail("trainingTypes", "Not Found").build();
        }
    }
}