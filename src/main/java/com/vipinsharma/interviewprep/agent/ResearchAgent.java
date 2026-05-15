package com.vipinsharma.interviewprep.agent;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vipinsharma.interviewprep.agent.tools.TavilySearchTool;
import com.vipinsharma.interviewprep.dto.CompanyBrief;

@Component
public class ResearchAgent {

    private static final Logger log = LoggerFactory.getLogger(ResearchAgent.class);

    private static final String SYSTEM_PROMPT =
            """
            You are an expert company researcher. Given a company name, job title, and job description,
            research the company thoroughly and return ONLY a JSON object with the following fields:
            - companySummary: A concise summary of what the company does (string)
            - techStack: Technologies the company is known for using (array of strings)
            - cultureSignals: Key aspects of the company's engineering culture and values (string)
            - recentNews: Notable recent developments, product launches, or news about the company (string)
            - likelyInterviewTopics: Topics likely to come up in a technical interview at this company (array of strings)

            Return ONLY the JSON object. No markdown, no preamble, no explanation.
            """;

    private final ChatClient chatClient;
    private final TavilySearchTool tavilySearchTool;
    private final ObjectMapper objectMapper;

    public ResearchAgent(ChatClient.Builder chatClientBuilder, TavilySearchTool tavilySearchTool) {
        this.chatClient = chatClientBuilder.defaultSystem(SYSTEM_PROMPT).build();
        this.tavilySearchTool = tavilySearchTool;
        this.objectMapper = new ObjectMapper();
    }

    public CompanyBrief research(String companyName, String jobTitle, String jobDescription) {
        if (!StringUtils.hasText(companyName)) {
            throw new IllegalArgumentException("companyName must not be null or blank");
        }
        if (!StringUtils.hasText(jobTitle)) {
            throw new IllegalArgumentException("jobTitle must not be null or blank");
        }
        if (!StringUtils.hasText(jobDescription)) {
            throw new IllegalArgumentException("jobDescription must not be null or blank");
        }

        String userPrompt = buildUserPrompt(companyName, jobTitle, jobDescription);

        String responseContent = chatClient
                .prompt(userPrompt)
                .tools(tavilySearchTool)
                .call()
                .content();

        return parseCompanyBrief(companyName, responseContent);
    }

    private String buildUserPrompt(String companyName, String jobTitle, String jobDescription) {
        return String.format(
                "Research the company '%s' for a candidate applying for the role of '%s'. "
                        + "Job description: %s",
                companyName,
                jobTitle,
                jobDescription);
    }

    private static String stripMarkdownFences(String text) {
        if (text == null) return null;
        return text.replaceAll("(?s)^```[a-zA-Z]*\\n?", "").replaceAll("(?s)```$", "").strip();
    }

    private CompanyBrief parseCompanyBrief(String companyName, String json) {
        try {
            ParsedBrief parsed = objectMapper.readValue(stripMarkdownFences(json), ParsedBrief.class);
            return new CompanyBrief(
                    companyName,
                    parsed.companySummary(),
                    parsed.techStack(),
                    parsed.cultureSignals(),
                    parsed.recentNews(),
                    parsed.likelyInterviewTopics());
        } catch (Exception e) {
            log.warn("Failed to parse LLM response as CompanyBrief JSON for company '{}': {}",
                    companyName, e.getMessage());
            return new CompanyBrief(companyName, null, null, null, null, null);
        }
    }

    private record ParsedBrief(
            String companySummary,
            List<String> techStack,
            String cultureSignals,
            String recentNews,
            List<String> likelyInterviewTopics) {}
}
