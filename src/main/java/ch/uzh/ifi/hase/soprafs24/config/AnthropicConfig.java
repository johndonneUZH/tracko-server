package ch.uzh.ifi.hase.soprafs24.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import javax.annotation.PostConstruct;

@Configuration
@ConditionalOnProperty(name = "anthropic.enabled", havingValue = "true", matchIfMissing = false)
public class AnthropicConfig {

    @Value("${ANTHROPIC_API_KEY}")
    private String apiKey;

    @PostConstruct
    public void validateConfig() {
        if (apiKey == null || apiKey.isEmpty()) {
            // Try getting from environment directly as fallback
            apiKey = System.getenv("ANTHROPIC_API_KEY");
            
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalStateException("Anthropic API key is not configured");
            }
        }
    }

    @Value("${anthropic.model:claude-3-7-sonnet-20250219}")
    private String model = "claude-3-7-sonnet-20250219";

    @Value("${anthropic.max-tokens:20000}")
    private int maxTokens = 20000;

    @Value("${anthropic.temperature:1.0}")
    private double temperature = 1.0;

    @Value("${anthropic.rate-limit:5}")
    private int rateLimit = 5;

    // Default constructor that explicitly sets defaults
    public AnthropicConfig() {
        // Defaults already set with field initialization
    }

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
