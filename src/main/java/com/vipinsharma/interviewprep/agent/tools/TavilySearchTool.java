package com.vipinsharma.interviewprep.agent.tools;

import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TavilySearchTool {

    private final RestClient restClient;
    private final String apiKey;

    public TavilySearchTool(
            RestClient.Builder restClientBuilder,
            @Value("${tavily.api-key}") String apiKey) {
        this.restClient = restClientBuilder.baseUrl("https://api.tavily.com").build();
        this.apiKey = apiKey;
    }

    @Tool(description =
            "Search the web for information about companies, technologies, engineering culture, "
            + "and job interview topics")
    public String search(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query must not be null or blank");
        }
        if (query.length() > 500) {
            throw new IllegalArgumentException("Search query exceeds maximum length of 500 characters");
        }

        Map<String, Object> requestBody = Map.of(
            "api_key", apiKey,
            "query", query,
            "search_depth", "basic",
            "max_results", 5
        );

        String response = restClient.post()
                .uri("/search")
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(String.class);

        return response != null ? response : "No results found for: " + query;
    }
}
