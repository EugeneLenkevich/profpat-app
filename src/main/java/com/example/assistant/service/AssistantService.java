package com.example.assistant.service;

import com.example.assistant.config.YandexAssistantProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

/**
 * Calls the Yandex Assistant REST API (OpenAI-compatible) and returns
 * the raw JSON response body as a String.
 *
 * <p>Request body sent to POST /responses:
 * <pre>
 * {
 *   "prompt": { "id": "&lt;promptId&gt;" },
 *   "input": "&lt;user message&gt;"
 * }
 * </pre>
 */
@Service
public class AssistantService {

    private final WebClient webClient;
    private final YandexAssistantProperties props;

    public AssistantService(WebClient yandexAssistantWebClient,
                            YandexAssistantProperties props) {
        this.webClient = yandexAssistantWebClient;
        this.props = props;
    }

    /**
     * Sends {@code input} to the Yandex Assistant API and returns the raw JSON response.
     *
     * @param input user message text
     * @return raw JSON string from Yandex API
     * @throws WebClientResponseException on 4xx/5xx responses
     */
    public String ask(String input) {
        Map<String, Object> body = Map.of(
                "prompt", Map.of("id", props.promptId()),
                "input", input
        );

        return webClient.post()
                .uri("/responses")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
