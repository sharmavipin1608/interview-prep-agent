package com.vipinsharma.interviewprep;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Verifies the application entry-point main() method runs without throwing.
 * Uses the "test" profile to avoid requiring real external credentials.
 */
@ActiveProfiles("test")
class InterviewPrepAgentApplicationMainTest {

    @Test
    void main_method_starts_and_stops_without_exception() {
        assertThatCode(() ->
                InterviewPrepAgentApplication.main(new String[]{"--spring.profiles.active=test",
                        "--spring.main.web-application-type=none"})
        ).doesNotThrowAnyException();
    }
}
