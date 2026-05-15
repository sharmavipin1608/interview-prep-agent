package com.vipinsharma.interviewprep.agent;

import java.util.Collections;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.vipinsharma.interviewprep.dto.CompanyBrief;

@Component
public class InterviewerAgent {

    private static final String SYSTEM_PROMPT_TEMPLATE =
            """
            You are a senior technical interviewer at %s.
            Company context: %s
            Tech stack: %s
            Culture: %s
            Job description: %s
            Known weak areas to probe: %s

            Conduct a realistic technical interview. Ask ONE focused question at a time.
            Follow up based on the candidate's answer. Adapt difficulty and topics to the weak areas listed.
            Stay in character as an interviewer throughout the session.
            """;

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public InterviewerAgent(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
        this.chatClient = chatClientBuilder.build();
        this.chatMemory = chatMemory;
    }

    public String chat(
            String sessionId,
            CompanyBrief companyBrief,
            String jobDescription,
            List<String> weakAreas,
            String userMessage) {

        if (!StringUtils.hasText(sessionId)) {
            throw new IllegalArgumentException("sessionId must not be null or blank");
        }
        if (companyBrief == null) {
            throw new IllegalArgumentException("companyBrief must not be null");
        }
        if (!StringUtils.hasText(jobDescription)) {
            throw new IllegalArgumentException("jobDescription must not be null or blank");
        }
        if (!StringUtils.hasText(userMessage)) {
            throw new IllegalArgumentException("userMessage must not be null or blank");
        }

        List<String> areas = (weakAreas != null) ? weakAreas : Collections.emptyList();
        String systemPrompt = buildSystemPrompt(companyBrief, jobDescription, areas);

        MessageChatMemoryAdvisor advisor = MessageChatMemoryAdvisor.builder(chatMemory)
                .conversationId(sessionId)
                .build();

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .advisors(advisor)
                .call()
                .content();
    }

    private String buildSystemPrompt(CompanyBrief companyBrief, String jobDescription, List<String> weakAreas) {
        return String.format(
                SYSTEM_PROMPT_TEMPLATE,
                companyBrief.companyName(),
                companyBrief.companySummary(),
                companyBrief.techStack(),
                companyBrief.cultureSignals(),
                jobDescription,
                String.join(", ", weakAreas));
    }
}
