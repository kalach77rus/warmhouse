package org.example;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Device Management API",
                version = "1.0.0",
                description = "API для управления устройствами (создание, обновление, удаление, статус, конфигурация и команды)",
                contact = @Contact(name = "WarmHouse Team"),
                license = @License(name = "MIT")
        ),
        servers = {
                @Server(url = "/", description = "Default Server")
        }
)
public class DeviceManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeviceManagementApplication.class, args);
    }
}