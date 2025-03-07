package sports.center.com.util.healthIndicator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import sports.center.com.healthIndicator.CustomAppHealthIndicator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomAppHealthIndicatorTest {

    private CustomAppHealthIndicator customAppHealthIndicator;

    @BeforeEach
    void setUp() {
        customAppHealthIndicator = new CustomAppHealthIndicator();
    }

    @Test
    void testHealth_StatusUp() {
        Health health = customAppHealthIndicator.health();

        assertEquals("UP", health.getStatus().getCode());
    }

    @Test
    void testHealth_UptimeIsPositive() throws InterruptedException {
        Thread.sleep(1000);

        Health health = customAppHealthIndicator.health();
        String uptime = (String) health.getDetails().get("uptime");

        assertTrue(uptime.matches("\\d+ seconds"));
        int uptimeValue = Integer.parseInt(uptime.split(" ")[0]);
        assertTrue(uptimeValue > 0);
    }
}
