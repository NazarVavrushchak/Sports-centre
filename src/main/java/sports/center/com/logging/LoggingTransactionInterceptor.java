package sports.center.com.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Slf4j
@Component
public class LoggingTransactionInterceptor implements HandlerInterceptor {
    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String transactionId = request.getHeader(TRANSACTION_ID_HEADER);
        if (transactionId == null || transactionId.isEmpty()) {
            transactionId = UUID.randomUUID().toString();
        }

        request.setAttribute(TRANSACTION_ID_HEADER, transactionId);
        MDC.put("transactionId", transactionId);
        log.info("Transaction id [{}] - Started request: {} {}", transactionId, request.getMethod(), request.getRequestURI());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String transactionId = (String) request.getAttribute(TRANSACTION_ID_HEADER);
        int status = response.getStatus();

        log.info("Transaction [{}] - Completed request: {} {} | Status: {}",
                transactionId, request.getMethod(), request.getRequestURI(), status);

        MDC.clear();
    }
}