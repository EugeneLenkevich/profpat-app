package com.example.assistant.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Incoming request body from the browser: { "input": "user message" }
 */
public record AskRequest(
        @NotBlank(message = "input must not be blank")
        String input
) {}
