package sports.center.com.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
public class RequestMetricsAspect {

    private final Counter requestCounter;
    private final Timer responseTimer;

    public RequestMetricsAspect(MeterRegistry meterRegistry) {
        this.requestCounter = meterRegistry.counter("http_requests_total", "type", "all");
        this.responseTimer = meterRegistry.timer("http_response_time", "type", "all");
    }

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void allControllers() {}

    @Around("allControllers()")
    public Object measureExecutionTimeAndCountRequests(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        Object result = joinPoint.proceed();
        long duration = System.nanoTime() - start;

        requestCounter.increment();
        responseTimer.record(duration, TimeUnit.NANOSECONDS);

        log.info("Request counted: {} total", requestCounter.count());
        log.info("Response time recorded: {} ms", duration / 1_000_000.0);

        return result;
    }
}
