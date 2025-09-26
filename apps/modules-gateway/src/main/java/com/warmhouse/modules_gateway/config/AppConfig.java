package com.warmhouse.modules_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        // Настраиваем обработку chunked encoding
        restTemplate.getInterceptors().add((request, body, execution) -> {
            // Убираем Transfer-Encoding заголовок если он есть
            request.getHeaders().remove("Transfer-Encoding");
            return execution.execute(request, body);
        });
        
        return restTemplate;
    }
}
