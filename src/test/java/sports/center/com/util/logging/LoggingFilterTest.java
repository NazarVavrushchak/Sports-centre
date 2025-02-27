package sports.center.com.util.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.ContentCachingResponseWrapper;
import sports.center.com.logging.LoggingFilter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ContentCachingResponseWrapper wrappedResponse;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private LoggingFilter loggingFilter;

    @BeforeEach
    void setUp() {
        loggingFilter = new LoggingFilter();
        lenient().when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.emptyList()));
        lenient().when(response.getStatus()).thenReturn(200);
        lenient().when(wrappedResponse.getContentAsByteArray()).thenReturn("Response Body".getBytes());
    }


    @Test
    void shouldAddTransactionIdIfNotPresent() throws ServletException, IOException {
        when(request.getAttribute("X-Transaction-Id")).thenReturn(null);

        loggingFilter.doFilter(request, response, filterChain);

        verify(request).setAttribute(eq("X-Transaction-Id"), anyString());
    }


    @Test
    void shouldUseExistingTransactionId() throws ServletException, IOException {
        String existingTransactionId = UUID.randomUUID().toString();
        when(request.getAttribute("X-Transaction-Id")).thenReturn(existingTransactionId);

        loggingFilter.doFilter(request, response, filterChain);

        verify(request, never()).setAttribute(eq("X-Transaction-Id"), anyString());
    }


    @Test
    void shouldCaptureHeaders() throws Exception {
        LoggingFilter loggingFilter = new LoggingFilter();
        Method method = LoggingFilter.class.getDeclaredMethod("getHeaders", HttpServletRequest.class);
        method.setAccessible(true);

        when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.singleton("Authorization")));
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");

        String headers = (String) method.invoke(loggingFilter, request);

        assertEquals("Authorization: Bearer token123; ", headers);
    }

    @Test
    void shouldCaptureResponseBody() throws Exception {
        LoggingFilter loggingFilter = new LoggingFilter();
        Method method = LoggingFilter.class.getDeclaredMethod("getResponseBody", ContentCachingResponseWrapper.class);
        method.setAccessible(true);

        when(wrappedResponse.getContentAsByteArray()).thenReturn("Test Response".getBytes());

        String responseBody = (String) method.invoke(loggingFilter, wrappedResponse);

        assertEquals("Test Response", responseBody);
    }

}
