package com.shop.onlineshopping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Sends email through Mailtrap's HTTPS Send API. This is the app's only email transport:
 * it talks HTTPS (port 443), so it works even on networks that block outbound SMTP.
 * Throws on any failure; EmailService catches it so a mail problem can never break the payment flow.
 */
@Service
public class MailtrapApiEmailTransport implements EmailTransport {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String endpoint;
    private final String token;
    private final String from;
    private final String fromName;

    public MailtrapApiEmailTransport(
            @Value("${mailtrap.api.base-url:https://sandbox.api.mailtrap.io}") String baseUrl,
            @Value("${mailtrap.api.inbox-id:}") String inboxId,
            @Value("${mailtrap.api.token:}") String token,
            @Value("${app.mail.from}") String from,
            @Value("${app.mail.from-name:everystore}") String fromName) {
        this.endpoint = baseUrl + "/api/send/" + inboxId;
        this.token = token;
        this.from = from;
        this.fromName = fromName;
    }

    @Override
    public void send(String to, String subject, String body) {
        try {
            Map<String, Object> payload = Map.of(
                    "from", Map.of("email", from, "name", fromName),
                    "to", List.of(Map.of("email", to)),
                    "subject", subject,
                    "text", body);
            String json = mapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(8))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException(
                        "Mailtrap API returned " + response.statusCode() + ": " + response.body());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            // Wrap checked exceptions (IOException, InterruptedException) so the caller has one type to catch.
            throw new IllegalStateException("Mailtrap API send failed: " + e.getMessage(), e);
        }
    }
}
