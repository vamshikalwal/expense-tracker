package com.expenseTracker.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Expense Tracker API",
                version = "1.0.0",
                description = "Complete REST API for Expense Tracker application with JWT authentication",
                contact = @Contact(
                        name = "Expense Tracker",
                        url = "https://github.com"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080/api",
                        description = "Development Server"
                ),
                @Server(
                        url = "https://api.expensetracker.com/api",
                        description = "Production Server"
                )
        }
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Enter JWT token"
)
public class OpenApiConfig {
}
