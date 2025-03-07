package sports.center.com.healthIndicator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CustomAppHealthIndicator implements HealthIndicator {
    private final long startTime;

    public CustomAppHealthIndicator() {
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public Health health() {
        long uptime = (System.currentTimeMillis() - startTime) / 1000;
        return Health.up().withDetail("uptime", uptime + " seconds").build();
    }
}
