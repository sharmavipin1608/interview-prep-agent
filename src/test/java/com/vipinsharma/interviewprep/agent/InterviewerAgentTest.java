package com.vipinsharma.interviewprep.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;

import com.vipinsharma.interviewprep.dto.CompanyBrief;

class InterviewerAgentTest {

    private ChatClient chatClient;
    private ChatClient.ChatClientRequestSpec requestSpec;
    private ChatClient.CallResponseSpec callResponseSpec;
    private ChatClient.Builder builder;
    private ChatMemory chatMemory;

    private static final CompanyBrief SAMPLE_BRIEF = new CompanyBrief(
            "Stripe",
            "Global payment infrastructure",
            List.of("Java", "Ruby"),
            "High ownership culture",
            "Launched new pricing model",
            List.of("System design", "Distributed systems"));

    @BeforeEach
    void setUp() {
        callResponseSpec = mock(ChatClient.CallResponseSpec.class);
        when(callResponseSpec.content()).thenReturn("Tell me about your background.");

        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.advisors(any(Advisor[].class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);

        chatClient = mock(ChatClient.class);
        when(chatClient.prompt()).thenReturn(requestSpec);

        builder = mock(ChatClient.Builder.class);
        when(builder.build()).thenReturn(chatClient);

        chatMemory = mock(ChatMemory.class);
    }

    private InterviewerAgent buildAgent() {
        return new InterviewerAgent(builder, chatMemory);
    }

    private CompanyBrief sampleBrief() {
        return SAMPLE_BRIEF;
    }

    @Test
    void chat_whenValidInputs_returnsNonBlankResponse() {
        String response = buildAgent().chat("session-1", SAMPLE_BRIEF, "Backend engineer role", null, "Hello");

        assertThat(response).isNotBlank();
    }

    @Test
    void chat_whenSessionIdIsNull_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> buildAgent().chat(null, SAMPLE_BRIEF, "Backend engineer role", null, "Hello"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chat_whenSessionIdIsBlank_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> buildAgent().chat("   ", SAMPLE_BRIEF, "Backend engineer role", null, "Hello"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chat_whenCompanyBriefIsNull_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> buildAgent().chat("session-1", null, "Backend engineer role", null, "Hello"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chat_whenJobDescriptionIsNull_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> buildAgent().chat("session-1", SAMPLE_BRIEF, null, null, "Hello"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chat_whenJobDescriptionIsBlank_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> buildAgent().chat("session-1", SAMPLE_BRIEF, "  ", null, "Hello"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chat_whenUserMessageIsNull_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> buildAgent().chat("session-1", SAMPLE_BRIEF, "Backend engineer role", null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chat_whenUserMessageIsBlank_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> buildAgent().chat("session-1", SAMPLE_BRIEF, "Backend engineer role", null, "   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chat_whenWeakAreasIsNull_doesNotThrow() {
        when(callResponseSpec.content()).thenReturn("Tell me about a distributed system you designed.");
        assertDoesNotThrow(() -> buildAgent().chat("session-1", sampleBrief(), "Build APIs", null, "Hi"));
    }

    @Test
    void chat_whenWeakAreasIsNull_returnsNonBlankResponse() {
        when(callResponseSpec.content()).thenReturn("Tell me about a distributed system you designed.");
        String response = buildAgent().chat("session-1", sampleBrief(), "Build APIs", null, "Hi");
        assertThat(response).isNotBlank();
    }

    @Test
    void chat_whenValidInputs_responseMatchesLlmOutput() {
        when(callResponseSpec.content()).thenReturn("Tell me about your background.");

        String response = buildAgent().chat(
                "session-1", SAMPLE_BRIEF, "Backend engineer role", List.of("concurrency"), "Hello");

        assertThat(response).isEqualTo("Tell me about your background.");
    }
}
