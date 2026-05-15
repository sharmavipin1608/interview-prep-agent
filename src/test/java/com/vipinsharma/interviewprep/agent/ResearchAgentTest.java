package com.vipinsharma.interviewprep.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import com.vipinsharma.interviewprep.agent.tools.TavilySearchTool;
import com.vipinsharma.interviewprep.dto.CompanyBrief;

class ResearchAgentTest {

    private ChatClient chatClient;
    private ChatClient.ChatClientRequestSpec requestSpec;
    private ChatClient.CallResponseSpec callResponseSpec;
    private TavilySearchTool tavilySearchTool;

    @BeforeEach
    void setUp() {
        callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        when(requestSpec.tools(any(Object[].class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);

        chatClient = mock(ChatClient.class);
        when(chatClient.prompt(anyString())).thenReturn(requestSpec);

        ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
        when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);

        tavilySearchTool = mock(TavilySearchTool.class);
    }

    private ResearchAgent buildAgent(ChatClient.Builder builder) {
        return new ResearchAgent(builder, tavilySearchTool);
    }

    @Test
    void research_whenCompanyNameIsNull_throwsIllegalArgumentException() {
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.defaultSystem(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(chatClient);

        assertThatThrownBy(() -> buildAgent(builder).research(null, "Engineer", "Build systems"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void research_whenJobTitleIsBlank_throwsIllegalArgumentException() {
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.defaultSystem(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(chatClient);

        assertThatThrownBy(() -> buildAgent(builder).research("Stripe", "  ", "Build systems"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void research_whenJobDescriptionIsBlank_throwsIllegalArgumentException() {
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.defaultSystem(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(chatClient);

        assertThatThrownBy(() -> buildAgent(builder).research("Stripe", "Engineer", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void research_whenLlmReturnsValidJson_returnsCompanyBriefWithCompanySummary() {
        String json = """
                {
                  "companySummary": "Stripe is a global payment infrastructure company.",
                  "techStack": ["Java", "Ruby", "Go"],
                  "cultureSignals": "High ownership, data-driven culture.",
                  "recentNews": "Stripe launched new pricing model.",
                  "likelyInterviewTopics": ["System design", "Distributed systems"]
                }
                """;
        when(callResponseSpec.content()).thenReturn(json);

        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.defaultSystem(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(chatClient);

        CompanyBrief brief = buildAgent(builder).research("Stripe", "Software Engineer", "Build APIs");

        assertThat(brief.companySummary()).isEqualTo("Stripe is a global payment infrastructure company.");
    }

    @Test
    void research_whenLlmReturnsValidJson_returnsCompanyBriefWithTechStack() {
        String json = """
                {
                  "companySummary": "Stripe is a global payment infrastructure company.",
                  "techStack": ["Java", "Ruby", "Go"],
                  "cultureSignals": "High ownership, data-driven culture.",
                  "recentNews": "Stripe launched new pricing model.",
                  "likelyInterviewTopics": ["System design", "Distributed systems"]
                }
                """;
        when(callResponseSpec.content()).thenReturn(json);

        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.defaultSystem(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(chatClient);

        CompanyBrief brief = buildAgent(builder).research("Stripe", "Software Engineer", "Build APIs");

        assertThat(brief.techStack()).containsExactly("Java", "Ruby", "Go");
    }

    @Test
    void research_whenLlmReturnsValidJson_returnsCompanyBriefWithCultureSignals() {
        String json = """
                {
                  "companySummary": "Stripe is a global payment infrastructure company.",
                  "techStack": ["Java", "Ruby", "Go"],
                  "cultureSignals": "High ownership, data-driven culture.",
                  "recentNews": "Stripe launched new pricing model.",
                  "likelyInterviewTopics": ["System design", "Distributed systems"]
                }
                """;
        when(callResponseSpec.content()).thenReturn(json);

        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.defaultSystem(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(chatClient);

        CompanyBrief brief = buildAgent(builder).research("Stripe", "Software Engineer", "Build APIs");

        assertThat(brief.cultureSignals()).isEqualTo("High ownership, data-driven culture.");
    }

    @Test
    void research_whenLlmReturnsValidJson_returnsCompanyBriefWithRecentNews() {
        String json = """
                {
                  "companySummary": "Stripe is a global payment infrastructure company.",
                  "techStack": ["Java", "Ruby", "Go"],
                  "cultureSignals": "High ownership, data-driven culture.",
                  "recentNews": "Stripe launched new pricing model.",
                  "likelyInterviewTopics": ["System design", "Distributed systems"]
                }
                """;
        when(callResponseSpec.content()).thenReturn(json);

        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.defaultSystem(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(chatClient);

        CompanyBrief brief = buildAgent(builder).research("Stripe", "Software Engineer", "Build APIs");

        assertThat(brief.recentNews()).isEqualTo("Stripe launched new pricing model.");
    }

    @Test
    void research_whenLlmReturnsValidJson_returnsCompanyBriefWithLikelyInterviewTopics() {
        String json = """
                {
                  "companySummary": "Stripe is a global payment infrastructure company.",
                  "techStack": ["Java", "Ruby", "Go"],
                  "cultureSignals": "High ownership, data-driven culture.",
                  "recentNews": "Stripe launched new pricing model.",
                  "likelyInterviewTopics": ["System design", "Distributed systems"]
                }
                """;
        when(callResponseSpec.content()).thenReturn(json);

        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.defaultSystem(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(chatClient);

        CompanyBrief brief = buildAgent(builder).research("Stripe", "Software Engineer", "Build APIs");

        assertThat(brief.likelyInterviewTopics()).containsExactly("System design", "Distributed systems");
    }

    @Test
    void research_whenLlmReturnsInvalidJson_returnsFallbackCompanyBrief() {
        when(callResponseSpec.content()).thenReturn("This is not valid JSON at all");

        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.defaultSystem(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(chatClient);

        CompanyBrief brief = buildAgent(builder).research("Stripe", "Software Engineer", "Build APIs");

        assertThat(brief.companyName()).isEqualTo("Stripe");
    }

    @Test
    void research_whenLlmReturnsInvalidJson_fallbackBriefHasNullCompanySummary() {
        when(callResponseSpec.content()).thenReturn("{broken json");

        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.defaultSystem(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(chatClient);

        CompanyBrief brief = buildAgent(builder).research("Stripe", "Software Engineer", "Build APIs");

        assertThat(brief.companySummary()).isNull();
    }

    @Test
    void research_whenLlmReturnsValidJson_companyNameIsPassedThrough() {
        String json = """
                {
                  "companySummary": "A leading tech firm.",
                  "techStack": ["Python"],
                  "cultureSignals": "Fast-paced.",
                  "recentNews": "IPO planned.",
                  "likelyInterviewTopics": ["Algorithms"]
                }
                """;
        when(callResponseSpec.content()).thenReturn(json);

        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.defaultSystem(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(chatClient);

        CompanyBrief brief = buildAgent(builder).research("Acme Corp", "Backend Engineer", "Scale services");

        assertThat(brief.companyName()).isEqualTo("Acme Corp");
    }

    @Test
    void research_whenLlmReturnsValidJson_techStackContainsExpectedSize() {
        String json = """
                {
                  "companySummary": "Summary.",
                  "techStack": ["Java", "Kubernetes", "Kafka"],
                  "cultureSignals": "Inclusive culture.",
                  "recentNews": "New office opened.",
                  "likelyInterviewTopics": ["Concurrency", "Microservices", "Cloud"]
                }
                """;
        when(callResponseSpec.content()).thenReturn(json);

        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.defaultSystem(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(chatClient);

        CompanyBrief brief = buildAgent(builder).research("Atlassian", "SRE", "Maintain infra");

        assertThat(brief.techStack()).hasSize(3);
    }

    @Test
    void research_whenLlmReturnsValidJson_likelyInterviewTopicsContainsExpectedSize() {
        String json = """
                {
                  "companySummary": "Summary.",
                  "techStack": ["Java"],
                  "cultureSignals": "Culture.",
                  "recentNews": "News.",
                  "likelyInterviewTopics": ["Concurrency", "Microservices", "Cloud"]
                }
                """;
        when(callResponseSpec.content()).thenReturn(json);

        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.defaultSystem(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(chatClient);

        CompanyBrief brief = buildAgent(builder).research("Atlassian", "SRE", "Maintain infra");

        assertThat(brief.likelyInterviewTopics()).hasSize(3);
    }

    @Test
    void research_whenCompanyNameIsBlank_throwsIllegalArgumentException() {
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.defaultSystem(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(chatClient);

        assertThatThrownBy(() -> buildAgent(builder).research("   ", "Engineer", "Build systems"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void research_whenJobTitleIsNull_throwsIllegalArgumentException() {
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.defaultSystem(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(chatClient);

        assertThatThrownBy(() -> buildAgent(builder).research("Stripe", null, "Build systems"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void research_whenJobDescriptionIsNull_throwsIllegalArgumentException() {
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.defaultSystem(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(chatClient);

        assertThatThrownBy(() -> buildAgent(builder).research("Stripe", "Engineer", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
