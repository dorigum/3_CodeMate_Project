package com.codemate;

import com.codemate.support.IntegrationTestSupport;
import java.sql.Connection;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("mysql")
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "spring.flyway.baseline-on-migrate=false",
        "spring.jpa.show-sql=false",
        "spring.jpa.properties.hibernate.format_sql=false",
        "spring.jpa.properties.hibernate.highlight_sql=false"
})
@Testcontainers(disabledWithoutDocker = true)
class MySqlTestcontainersIntegrationTest extends IntegrationTestSupport {

    @Container
    @ServiceConnection
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withDatabaseName("codemate_test")
            .withUsername("codemate")
            .withPassword("codemate_test_password");

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Flyway flyway;

    @Test
    void mysqlContainerAndFlywayMigrationAreReady() throws Exception {
        assertThat(MYSQL.isRunning()).isTrue();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("2");

        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("MySQL");

            try (ResultSet tables = connection.getMetaData().getTables(
                    connection.getCatalog(),
                    null,
                    "study_members",
                    new String[]{"TABLE"}
            )) {
                assertThat(tables.next()).isTrue();
            }

            try (ResultSet tables = connection.getMetaData().getTables(
                    connection.getCatalog(),
                    null,
                    "refresh_tokens",
                    new String[]{"TABLE"}
            )) {
                assertThat(tables.next()).isTrue();
            }
        }
    }

    @Test
    void coreStudyFlowWorksOnRealMySql() throws Exception {
        String hostToken = signupAndLogin(
                "mysql-host@example.com",
                "mysql-host"
        );
        Long studyId = createStudy(hostToken, "MySQL 통합 테스트 스터디", 4);

        mockMvc.perform(patch("/api/studies/{studyId}/close", studyId)
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));

        mockMvc.perform(get("/api/studies/{studyId}", studyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("MySQL 통합 테스트 스터디"))
                .andExpect(jsonPath("$.data.status").value("CLOSED"));
    }
}
