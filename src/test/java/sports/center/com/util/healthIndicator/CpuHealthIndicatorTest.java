package sports.center.com.util.healthIndicator;

import com.sun.management.OperatingSystemMXBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import sports.center.com.healthIndicator.CpuHealthIndicator;

import java.lang.management.ManagementFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CpuHealthIndicatorTest {

    private CpuHealthIndicator cpuHealthIndicator;

    @BeforeEach
    void setUp() {
        cpuHealthIndicator = new CpuHealthIndicator();
    }

    @Test
    void testHealth_UnknownCpuLoad() {
        try (MockedStatic<ManagementFactory> managementFactoryMockedStatic = Mockito.mockStatic(ManagementFactory.class)) {
            OperatingSystemMXBean osBean = mock(OperatingSystemMXBean.class);
            managementFactoryMockedStatic.when(ManagementFactory::getOperatingSystemMXBean).thenReturn(osBean);
            when(osBean.getSystemCpuLoad()).thenReturn(-1.0);

            Health health = cpuHealthIndicator.health();
            assertEquals("UNKNOWN", health.getStatus().getCode());
            assertEquals("Не вдалося отримати значення", health.getDetails().get("CPU Load"));
        }
    }

    @Test
    void testHealth_Up() {
        try (MockedStatic<ManagementFactory> managementFactoryMockedStatic = Mockito.mockStatic(ManagementFactory.class)) {
            OperatingSystemMXBean osBean = mock(OperatingSystemMXBean.class);
            managementFactoryMockedStatic.when(ManagementFactory::getOperatingSystemMXBean).thenReturn(osBean);
            when(osBean.getSystemCpuLoad()).thenReturn(0.5);

            Health health = cpuHealthIndicator.health();
            assertEquals("UP", health.getStatus().getCode());
            assertEquals(50.0, health.getDetails().get("CPU Load (%)"));
        }
    }

    @Test
    void testHealth_Down() {
        try (MockedStatic<ManagementFactory> managementFactoryMockedStatic = Mockito.mockStatic(ManagementFactory.class)) {
            OperatingSystemMXBean osBean = mock(OperatingSystemMXBean.class);
            managementFactoryMockedStatic.when(ManagementFactory::getOperatingSystemMXBean).thenReturn(osBean);
            when(osBean.getSystemCpuLoad()).thenReturn(0.9);

            Health health = cpuHealthIndicator.health();
            assertEquals("DOWN", health.getStatus().getCode());
            assertEquals(90.0, health.getDetails().get("CPU Load (%)"));
        }
    }
}
