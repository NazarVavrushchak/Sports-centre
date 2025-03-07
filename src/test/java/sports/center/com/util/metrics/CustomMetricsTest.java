package sports.center.com.util.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sports.center.com.metrics.CustomMetrics;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CustomMetricsTest {

    private MeterRegistry meterRegistry;
    private CustomMetrics customMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        customMetrics = new CustomMetrics(meterRegistry);
    }

    @Test
    void shouldIncrementRequestCounter() {
        double initialCount = meterRegistry.counter("http_requests_total", "type", "all").count();
        customMetrics.incrementRequestCount();
        double updatedCount = meterRegistry.counter("http_requests_total", "type", "all").count();
        assertEquals(initialCount + 1, updatedCount);
    }

    @Test
    void shouldReturnResponseTimer() {
        Timer timer = customMetrics.getResponseTimer();
        assertEquals("http_response_time", timer.getId().getName());
    }

    @Test
    void shouldRegisterMemoryMetrics() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        customMetrics.registerMemoryMetrics();
        Double memoryUsage = meterRegistry.find("jvm_memory_used_bytes").gauge().value();
        assertEquals(memoryMXBean.getHeapMemoryUsage().getUsed(), memoryUsage);
    }
}

