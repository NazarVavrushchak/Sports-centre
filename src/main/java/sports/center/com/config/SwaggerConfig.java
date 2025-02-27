package sports.center.com.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Documentation")
                        .version("1.0")
                        .description("Api Documentation"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        Logger logger = LoggerFactory.getLogger(SwaggerConfig.class);

        GroupedOpenApi api = GroupedOpenApi.builder()
                .group("public")
                .packagesToScan("sports.center.com.controller")
                .pathsToMatch("/trainee/**", "/trainer/**", "/training/**")
                .build();

        logger.info("Swagger API Loaded for paths: /trainee/**, /trainer/**, /training/**");
        return api;
    }
}