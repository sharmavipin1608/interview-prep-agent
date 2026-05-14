package com.vipinsharma.interviewprep;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test: full Spring context + embedded web server binds successfully.
 * Tests the seam between the Spring Boot auto-configuration, JPA, and embedded Tomcat.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class InterviewPrepAgentApplicationWebEnvTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void embedded_web_server_binds_and_responds_to_actuator_health_or_404() {
        // No controllers exist yet — any HTTP response (including 404) confirms the
        // web server started and is accepting connections on the random port.
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/", String.class);

        assertThat(response.getStatusCode())
                .as("Expected any valid HTTP response — 404 is fine at bootstrap stage")
                .isNotNull()
                .isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.FORBIDDEN,
                        HttpStatus.UNAUTHORIZED, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    void server_port_is_assigned_a_non_zero_random_port() {
        assertThat(port)
                .as("Embedded server must bind to a valid port")
                .isGreaterThan(0)
                .isLessThanOrEqualTo(65535);
    }
}
