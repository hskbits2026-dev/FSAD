package com.shop.onlineshopping.service;

/**
 * How an email actually leaves the app. We have two implementations:
 *  - {@link SmtpEmailTransport}        — classic SMTP (works where outbound SMTP is allowed)
 *  - {@link MailtrapApiEmailTransport} — Mailtrap's HTTPS Send API (works even where SMTP is firewalled)
 * Which one is active is chosen by the {@code app.mail.transport} property, so the rest of the
 * app ({@link EmailService}) never cares how the mail is sent — same idea as {@code StripeGateway}.
 */
public interface EmailTransport {
    void send(String to, String subject, String body);
}
