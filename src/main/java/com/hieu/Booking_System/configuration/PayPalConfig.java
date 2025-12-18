package com.hieu.Booking_System.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;

import lombok.Getter;

@Configuration
@Getter
public class PayPalConfig {
    @Value("${spring.paypal.clientId}")
    private String clientId;

    @Value("${spring.paypal.clientSecret}")
    private String clientSecret;

    @Value("${spring.paypal.mode}")
    private String mode;

    @Value("${spring.paypal.success-url}")
    private String successUrl;

    @Value("${spring.paypal.cancel-url}")
    private String cancelUrl;

    @Bean
    public PayPalHttpClient payPalHttpClient() {
        PayPalEnvironment environment;
        if ("live".equalsIgnoreCase(mode)) {
            environment = new PayPalEnvironment.Live(clientId, clientSecret);
        } else {
            environment = new PayPalEnvironment.Sandbox(clientId, clientSecret);
        }
        return new PayPalHttpClient(environment);
    }
}
