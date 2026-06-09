package com.codemate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer());

    @Test
    void defaultProfileUsesH2() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getEnvironment().getDefaultProfiles()).contains("h2");
            assertThat(context.getEnvironment().getProperty("spring.datasource.url"))
                    .startsWith("jdbc:h2:mem:codemate");
            assertThat(context.getEnvironment().getProperty("spring.h2.console.enabled"))
                    .isEqualTo("true");
            assertThat(context.getEnvironment().getProperty("spring.jpa.hibernate.ddl-auto"))
                    .isEqualTo("validate");
            assertThat(context.getEnvironment().getProperty("spring.flyway.locations"))
                    .isEqualTo("classpath:db/migration/h2");
        });
    }

    @Test
    void mysqlProfileUsesEnvironmentVariables() {
        contextRunner
                .withPropertyValues(
                        "spring.profiles.active=mysql",
                        "CODEMATE_DB_HOST=mysql.example.com",
                        "CODEMATE_DB_PORT=3307",
                        "CODEMATE_DB_NAME=codemate_test",
                        "CODEMATE_DB_USERNAME=test_user",
                        "CODEMATE_DB_PASSWORD=test_password"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getEnvironment().getProperty("spring.datasource.url"))
                            .startsWith("jdbc:mysql://mysql.example.com:3307/codemate_test");
                    assertThat(context.getEnvironment().getProperty("spring.datasource.username"))
                            .isEqualTo("test_user");
                    assertThat(context.getEnvironment().getProperty("spring.datasource.password"))
                            .isEqualTo("test_password");
                    assertThat(context.getEnvironment().getProperty("spring.h2.console.enabled"))
                            .isEqualTo("false");
                    assertThat(context.getEnvironment().getProperty("spring.jpa.hibernate.ddl-auto"))
                            .isEqualTo("validate");
                    assertThat(context.getEnvironment().getProperty("spring.flyway.baseline-on-migrate"))
                            .isEqualTo("true");
                    assertThat(context.getEnvironment().getProperty("spring.flyway.locations"))
                            .isEqualTo("classpath:db/migration/mysql");
                });
    }

    @Test
    void prodProfileUsesSecureProductionDefaults() {
        contextRunner
                .withPropertyValues(
                        "spring.profiles.active=prod",
                        "CODEMATE_DB_HOST=prod-db.example.com",
                        "CODEMATE_DB_PORT=3306",
                        "CODEMATE_DB_NAME=codemate_prod",
                        "CODEMATE_DB_USERNAME=codemate_prod_user",
                        "CODEMATE_DB_PASSWORD=prod-db-password",
                        "CODEMATE_JWT_SECRET=Y29kZW1hdGUtcHJvZHVjdGlvbi1qd3Qtc2VjcmV0LWtleS10ZXN0"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getEnvironment().getActiveProfiles())
                            .containsExactly("prod");
                    assertThat(context.getEnvironment().getProperty("spring.datasource.url"))
                            .startsWith("jdbc:mysql://prod-db.example.com:3306/codemate_prod");
                    assertThat(context.getEnvironment().getProperty("spring.h2.console.enabled"))
                            .isEqualTo("false");
                    assertThat(context.getEnvironment().getProperty("spring.jpa.show-sql"))
                            .isEqualTo("false");
                    assertThat(context.getEnvironment().getProperty("spring.flyway.baseline-on-migrate"))
                            .isEqualTo("false");
                    assertThat(context.getEnvironment().getProperty("springdoc.api-docs.enabled"))
                            .isEqualTo("false");
                    assertThat(context.getEnvironment().getProperty("springdoc.swagger-ui.enabled"))
                            .isEqualTo("false");
                    assertThat(context.getEnvironment().getProperty("server.error.include-stacktrace"))
                            .isEqualTo("never");
                    assertThat(context.getEnvironment().getProperty("codemate.jwt.secret"))
                            .isEqualTo("Y29kZW1hdGUtcHJvZHVjdGlvbi1qd3Qtc2VjcmV0LWtleS10ZXN0");
                });
    }
}
