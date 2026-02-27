package com.example.assistant.controller;

import com.example.assistant.dto.AskRequest;
import com.example.assistant.service.AssistantService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * REST endpoint consumed by the browser's fetch() call.
 *
 * <pre>
 * POST /api/ask
 * Content-Type: application/json
 * Body: { "input": "user message" }
 *
 * 200 OK  – raw JSON from Yandex API
 * 400     – validation error (blank input)
 * 502     – upstream Yandex API error
 * </pre>
 */
@RestController
@RequestMapping("/api")
public class AssistantController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping(value = "/ask",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> ask(@Valid @RequestBody AskRequest request) {
        try {
            String json = assistantService.ask(request.input());
            // Return the raw JSON string directly so the browser can parse it as-is
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json);
        } catch (WebClientResponseException ex) {
            // Propagate upstream error body to the browser for visibility
            return ResponseEntity.status(502)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"Upstream API error\",\"status\":" + ex.getStatusCode().value()
                            + ",\"detail\":" + ex.getResponseBodyAsString() + "}");
        }
    }
}
