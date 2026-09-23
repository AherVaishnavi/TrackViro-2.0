package com.trackviro.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * NEW in Step 7.
 *
 * Uses io.swagger.v3.oas.models.* (swagger-core, pulled in transitively
 * by springdoc-openapi-starter-webmvc-ui) to describe the API and
 * declare a single global "bearerAuth" security scheme — the padlock
 * every endpoint in Swagger UI shows, and where you paste the token
 * from POST /api/auth/login.
 *
 * This class has no dependency on Jackson at all, in either version —
 * it only builds swagger-core model objects, which is why the
 * Jackson 2 vs Jackson 3 situation elsewhere in this project (see the
 * Step 7 summary) does not touch this file.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI trackViroOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("TrackViro 2.0 API")
                        .version("2.0")
                        .description(
                            "Corporate expense tracking REST API. " +
                            "Log in via POST /api/auth/login, then click " +
                            "Authorize above and paste the returned token " +
                            "(no need to type \"Bearer \" — Swagger adds that " +
                            "prefix itself). " +
                            "Endpoints are grouped by tag to match each role: " +
                            "Authentication (public), Employee, Manager, " +
                            "Finance, and Profile (shared by all three roles)."
                        ))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
