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
                });
    }
}
