package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import ch.uzh.ifi.hase.soprafs24.config.AnthropicConfig;

@SpringBootTest(classes = {AnthropicConfig.class, AnthropicRateLimiterService.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "anthropic.enabled=true",
    "anthropic.api-key=test-api-key",
    "anthropic.model=test-model",
    "anthropic.max-tokens=1000",
    "anthropic.temperature=0.7",
    "anthropic.rate-limit=10"
})
public class AnthropicRateLimiterServiceTest {

    private AnthropicRateLimiterService rateLimiterService;

    @MockBean
    private Semaphore semaphore;

    @MockBean
    private ScheduledExecutorService scheduler;

    private final int RATE_LIMIT = 5;

    @BeforeEach
    public void setup() {
        rateLimiterService = new AnthropicRateLimiterService(semaphore, scheduler, RATE_LIMIT);
    }

    @Test
    public void init_schedulesRateLimitReplenishment() {
        reset(scheduler);
        
        rateLimiterService.init();

        verify(scheduler).scheduleAtFixedRate(
            any(),
            eq(1L),
            eq(1L),
            eq(TimeUnit.MINUTES)
        );
    }

    @Test
    public void acquirePermit_success() throws InterruptedException {
        when(semaphore.tryAcquire(10, TimeUnit.SECONDS)).thenReturn(true);

        boolean result = rateLimiterService.acquirePermit();

        assertTrue(result);
        verify(semaphore).tryAcquire(10, TimeUnit.SECONDS);
    }

    @Test
    public void acquirePermit_failure() throws InterruptedException {
        when(semaphore.tryAcquire(10, TimeUnit.SECONDS)).thenReturn(false);

        boolean result = rateLimiterService.acquirePermit();

        assertFalse(result);
        verify(semaphore).tryAcquire(10, TimeUnit.SECONDS);
    }

    @Test
    public void acquirePermit_interrupted() throws InterruptedException {
        when(semaphore.tryAcquire(10, TimeUnit.SECONDS)).thenThrow(new InterruptedException());

        boolean result = rateLimiterService.acquirePermit();

        assertFalse(result);
        verify(semaphore).tryAcquire(10, TimeUnit.SECONDS);
    }

    @Test
    public void cleanup_shutsDownScheduler() {
        rateLimiterService.cleanup();

        verify(scheduler).shutdown();
    }

    private static <T> T any() {
        return org.mockito.ArgumentMatchers.any();
    }
    
    private static <T> T eq(T value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}