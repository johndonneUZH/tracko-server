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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@ConditionalOnProperty(name = "anthropic.enabled", havingValue = "true", matchIfMissing = false)
public class AnthropicConfig {

    private static final Logger logger = LoggerFactory.getLogger(AnthropicConfig.class);
    private static final String SECRET_NAME_PREFIX = "projects/sopra-fs25-group-46-server/secrets/";
    private static final String SECRET_VERSION_SUFFIX = "/versions/latest";

    @Value("${ANTHROPIC_API_KEY}")
    private String apiKey;

    @PostConstruct
    public void validateConfig() throws IOException {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            apiKey = System.getenv("ANTHROPIC_API_KEY");
            
            if (apiKey == null || apiKey.trim().isEmpty()) {
                logger.warn("Anthropic API key is not configured - Anthropic features will be disabled");
                return;  
            }
        }
        
        if (apiKey.startsWith(SECRET_NAME_PREFIX)) {
            try {
                this.apiKey = fetchSecretFromManager(apiKey);
            } catch (IOException e) {
                logger.error("Failed to fetch secret from manager", e);
                throw new IllegalStateException("Failed to fetch Anthropic API key from secret manager", e);
            }
        }
    }

    private String fetchSecretFromManager(String secretPath) throws IOException {
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            // Ensure the path is properly formatted
            if (!secretPath.endsWith(SECRET_VERSION_SUFFIX)) {
                secretPath += SECRET_VERSION_SUFFIX;
            }
            
            SecretVersionName name = SecretVersionName.parse(secretPath);
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