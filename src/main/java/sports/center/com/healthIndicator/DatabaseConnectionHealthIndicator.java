package sports.center.com.healthIndicator;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class DatabaseConnectionHealthIndicator implements HealthIndicator {
    private final DataSource dataSource;

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                return Health.up().withDetail("database", "Connected").build();
            } else {
                return Health.down().withDetail("database", "Connection is not valid").build();
            }
        } catch (SQLException e) {
            return Health.down(e).withDetail("database", "Connection failed").build();
        }
    }
}