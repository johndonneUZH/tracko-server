package ch.uzh.ifi.hase.soprafs24.config;

import com.google.cloud.secretmanager.v1.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import javax.annotation.PostConstruct;
import java.io.IOException;

@Configuration
@ConditionalOnProperty(name = "anthropic.enabled", havingValue = "true", matchIfMissing = false)
public class AnthropicConfig {

    private static final String SECRET_NAME_PREFIX = "projects/sopra-fs25-group-46-server/secrets/";
    private static final String SECRET_VERSION_SUFFIX = "/versions/latest";

    @Value("${ANTHROPIC_API_KEY}")
    private String apiKey;

    @PostConstruct
    public void validateConfig() throws IOException {
        if (apiKey == null || apiKey.isEmpty()) {
            // Try getting from environment directly as fallback
            apiKey = System.getenv("ANTHROPIC_API_KEY");
            
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalStateException("Anthropic API key is not configured");
            }
        }

        // If the value is a secret path (starts with projects/), fetch the actual secret
        if (apiKey.startsWith(SECRET_NAME_PREFIX)) {
            this.apiKey = fetchSecretFromManager(apiKey);
        }
    }

    private String fetchSecretFromManager(String secretPath) throws IOException {
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            // Remove the prefix if present (just to be safe)
            String fullSecretName = secretPath;
            if (secretPath.startsWith(SECRET_NAME_PREFIX)) {
                fullSecretName = secretPath.substring(SECRET_NAME_PREFIX.length());
            }
            
            // Ensure it has the version suffix
            if (!fullSecretName.endsWith(SECRET_VERSION_SUFFIX)) {
                fullSecretName += SECRET_VERSION_SUFFIX;
            }

            SecretVersionName name = SecretVersionName.parse(fullSecretName);
            AccessSecretVersionResponse response = client.accessSecretVersion(name);
            return response.getPayload().getData().toStringUtf8();
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
