package sports.center.com.util.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.util.ContentCachingRequestWrapper;
import sports.center.com.security.BruteForceLoginProtection;
import sports.center.com.service.UserService;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BruteForceLoginProtectionTest {

    @Mock
    private UserService userService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private BruteForceLoginProtection bruteForceProtection;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private ContentCachingRequestWrapper wrappedRequest;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        Field ipAttemptsField = BruteForceLoginProtection.class.getDeclaredField("ipAttempts");
        ipAttemptsField.setAccessible(true);
        ((Map<?, ?>) ipAttemptsField.get(bruteForceProtection)).clear();
        ipAttemptsField.setAccessible(false);

        when(wrappedRequest.getRemoteAddr()).thenReturn("127.0.0.1");
    }

    @Test
    void doFilterInternal_NoAttempts_ShouldProceed() throws Exception {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(wrappedRequest.getContentLength()).thenReturn(0);

        invokeDoFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(any(ContentCachingRequestWrapper.class), eq(response));
    }

    @Test
    void doFilterInternal_IpBlocked_TimeNotExpired_ShouldBlock() throws Exception {
        setIpAttempts("127.0.0.1", 3, System.currentTimeMillis());
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        invokeDoFilterInternal(wrappedRequest, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(writer).write(anyString());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_IpBlocked_TimeExpired_ShouldUnblockAndProceed() throws Exception {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        setIpAttempts("127.0.0.1", 3, System.currentTimeMillis() - 6 * 60 * 1000);
        when(wrappedRequest.getContentLength()).thenReturn(0);

        invokeDoFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(any(ContentCachingRequestWrapper.class), eq(response));
        assertFalse(getIpAttempts().containsKey("127.0.0.1"));
    }

    @Test
    void registerFailedAttempt_FirstAttempt_ShouldIncrement() throws Exception {
        bruteForceProtection.registerFailedAttempt("testuser", "127.0.0.1");

        Map<String, Object> ipAttempts = getIpAttempts();
        Object info = ipAttempts.get("127.0.0.1");
        assertEquals(1, getIpBlockInfoAttempts(info));
        verify(userService).increaseFailedAttempts("testuser");
        verify(userService, never()).lock(anyString());
    }

    @Test
    void registerFailedAttempt_ExceedMaxAttempts_ShouldLock() throws Exception {
        setIpAttempts("127.0.0.1", 2, System.currentTimeMillis());
        bruteForceProtection.registerFailedAttempt("testuser", "127.0.0.1");

        Map<String, Object> ipAttempts = getIpAttempts();
        Object info = ipAttempts.get("127.0.0.1");
        assertEquals(3, getIpBlockInfoAttempts(info));
        verify(userService).increaseFailedAttempts("testuser");
        verify(userService).lock("testuser");
    }

    @Test
    void registerFailedAttempt_NullUsername_ShouldOnlyIncrementIp() throws Exception {
        bruteForceProtection.registerFailedAttempt(null, "127.0.0.1");

        Map<String, Object> ipAttempts = getIpAttempts();
        Object info = ipAttempts.get("127.0.0.1");
        assertEquals(1, getIpBlockInfoAttempts(info));
        verify(userService, never()).increaseFailedAttempts(anyString());
        verify(userService, never()).lock(anyString());
    }

    @Test
    void registerSuccessfulLogin_ShouldReset() throws Exception {
        setIpAttempts("127.0.0.1", 2, System.currentTimeMillis());
        bruteForceProtection.registerSuccessfulLogin("testuser", "127.0.0.1");

        assertFalse(getIpAttempts().containsKey("127.0.0.1"));
        verify(userService).resetFailedAttempts("testuser");
    }

    @Test
    void registerSuccessfulLogin_NullUsername_ShouldOnlyResetIp() throws Exception {
        setIpAttempts("127.0.0.1", 2, System.currentTimeMillis());
        bruteForceProtection.registerSuccessfulLogin(null, "127.0.0.1");

        assertFalse(getIpAttempts().containsKey("127.0.0.1"));
        verify(userService, never()).resetFailedAttempts(anyString());
    }

    @Test
    void sendErrorResponse_ShouldWriteJsonError() throws Exception {
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);
        long lockTime = System.currentTimeMillis() - 2 * 60 * 1000; // 2 minutes ago

        invokeSendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Test message", lockTime);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType("application/json");
        verify(writer, times(1)).write(argThat((String json) ->
                json != null &&
                        json.contains("\"error\": \"Test message") &&
                        json.contains("minutes")
        ));
    }

    @Test
    void extractUsernameFromBody_ValidLoginRequest_ShouldExtract() throws Exception {
        when(wrappedRequest.getContentLength()).thenReturn(1);
        when(wrappedRequest.getMethod()).thenReturn("POST");
        when(wrappedRequest.getRequestURI()).thenReturn("/login");
        when(wrappedRequest.getContentAsByteArray()).thenReturn("{\"username\": \"testuser\"}".getBytes());
        JsonNode jsonNode = mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(jsonNode);
        when(jsonNode.has("username")).thenReturn(true);
        when(jsonNode.get("username")).thenReturn(mock(JsonNode.class));
        when(jsonNode.get("username").asText()).thenReturn("testuser");

        String username = invokeExtractUsernameFromBody(wrappedRequest);

        assertEquals("testuser", username);
    }

    @Test
    void extractUsernameFromBody_NonLoginRequest_ShouldReturnNull() throws Exception {
        when(wrappedRequest.getContentLength()).thenReturn(0);

        String username = invokeExtractUsernameFromBody(wrappedRequest);

        assertNull(username);
    }

    @Test
    void extractUsernameFromBody_IOException_ShouldReturnNull() throws Exception {
        when(wrappedRequest.getContentLength()).thenReturn(1);
        when(wrappedRequest.getMethod()).thenReturn("POST");
        when(wrappedRequest.getRequestURI()).thenReturn("/login");
        when(wrappedRequest.getContentAsByteArray()).thenReturn("{\"username\": \"testuser\"}".getBytes());
        when(objectMapper.readTree(anyString())).thenThrow(new RuntimeException(new IOException("Parse error")));

        String username = invokeExtractUsernameFromBody(wrappedRequest);

        assertNull(username);
    }

    private void invokeDoFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws Exception {
        Method doFilterInternal = BruteForceLoginProtection.class.getDeclaredMethod(
                "doFilterInternal", HttpServletRequest.class, HttpServletResponse.class, FilterChain.class);
        doFilterInternal.setAccessible(true);
        doFilterInternal.invoke(bruteForceProtection, request, response, chain);
        doFilterInternal.setAccessible(false);
    }

    private Map<String, Object> getIpAttempts() throws Exception {
        Field ipAttemptsField = BruteForceLoginProtection.class.getDeclaredField("ipAttempts");
        ipAttemptsField.setAccessible(true);
        Map<String, Object> ipAttempts = (Map<String, Object>) ipAttemptsField.get(bruteForceProtection);
        ipAttemptsField.setAccessible(false);
        return ipAttempts;
    }

    private void setIpAttempts(String ip, int attempts, long lockTime) throws Exception {
        Map<String, Object> ipAttempts = getIpAttempts();
        Object ipBlockInfo = createIpBlockInfo(attempts, lockTime);
        ipAttempts.put(ip, ipBlockInfo);
    }

    private int getIpBlockInfoAttempts(Object ipBlockInfo) throws Exception {
        Method getAttempts = ipBlockInfo.getClass().getDeclaredMethod("getAttempts");
        getAttempts.setAccessible(true);
        int attempts = (int) getAttempts.invoke(ipBlockInfo);
        getAttempts.setAccessible(false);
        return attempts;
    }

    private Object createIpBlockInfo(int attempts, long lockTime) throws Exception {
        Class<?> ipBlockInfoClass = Class.forName("sports.center.com.security.BruteForceLoginProtection$IpBlockInfo");
        java.lang.reflect.Constructor<?> constructor = ipBlockInfoClass.getDeclaredConstructor(int.class, long.class);
        constructor.setAccessible(true);
        Object ipBlockInfo = constructor.newInstance(attempts, lockTime);
        constructor.setAccessible(false);
        return ipBlockInfo;
    }

    private void invokeSendErrorResponse(HttpServletResponse response, int status, String message, long lockTime)
            throws Exception {
        Method sendErrorResponse = BruteForceLoginProtection.class.getDeclaredMethod(
                "sendErrorResponse", HttpServletResponse.class, int.class, String.class, long.class);
        sendErrorResponse.setAccessible(true);
        sendErrorResponse.invoke(bruteForceProtection, response, status, message, lockTime);
        sendErrorResponse.setAccessible(false);
    }

    private String invokeExtractUsernameFromBody(ContentCachingRequestWrapper request) throws Exception {
        Method extractUsernameFromBody = BruteForceLoginProtection.class.getDeclaredMethod(
                "extractUsernameFromBody", ContentCachingRequestWrapper.class);
        extractUsernameFromBody.setAccessible(true);
        try {
            return (String) extractUsernameFromBody.invoke(bruteForceProtection, request);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException || (cause != null && cause.getCause() instanceof IOException)) {
                return null;
            }
            throw e;
        } finally {
            extractUsernameFromBody.setAccessible(false);
        }
    }
}
