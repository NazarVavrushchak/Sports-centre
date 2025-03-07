package sports.center.com.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

@Component
public class CustomMetrics {
    private final Counter requestCounter;
    private final Timer responseTime;
    private final MeterRegistry meterRegistry;

    public CustomMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.requestCounter = meterRegistry.counter("http_requests_total", "type", "all");
        this.responseTime = meterRegistry.timer("http_response_time", "type", "all");
    }

    public void incrementRequestCount() {
        requestCounter.increment();
    }

    public Timer getResponseTimer() {
        return responseTime;
    }

    @PostConstruct
    public void registerMemoryMetrics() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

        meterRegistry.gauge("jvm_memory_used_bytes", memoryMXBean,
                bean -> bean.getHeapMemoryUsage().getUsed());
    }

}
