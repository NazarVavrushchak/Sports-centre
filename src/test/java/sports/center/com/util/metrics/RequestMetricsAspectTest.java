package sports.center.com.util.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sports.center.com.metrics.RequestMetricsAspect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestMetricsAspectTest {

    private MeterRegistry meterRegistry;
    private Counter requestCounter;
    private Timer responseTimer;
    private RequestMetricsAspect requestMetricsAspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        requestCounter = meterRegistry.counter("http_requests_total", "type", "all");
        responseTimer = meterRegistry.timer("http_response_time", "type", "all");

        requestMetricsAspect = new RequestMetricsAspect(meterRegistry);
    }

    @Test
    void shouldMeasureExecutionTimeAndCountRequests() throws Throwable {
        when(joinPoint.proceed()).thenReturn("MockedResponse");

        Object result = requestMetricsAspect.measureExecutionTimeAndCountRequests(joinPoint);

        assertEquals(1.0, requestCounter.count());

        assertEquals(1, responseTimer.count());

        assertEquals("MockedResponse", result);
    }
}