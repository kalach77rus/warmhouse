package com.warmhouse.modules_gateway.service;

import com.warmhouse.modules_gateway.model.ModuleRegistration;
import com.warmhouse.modules_gateway.repository.ModuleRegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProxyService {
    
    private final ModuleRegistrationRepository repository;
    private final RestTemplate restTemplate;
    
    public ResponseEntity<String> proxyRequest(String moduleId, HttpMethod method, String targetPath, 
                                             HttpHeaders headers, String body) {
        
        log.info("Proxying {} request to module {} at path {}", method, moduleId, targetPath);
        
        // 1. Валидация безопасности
        validateSecurity(targetPath);
        
        // 2. Получение информации о модуле
        ModuleRegistration module = getModuleInfo(moduleId);
        
        // 3. Проверка активности модуля
        validateModuleActivity(module);
        
        // 4. Выполнение прокси-запроса
        return executeProxyRequest(module, method, targetPath, body);
    }
    
    private void validateSecurity(String targetPath) {
        // Заглушка для ограничения путей проксирования
        if (targetPath.startsWith("/secret")) {
            throw new SecurityException("Access to secret paths is not allowed");
        }
        
        // Можно добавить другие проверки безопасности
        log.debug("Security validation passed for path: {}", targetPath);
    }
    
    private ModuleRegistration getModuleInfo(String moduleId) {
        // Ищем модуль по moduleId (предполагаем, что homeId можно получить из контекста или параметров)
        // Для простоты ищем первый активный модуль с таким moduleId
        return repository.findAll().stream()
                .filter(module -> module.getModuleId().equals(moduleId))
                .filter(module -> "ACTIVE".equals(module.getStatus()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Module not found or not active: " + moduleId));
    }
    
    private void validateModuleActivity(ModuleRegistration module) {
        // Проверяем, что модуль зарегистрирован
        if (module == null) {
            throw new RuntimeException("Module not registered: " + module.getModuleId());
        }
        
        // Проверяем статус модуля
        if (!"ACTIVE".equals(module.getStatus())) {
            throw new RuntimeException("Module is not active: " + module.getModuleId());
        }
        
        // Проверяем последний heartbeat (если он был более 5 минут назад, считаем модуль неактивным)
        if (module.getLastHeartbeat() != null) {
            long minutesSinceLastHeartbeat = ChronoUnit.MINUTES.between(
                module.getLastHeartbeat(), LocalDateTime.now());
            
            if (minutesSinceLastHeartbeat > 5) {
                throw new RuntimeException("Module appears to be inactive (no heartbeat for " + 
                    minutesSinceLastHeartbeat + " minutes): " + module.getModuleId());
            }
        }
        
        log.debug("Module activity validation passed for: {}", module.getModuleId());
    }
    
    private ResponseEntity<String> executeProxyRequest(ModuleRegistration module, HttpMethod method, 
                                                     String targetPath, String body) {
        
        String targetUrl = module.getBaseUrl() + targetPath;
        
        log.info("Executing proxy request to: {} {}", method, targetUrl);
        
        try {
            // Создаем HttpEntity с минимальными заголовками
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Явно убираем проблемные заголовки
            headers.remove("Transfer-Encoding");
            headers.remove("Connection");
            headers.remove("Keep-Alive");
            
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                targetUrl,
                method,
                entity,
                String.class
            );
            
            log.info("Proxy request successful: {} {}", response.getStatusCode(), targetUrl);
            return response;
            
        } catch (HttpClientErrorException e) {
            log.error("Client error during proxy request to {}: {}", targetUrl, e.getMessage());
            throw new RuntimeException("Module returned client error: " + e.getStatusCode() + " - " + e.getMessage());
            
        } catch (HttpServerErrorException e) {
            log.error("Server error during proxy request to {}: {}", targetUrl, e.getMessage());
            throw new RuntimeException("Module returned server error: " + e.getStatusCode() + " - " + e.getMessage());
            
        } catch (ResourceAccessException e) {
            log.error("Module is not accessible: {}", targetUrl, e);
            throw new RuntimeException("Module is not accessible: " + e.getMessage());
            
        } catch (Exception e) {
            log.error("Unexpected error during proxy request to {}: {}", targetUrl, e.getMessage(), e);
            throw new RuntimeException("Unexpected error during proxy request: " + e.getMessage());
        }
    }
}
