package com.example.bfh.service;

import com.example.bfh.logic.SqlSolver;
import com.example.bfh.model.GenerateWebhookResponse;
import com.example.bfh.model.SubmitRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class WebhookClient {
    private static final Logger log = LoggerFactory.getLogger(WebhookClient.class);

    private final WebClient webClient;

    @Value("${app.participant.name}")
    private String name;
    @Value("${app.participant.regno}")
    private String regNo;
    @Value("${app.participant.email}")
    private String email;

    public WebhookClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public void runFlow() {
        try {
            GenerateWebhookResponse resp = generateWebhook();
            if (resp == null) {
                log.error("generateWebhook returned null; aborting submit.");
                return;
            }
            log.info("Received webhook response: {}", resp);

            String finalQuery = SqlSolver.buildFinalQuery(regNo);
            log.info("Computed finalQuery for regNo {}: \n{}", regNo, finalQuery);

            submitFinalQuery(resp, finalQuery);
        } catch (Exception ex) {
            log.error("Error in runFlow: {}", ex.getMessage(), ex);
        }
    }

    private GenerateWebhookResponse generateWebhook() {
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        Map<String, Object> body = Map.of(
                "name", name,
                "regNo", regNo,
                "email", email
        );
        log.info("POST {}", url);
        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(GenerateWebhookResponse.class)
                .doOnError(err -> log.error("generateWebhook failed: {}", err.toString()))
                .onErrorResume(err -> Mono.empty())
                .block();
    }

    private void submitFinalQuery(GenerateWebhookResponse resp, String finalQuery) {
        String submitUrl = (resp.getWebhook() != null && !resp.getWebhook().isBlank())
                ? resp.getWebhook()
                : "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

        log.info("Submitting final query to {}", submitUrl);

        webClient.post()
                .uri(submitUrl)
                .header(HttpHeaders.AUTHORIZATION, resp.getAccessToken() != null ? resp.getAccessToken() : "")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new SubmitRequest(finalQuery))
                .retrieve()
                .toBodilessEntity()
                .doOnError(err -> log.error("submitFinalQuery failed: {}", err.toString()))
                .onErrorResume(err -> Mono.empty())
                .block();
        log.info("Submission attempted (check server for result).");
    }
}
