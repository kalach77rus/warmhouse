package org.example.configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI telemetryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Telemetry Service API")
                        .version("v1")
                        .description("API сервиса телеметрии: чтение измерений температуры")
                        .contact(new Contact().name("WarmHouse Team").email("team@example.org"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("Проект WarmHouse")
                        .url("https://example.org/warmhouse"));
    }
}


