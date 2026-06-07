package com.codemate.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String JWT_SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI codeMateOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("CodeMate API")
                        .description("개발자 스터디 및 모각코 모집·참여 관리 API")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(
                                JWT_SECURITY_SCHEME,
                                new SecurityScheme()
                                        .name(JWT_SECURITY_SCHEME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ));
    }
}
