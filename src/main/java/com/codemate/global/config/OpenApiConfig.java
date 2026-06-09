package com.codemate.global.config;

import com.codemate.global.response.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String JWT_SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI codeMateOpenApi() {
        Components components = new Components()
                .addSecuritySchemes(
                        JWT_SECURITY_SCHEME,
                        new SecurityScheme()
                                .name(JWT_SECURITY_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                );

        ModelConverters.getInstance()
                .read(ErrorResponse.class)
                .forEach(components::addSchemas);

        return new OpenAPI()
                .info(new Info()
                        .title("CodeMate API")
                        .description("개발자 스터디 및 모각코 모집·참여 관리 API")
                        .version("v1"))
                .components(components);
    }

    @Bean
    public OperationCustomizer commonErrorResponseCustomizer() {
        return (operation, handlerMethod) -> {
            operation.getResponses().forEach((responseCode, response) -> {
                if (responseCode.startsWith("4") || responseCode.startsWith("5")) {
                    applyErrorContent(response, responseCode);
                }
            });

            operation.getResponses().computeIfAbsent(
                    "500",
                    code -> applyErrorContent(
                            new ApiResponse().description("예상하지 못한 서버 오류"),
                            code
                    )
            );
            return operation;
        };
    }

    private ApiResponse applyErrorContent(ApiResponse response, String responseCode) {
        Schema<?> schema = new Schema<>().$ref("#/components/schemas/ErrorResponse");
        Example example = new Example().value(errorExample(responseCode));
        MediaType mediaType = new MediaType()
                .schema(schema)
                .addExamples("error", example);
        response.setContent(new Content().addMediaType("application/json", mediaType));
        return response;
    }

    private Map<String, Object> errorExample(String responseCode) {
        Map<String, Object> example = new LinkedHashMap<>();
        example.put("success", false);
        example.put("message", errorMessage(responseCode));

        if ("400".equals(responseCode)) {
            example.put("errors", Map.of("field", "필드 입력값이 올바르지 않습니다."));
        }
        return example;
    }

    private String errorMessage(String responseCode) {
        return switch (responseCode) {
            case "400" -> "입력값이 올바르지 않습니다.";
            case "401" -> "로그인이 필요합니다.";
            case "403" -> "접근 권한이 없습니다.";
            case "404" -> "요청한 리소스를 찾을 수 없습니다.";
            case "409" -> "요청이 현재 데이터 상태와 충돌합니다.";
            default -> "서버 내부 오류가 발생했습니다.";
        };
    }
}
