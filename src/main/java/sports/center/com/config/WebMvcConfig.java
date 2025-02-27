package sports.center.com.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import sports.center.com.logging.LoggingFilter;
import sports.center.com.logging.LoggingTransactionInterceptor;
import sports.center.com.security.BasicAuthFilter;
import sports.center.com.service.AuthService;

@RequiredArgsConstructor
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"org.springdoc"})
@Import({
        org.springdoc.core.configuration.SpringDocConfiguration.class,
        org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration.class,
        org.springdoc.core.properties.SwaggerUiConfigProperties.class,
        org.springdoc.core.properties.SwaggerUiOAuthProperties.class
})
public class WebMvcConfig implements WebMvcConfigurer {
    private final AuthService authService;
    private final LoggingTransactionInterceptor loggingTransactionInterceptor;

    @Bean
    public LoggingFilter loggingFilter() {
        return new LoggingFilter();
    }

    @Bean
    public Filter basicAuthFilter() {
        return new BasicAuthFilter(authService);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingTransactionInterceptor);
    }

    @Bean
    public GroupedOpenApi defaultApi() {
        return GroupedOpenApi.builder()
                .group("all")
                .packagesToScan("sports.center.com.controller")
                .build();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/static/swagger-ui/dist/");
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sports Center API")
                        .version("1.0")
                        .contact(new Contact().name("Support Team")
                                .email("support@example.com")
                                .url("https://example.com"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}