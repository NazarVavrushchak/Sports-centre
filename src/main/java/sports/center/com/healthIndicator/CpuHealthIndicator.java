package sports.center.com.healthIndicator;

import com.sun.management.OperatingSystemMXBean;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;

@Component
public class CpuHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        double cpuLoad = osBean.getSystemCpuLoad() * 100;

        if (cpuLoad < 0) {
            return Health.unknown().withDetail("CPU Load", "Не вдалося отримати значення").build();
        } else if (cpuLoad < 80) {
            return Health.up().withDetail("CPU Load (%)", cpuLoad).build();
        } else {
            return Health.down().withDetail("CPU Load (%)", cpuLoad).build();
        }
    }
}
