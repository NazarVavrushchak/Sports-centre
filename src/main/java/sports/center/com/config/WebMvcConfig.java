package sports.center.com.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import sports.center.com.logging.LoggingFilter;

@RequiredArgsConstructor
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Bean
    public LoggingFilter loggingFilter() {
        return new LoggingFilter();
    }
}