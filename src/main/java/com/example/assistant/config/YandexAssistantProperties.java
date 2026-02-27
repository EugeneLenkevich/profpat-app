package com.example.assistant.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Typed configuration for the Yandex Assistant API.
 * All values are read from application.yml under the "yandex.assistant" prefix.
 */
@Validated
@ConfigurationProperties(prefix = "yandex.assistant")
public record YandexAssistantProperties(

        @NotBlank(message = "yandex.assistant.api-key must not be blank")
        String apiKey,

        @NotBlank(message = "yandex.assistant.base-url must not be blank")
        String baseUrl,

        @NotBlank(message = "yandex.assistant.project must not be blank")
        String project,

        @NotBlank(message = "yandex.assistant.prompt-id must not be blank")
        String promptId
) {}
