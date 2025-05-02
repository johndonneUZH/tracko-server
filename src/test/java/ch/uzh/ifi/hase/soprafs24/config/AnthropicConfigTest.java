package ch.uzh.ifi.hase.soprafs24.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {AnthropicConfig.class})
@ActiveProfiles("test")
public class AnthropicConfigTest {

    @DynamicPropertySource
    static void setProps(DynamicPropertyRegistry registry) {
        // Simulates environment variable
        registry.add("ANTHROPIC_API_KEY", () -> "test-api-key");

        // Other props
        registry.add("anthropic.model", () -> "test-model");
        registry.add("anthropic.max-tokens", () -> "1000");
        registry.add("anthropic.temperature", () -> "0.7");
        registry.add("anthropic.rate-limit", () -> "10");
        registry.add("anthropic.enabled", () -> "true");
    }

    @Autowired
    private AnthropicConfig anthropicConfig;

    @Test
    public void testPropertiesInjection() {
        assertEquals("test-api-key", anthropicConfig.getApiKey());
        assertEquals("test-model", anthropicConfig.getModel());
        assertEquals(1000, anthropicConfig.getMaxTokens());
        assertEquals(0.7, anthropicConfig.getTemperature());
        assertEquals(10, anthropicConfig.getRateLimit());
    }

    @Test
    public void testDefaultValues() {
        AnthropicConfig config = new AnthropicConfig();
        ReflectionTestUtils.setField(config, "apiKey", "new-api-key");

        assertEquals("new-api-key", config.getApiKey());
        assertEquals("claude-3-7-sonnet-20250219", config.getModel());
        assertEquals(20000, config.getMaxTokens());
        assertEquals(1.0, config.getTemperature());
        assertEquals(5, config.getRateLimit());
    }

    @Test
    public void testRestTemplateBean() {
        assertNotNull(anthropicConfig.restTemplate());
    }

    @Test
    public void testAnthropicRateLimiterBean() {
        Semaphore rateLimiter = anthropicConfig.anthropicRateLimiter();
        assertNotNull(rateLimiter);
        assertEquals(10, rateLimiter.availablePermits());
    }

    @Test
    public void testScheduledExecutorServiceBean() {
        ScheduledExecutorService service = anthropicConfig.scheduledExecutorService();
        assertNotNull(service);
        assertFalse(service.isShutdown());
        service.shutdown();
    }
}
