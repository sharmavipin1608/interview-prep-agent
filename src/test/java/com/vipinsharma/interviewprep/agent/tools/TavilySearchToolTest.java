package com.vipinsharma.interviewprep.agent.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.vipinsharma.interviewprep.agent.tools.TavilySearchTool;

class TavilySearchToolTest {

    @SuppressWarnings("unchecked")
    private RestClient createMockRestClient(String fakeResponse) {
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);
        when(responseSpec.body(String.class)).thenReturn(fakeResponse);

        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        when(requestBodySpec.header(anyString(), any(String[].class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);

        RestClient restClient = mock(RestClient.class);
        when(restClient.post()).thenReturn(requestBodyUriSpec);

        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(restClient);

        return builder.build();
    }

    @Test
    void search_withValidQuery_returnsJsonContainingTitle() {
        String fakeResponse = """
                {"results":[
                  {"title":"Stripe Engineering","url":"https://stripe.com/blog","content":"Stripe uses Java, Ruby, Go"},
                  {"title":"Stripe Culture","url":"https://stripe.com/jobs","content":"Focus on impact and ownership"}
                ]}
                """;

        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);
        when(responseSpec.body(String.class)).thenReturn(fakeResponse);

        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        when(requestBodySpec.header(anyString(), any(String[].class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);

        RestClient restClient = mock(RestClient.class);
        when(restClient.post()).thenReturn(requestBodyUriSpec);

        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(restClient);

        TavilySearchTool tool = new TavilySearchTool(builder, "test-api-key");
        String result = tool.search("Stripe engineering culture");

        assertThat(result).contains("Stripe Engineering");
    }

    @Test
    void search_withValidQuery_returnsJsonContainingContent() {
        String fakeResponse = """
                {"results":[
                  {"title":"Stripe Engineering","url":"https://stripe.com/blog","content":"Stripe uses Java, Ruby, Go"},
                  {"title":"Stripe Culture","url":"https://stripe.com/jobs","content":"Focus on impact and ownership"}
                ]}
                """;

        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);
        when(responseSpec.body(String.class)).thenReturn(fakeResponse);

        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        when(requestBodySpec.header(anyString(), any(String[].class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);

        RestClient restClient = mock(RestClient.class);
        when(restClient.post()).thenReturn(requestBodyUriSpec);

        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(restClient);

        TavilySearchTool tool = new TavilySearchTool(builder, "test-api-key");
        String result = tool.search("Stripe engineering culture");

        assertThat(result).contains("Java, Ruby, Go");
    }

    @Test
    void search_whenRestClientReturnsNull_returnsFallbackMessage() {
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);
        when(responseSpec.body(String.class)).thenReturn(null);

        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        when(requestBodySpec.header(anyString(), any(String[].class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);

        RestClient restClient = mock(RestClient.class);
        when(restClient.post()).thenReturn(requestBodyUriSpec);

        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(restClient);

        TavilySearchTool tool = new TavilySearchTool(builder, "test-api-key");
        String result = tool.search("Java interviews");

        assertThat(result).isEqualTo("No results found for: Java interviews");
    }

    @Test
    void search_withEmptyResultsArray_returnsParsedJsonWithEmptyResults() {
        String fakeResponse = """
                {"results":[]}
                """;

        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);
        when(responseSpec.body(String.class)).thenReturn(fakeResponse);

        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        when(requestBodySpec.header(anyString(), any(String[].class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);

        RestClient restClient = mock(RestClient.class);
        when(restClient.post()).thenReturn(requestBodyUriSpec);

        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(restClient);

        TavilySearchTool tool = new TavilySearchTool(builder, "test-api-key");
        String result = tool.search("nonexistent topic");

        assertThat(result).contains("\"results\":[]");
    }

    @Test
    void search_withBlankQuery_throwsIllegalArgumentException() {
        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(mock(RestClient.class));

        TavilySearchTool tool = new TavilySearchTool(builder, "test-api-key");

        assertThatThrownBy(() -> tool.search(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must not be null or blank");
    }

    @Test
    void search_withNullQuery_throwsIllegalArgumentException() {
        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(mock(RestClient.class));

        TavilySearchTool tool = new TavilySearchTool(builder, "test-api-key");

        assertThatThrownBy(() -> tool.search(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must not be null or blank");
    }

    @Test
    void search_withMalformedJsonResponse_returnsResponseAsIs() {
        String fakeResponse = "{invalid json";

        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);
        when(responseSpec.body(String.class)).thenReturn(fakeResponse);

        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        when(requestBodySpec.header(anyString(), any(String[].class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);

        RestClient restClient = mock(RestClient.class);
        when(restClient.post()).thenReturn(requestBodyUriSpec);

        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(restClient);

        TavilySearchTool tool = new TavilySearchTool(builder, "test-api-key");
        String result = tool.search("test query");

        assertThat(result).isEqualTo("{invalid json");
    }
}
