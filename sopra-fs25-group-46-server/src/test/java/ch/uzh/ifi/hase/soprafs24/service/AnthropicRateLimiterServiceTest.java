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
import org.mockito.Mock;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import ch.uzh.ifi.hase.soprafs24.config.MongoTestConfig;
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
        // Reset the mock to clear previous invocations
        reset(scheduler);
        
        // when
        rateLimiterService.init();

        // then
        verify(scheduler).scheduleAtFixedRate(
            any(),
            eq(1L),
            eq(1L),
            eq(TimeUnit.MINUTES)
        );
    }

    @Test
    public void acquirePermit_success() throws InterruptedException {
        // given
        when(semaphore.tryAcquire(10, TimeUnit.SECONDS)).thenReturn(true);

        // when
        boolean result = rateLimiterService.acquirePermit();

        // then
        assertTrue(result);
        verify(semaphore).tryAcquire(10, TimeUnit.SECONDS);
    }

    @Test
    public void acquirePermit_failure() throws InterruptedException {
        // given
        when(semaphore.tryAcquire(10, TimeUnit.SECONDS)).thenReturn(false);

        // when
        boolean result = rateLimiterService.acquirePermit();

        // then
        assertFalse(result);
        verify(semaphore).tryAcquire(10, TimeUnit.SECONDS);
    }

    @Test
    public void acquirePermit_interrupted() throws InterruptedException {
        // given
        when(semaphore.tryAcquire(10, TimeUnit.SECONDS)).thenThrow(new InterruptedException());

        // when
        boolean result = rateLimiterService.acquirePermit();

        // then
        assertFalse(result);
        verify(semaphore).tryAcquire(10, TimeUnit.SECONDS);
    }

    @Test
    public void cleanup_shutsDownScheduler() {
        // when
        rateLimiterService.cleanup();

        // then
        verify(scheduler).shutdown();
    }

    // Help Mockito recognize 'any' matcher
    private static <T> T any() {
        return org.mockito.ArgumentMatchers.any();
    }
    
    // Help Mockito recognize 'eq' matcher
    private static <T> T eq(T value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}