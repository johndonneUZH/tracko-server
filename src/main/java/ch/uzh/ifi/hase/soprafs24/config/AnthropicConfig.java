package ch.uzh.ifi.hase.soprafs24.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;

// @Configuration
public class AnthropicConfig {

    @Value("${anthropic.api-key}")
    private String apiKey;

    @Value("${anthropic.model:claude-3-7-sonnet-20250219}")
    private String model;

    @Value("${anthropic.max-tokens:20000}")
    private int maxTokens;

    @Value("${anthropic.temperature:1.0}")
    private double temperature;

    @Value("${anthropic.rate-limit:5}")
    private int rateLimit;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Semaphore anthropicRateLimiter() {
        return new Semaphore(rateLimit);
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(1);
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getRateLimit() {
        return rateLimit;
    }
}