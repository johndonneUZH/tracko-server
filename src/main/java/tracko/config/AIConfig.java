package tracko.config;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;

@Configuration
@ConditionalOnProperty(name = "ai.enabled", havingValue = "true", matchIfMissing = false)
public class AIConfig {

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.model:gemini-2.0-flash-exp}")
    private String model;

    @Value("${ai.rate-limit:5}")
    private int rateLimit;

    @Bean
    public Client genAiClient() {
        return Client.builder()
                .apiKey(apiKey)
                .build();
    }

    @Bean
    public Semaphore aiRateLimiter() {
        return new Semaphore(rateLimit);
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(1);
    }

    public String getModel() {
        return model;
    }
}
