package sports.center.com.util.controller;

import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sports.center.com.controller.MetricsController;
import sports.center.com.metrics.CustomMetrics;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class MetricsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CustomMetrics customMetrics;

    @Mock
    private Timer timer;

    @InjectMocks
    private MetricsController metricsController;

    @BeforeEach
    void setUp() {
        when(customMetrics.getResponseTimer()).thenReturn(timer);
        mockMvc = MockMvcBuilders.standaloneSetup(metricsController).build();
    }

    @Test
    void testEndpoint_ReturnsCorrectResponse() throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, Metrics!"));
    }

    @Test
    void testEndpoint_CallsMetricsIncrement() throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isOk());

        verify(customMetrics, times(1)).incrementRequestCount();
    }

    @Test
    void testEndpoint_CallsMetricsTimer() throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isOk());

        verify(timer, times(1)).record(anyLong(), eq(TimeUnit.NANOSECONDS));
    }
}