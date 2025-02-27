package sports.center.com.util.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import sports.center.com.logging.LoggingTransactionInterceptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingTransactionInterceptorTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private LoggingTransactionInterceptor interceptor;

    @BeforeEach
    void setUp() {
        MDC.clear();
    }

    @Test
    void shouldGenerateTransactionIdIfMissing() {
        when(request.getHeader("X-Transaction-Id")).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/test");

        boolean result = interceptor.preHandle(request, response, new Object());

        verify(request).setAttribute(eq("X-Transaction-Id"), anyString());
        assertNotNull(MDC.get("transactionId"), "MDC should contain a transactionId");
        assertTrue(result, "Interceptor should allow request to proceed");
    }

    @Test
    void shouldUseExistingTransactionId() {
        String existingTransactionId = UUID.randomUUID().toString();
        when(request.getHeader("X-Transaction-Id")).thenReturn(existingTransactionId);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/test");

        boolean result = interceptor.preHandle(request, response, new Object());

        verify(request).setAttribute("X-Transaction-Id", existingTransactionId);
        assertEquals(existingTransactionId, MDC.get("transactionId"), "MDC should contain the existing transactionId");
        assertTrue(result, "Interceptor should allow request to proceed");
    }

    @Test
    void shouldLogRequestCompletionAndClearMDC() {
        String transactionId = UUID.randomUUID().toString();
        when(request.getAttribute("X-Transaction-Id")).thenReturn(transactionId);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/test");
        when(response.getStatus()).thenReturn(200);

        interceptor.afterCompletion(request, response, new Object(), null);

        assertNull(MDC.get("transactionId"), "MDC should be cleared after request completion");
    }
}

