package sports.center.com.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sports.center.com.metrics.CustomMetrics;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
public class MetricsController {
    private final CustomMetrics customMetrics;

    public MetricsController(CustomMetrics customMetrics) {
        this.customMetrics = customMetrics;
    }

    @GetMapping("/test")
    public String testEndpoint() {
        long startTime = System.nanoTime();

        customMetrics.incrementRequestCount();

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long endTime = System.nanoTime();
        customMetrics.getResponseTimer().record(endTime - startTime, TimeUnit.NANOSECONDS);

        return "Hello, Metrics!";
    }
}