package com.vipinsharma.interviewprep.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import com.vipinsharma.interviewprep.dto.SessionFeedback;

class CoachAgentTest {

    private static final UUID SESSION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String VALID_TRANSCRIPT = "Candidate: I would use a hash map for O(1) lookups.";
    private static final String VALID_JSON = """
            {
              "overallScore": 4,
              "weakAreas": ["system design", "concurrency"],
              "improvementSuggestions": ["Practice more LeetCode hard problems", "Study distributed systems"],
              "detailedFeedback": "The candidate demonstrated solid fundamentals but needs improvement in system design."
            }
            """;

    private ChatClient chatClient;
    private ChatClient.ChatClientRequestSpec requestSpec;
    private ChatClient.CallResponseSpec callResponseSpec;
    private ChatClient.Builder builder;

    @BeforeEach
    void setUp() {
        callResponseSpec = mock(ChatClient.CallResponseSpec.class);
        when(callResponseSpec.content()).thenReturn(VALID_JSON);

        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        when(requestSpec.call()).thenReturn(callResponseSpec);

        chatClient = mock(ChatClient.class);
        when(chatClient.prompt(anyString())).thenReturn(requestSpec);

        builder = mock(ChatClient.Builder.class);
        when(builder.defaultSystem(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(chatClient);
    }

    private CoachAgent buildAgent() {
        return new CoachAgent(builder);
    }

    @Test
    void evaluate_whenValidInputs_returnsSessionFeedbackWithCorrectSessionId() {
        SessionFeedback feedback = buildAgent().evaluate(SESSION_ID, VALID_TRANSCRIPT, List.of("algorithms"));

        assertThat(feedback.sessionId()).isEqualTo(SESSION_ID);
    }

    @Test
    void evaluate_whenValidInputs_returnsCorrectOverallScore() {
        SessionFeedback feedback = buildAgent().evaluate(SESSION_ID, VALID_TRANSCRIPT, List.of("algorithms"));

        assertThat(feedback.overallScore()).isEqualTo(4);
    }

    @Test
    void evaluate_whenValidInputs_returnsWeakAreas() {
        SessionFeedback feedback = buildAgent().evaluate(SESSION_ID, VALID_TRANSCRIPT, List.of("algorithms"));

        assertThat(feedback.weakAreas()).containsExactly("system design", "concurrency");
    }

    @Test
    void evaluate_whenValidInputs_returnsImprovementSuggestions() {
        SessionFeedback feedback = buildAgent().evaluate(SESSION_ID, VALID_TRANSCRIPT, List.of("algorithms"));

        assertThat(feedback.improvementSuggestions())
                .containsExactly("Practice more LeetCode hard problems", "Study distributed systems");
    }

    @Test
    void evaluate_whenValidInputs_returnsDetailedFeedback() {
        SessionFeedback feedback = buildAgent().evaluate(SESSION_ID, VALID_TRANSCRIPT, List.of("algorithms"));

        assertThat(feedback.detailedFeedback())
                .isEqualTo("The candidate demonstrated solid fundamentals but needs improvement in system design.");
    }

    @Test
    void evaluate_whenSessionIdIsNull_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> buildAgent().evaluate(null, VALID_TRANSCRIPT, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void evaluate_whenTranscriptIsNull_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> buildAgent().evaluate(SESSION_ID, null, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void evaluate_whenTranscriptIsBlank_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> buildAgent().evaluate(SESSION_ID, "   ", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void evaluate_whenWeakAreaHistoryIsNull_doesNotThrow() {
        assertDoesNotThrow(() -> buildAgent().evaluate(SESSION_ID, VALID_TRANSCRIPT, null));
    }

    @Test
    void evaluate_whenLlmReturnsInvalidJson_returnsFallbackWithOverallScoreZero() {
        when(callResponseSpec.content()).thenReturn("This is not valid JSON");

        SessionFeedback feedback = buildAgent().evaluate(SESSION_ID, VALID_TRANSCRIPT, List.of());

        assertThat(feedback.overallScore()).isEqualTo(0);
    }

    @Test
    void evaluate_whenLlmReturnsInvalidJson_fallbackHasEvaluationUnavailableMessage() {
        when(callResponseSpec.content()).thenReturn("{broken json");

        SessionFeedback feedback = buildAgent().evaluate(SESSION_ID, VALID_TRANSCRIPT, List.of());

        assertThat(feedback.detailedFeedback()).isEqualTo("Evaluation unavailable");
    }
}
