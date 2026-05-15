package com.vipinsharma.interviewprep.agent;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vipinsharma.interviewprep.dto.SessionFeedback;

@Component
public class CoachAgent {

    private static final Logger log = LoggerFactory.getLogger(CoachAgent.class);

    private static final String SYSTEM_PROMPT =
            """
            You are an expert technical interview coach. Evaluate the interview transcript and return ONLY a JSON object with these fields:
            - overallScore: integer 1-5 (1=poor, 5=excellent)
            - weakAreas: array of strings (top weak areas identified, max 3)
            - improvementSuggestions: array of strings (specific, actionable suggestions)
            - detailedFeedback: string (paragraph-length overall assessment)

            Return ONLY the JSON object. No markdown, no preamble.
            """;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public CoachAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.defaultSystem(SYSTEM_PROMPT).build();
        this.objectMapper = new ObjectMapper();
    }

    public SessionFeedback evaluate(UUID sessionId, String transcript, List<String> weakAreaHistory) {
        if (sessionId == null) {
            throw new IllegalArgumentException("sessionId must not be null");
        }
        if (!StringUtils.hasText(transcript)) {
            throw new IllegalArgumentException("transcript must not be null or blank");
        }

        List<String> history = weakAreaHistory != null ? weakAreaHistory : Collections.emptyList();
        String userPrompt = buildUserPrompt(transcript, history);

        String response = chatClient.prompt(userPrompt).call().content();

        return parseFeedback(sessionId, response);
    }

    private String buildUserPrompt(String transcript, List<String> weakAreaHistory) {
        String historyText = weakAreaHistory.isEmpty() ? "none" : String.join(", ", weakAreaHistory);
        return String.format(
                """
                Session transcript:
                %s

                Historical weak areas to watch for:
                %s

                Evaluate the candidate's performance and provide structured feedback.""",
                transcript,
                historyText);
    }

    private SessionFeedback parseFeedback(UUID sessionId, String json) {
        try {
            ParsedFeedback parsed = objectMapper.readValue(json, ParsedFeedback.class);
            return new SessionFeedback(
                    sessionId,
                    parsed.overallScore(),
                    parsed.weakAreas(),
                    parsed.improvementSuggestions(),
                    parsed.detailedFeedback());
        } catch (Exception e) {
            log.warn("Failed to parse LLM response as SessionFeedback JSON for session '{}': {}",
                    sessionId, e.getMessage());
            return new SessionFeedback(
                    sessionId,
                    0,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    "Evaluation unavailable");
        }
    }

    private record ParsedFeedback(
            int overallScore,
            List<String> weakAreas,
            List<String> improvementSuggestions,
            String detailedFeedback) {}
}
