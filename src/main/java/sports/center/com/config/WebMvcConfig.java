package sports.center.com.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import sports.center.com.logging.LoggingFilter;
import sports.center.com.logging.LoggingTransactionInterceptor;
import sports.center.com.service.AuthService;

@RequiredArgsConstructor
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final AuthService authService;
    private final LoggingTransactionInterceptor loggingTransactionInterceptor;

    @Bean
    public LoggingFilter loggingFilter() {
        return new LoggingFilter();
    }

}