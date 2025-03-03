package sports.center.com.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Enumeration;
import java.util.UUID;

@Component
@Slf4j
public class LoggingFilter implements Filter {
    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(res);

        String transactionId = (String) req.getAttribute(TRANSACTION_ID_HEADER);
        if (transactionId == null || transactionId.isEmpty()) {
            transactionId = UUID.randomUUID().toString();
            req.setAttribute(TRANSACTION_ID_HEADER, transactionId);
        }
        MDC.put("transactionId", transactionId);

        log.info("Transaction [{}] - Incoming Request: [{}] {} | Headers: {}",
                transactionId, req.getMethod(), req.getRequestURI(), getHeaders(req));

        chain.doFilter(req, wrappedResponse);

        wrappedResponse.copyBodyToResponse();
        int status = wrappedResponse.getStatus();
        String responseBody = getResponseBody(wrappedResponse);

        log.info("Transaction [{}] - Response: [{}] {} | Status: {} | Body: {}",
                transactionId, req.getMethod(), req.getRequestURI(), status, responseBody);

        MDC.clear();
    }

    private String getHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        StringBuilder headers = new StringBuilder();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.append(headerName).append(": ").append(request.getHeader(headerName)).append("; ");
        }
        return headers.toString();
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        return (content.length > 0) ? new String(content) : "No body";
    }
}