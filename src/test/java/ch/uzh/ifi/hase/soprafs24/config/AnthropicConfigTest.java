// package ch.uzh.ifi.hase.soprafs24.config;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.context.TestConfiguration;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Import;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.context.TestPropertySource;
// import org.springframework.test.util.ReflectionTestUtils;
// import org.springframework.web.client.RestTemplate;

// import java.util.concurrent.ScheduledExecutorService;
// import java.util.concurrent.Semaphore;

// import static org.junit.jupiter.api.Assertions.*;

// @SpringBootTest
// @ActiveProfiles("test")
// @TestPropertySource(properties = {
//     "anthropic.api-key=test-api-key",
//     "anthropic.model=test-model",
//     "anthropic.max-tokens=1000",
//     "anthropic.temperature=0.7",
//     "anthropic.rate-limit=10"
// })
// public class AnthropicConfigTest {

//     @TestConfiguration
//     @Import(AnthropicConfig.class)
//     static class TestConfig {
//         // Empty test configuration to import AnthropicConfig
//     }

//     @Autowired
//     private AnthropicConfig anthropicConfig;

//     @Test
//     public void testPropertiesInjection() {
//         // Test that properties are correctly injected
//         assertEquals("test-api-key", anthropicConfig.getApiKey());
//         assertEquals("test-model", anthropicConfig.getModel());
//         assertEquals(1000, anthropicConfig.getMaxTokens());
//         assertEquals(0.7, anthropicConfig.getTemperature());
//         assertEquals(10, anthropicConfig.getRateLimit());
//     }

//     @Test
//     public void testDefaultValues() {
//         // Create a new instance to test default values
//         AnthropicConfig config = new AnthropicConfig();
        
//         // Set only api-key, let other fields use defaults
//         ReflectionTestUtils.setField(config, "apiKey", "new-api-key");
        
//         assertEquals("new-api-key", config.getApiKey());
//         assertEquals("claude-3-7-sonnet-20250219", config.getModel()); // Default value
//         assertEquals(20000, config.getMaxTokens()); // Default value
//         assertEquals(1.0, config.getTemperature()); // Default value
//         assertEquals(5, config.getRateLimit()); // Default value
//     }

//     @Test
//     public void testRestTemplateBean() {
//         // Test that RestTemplate bean is created
//         RestTemplate restTemplate = anthropicConfig.restTemplate();
//         assertNotNull(restTemplate);
//     }

//     @Test
//     public void testAnthropicRateLimiterBean() {
//         // Test that Semaphore bean is created with correct permits
//         Semaphore rateLimiter = anthropicConfig.anthropicRateLimiter();
//         assertNotNull(rateLimiter);
//         assertEquals(10, rateLimiter.availablePermits());
//     }

//     @Test
//     public void testScheduledExecutorServiceBean() {
//         // Test that ScheduledExecutorService bean is created
//         ScheduledExecutorService service = anthropicConfig.scheduledExecutorService();
//         assertNotNull(service);
//         assertFalse(service.isShutdown());
        
//         // Clean up
//         service.shutdown();
//     }
// }