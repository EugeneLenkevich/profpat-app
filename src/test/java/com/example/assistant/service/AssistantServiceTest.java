package com.example.assistant.service;

import com.example.assistant.config.YandexAssistantProperties;
import com.example.assistant.config.WebClientConfig;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link AssistantService} using {@link MockWebServer}
 * to intercept outbound HTTP calls without hitting the real Yandex API.
 */
class AssistantServiceTest {

    private MockWebServer mockWebServer;
    private AssistantService assistantService;

    private static final String FAKE_PROMPT_ID = "test-prompt-id";
    private static final String FAKE_PROJECT   = "test-project";
    private static final String FAKE_API_KEY   = "test-api-key";

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/v1").toString();

        YandexAssistantProperties props = new YandexAssistantProperties(
                FAKE_API_KEY,
                baseUrl,
                FAKE_PROJECT,
                FAKE_PROMPT_ID
        );

        WebClientConfig config = new WebClientConfig();
        WebClient webClient = config.yandexAssistantWebClient(props);

        assistantService = new AssistantService(webClient, props);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void ask_sendsCorrectRequestAndReturnsBody() throws InterruptedException {
        // Arrange
        String fakeResponse = """
                {
                  "id": "resp-001",
                  "output": "Hello from Yandex!"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(fakeResponse));

        // Act
        String result = assistantService.ask("Hello");

        // Assert – response body is returned as-is
        assertThat(result).contains("resp-001");
        assertThat(result).contains("Hello from Yandex!");

        // Assert – correct HTTP request was sent
        RecordedRequest recorded = mockWebServer.takeRequest();
        assertThat(recorded.getMethod()).isEqualTo("POST");
        assertThat(recorded.getPath()).endsWith("/responses");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer " + FAKE_API_KEY);
        assertThat(recorded.getHeader("Content-Type")).contains("application/json");

        String requestBody = recorded.getBody().readUtf8();
        assertThat(requestBody).contains(FAKE_PROMPT_ID);
        assertThat(requestBody).contains("Hello");
    }

    @Test
    void ask_includesPromptIdInRequestBody() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"id\":\"r2\"}"));

        assistantService.ask("test input");

        RecordedRequest recorded = mockWebServer.takeRequest();
        String body = recorded.getBody().readUtf8();

        assertThat(body).contains("\"prompt\"");
        assertThat(body).contains(FAKE_PROMPT_ID);
        assertThat(body).contains("\"input\"");
        assertThat(body).contains("test input");
    }
}
