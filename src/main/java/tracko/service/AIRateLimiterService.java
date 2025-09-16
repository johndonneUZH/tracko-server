package tracko.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(name = "ai.enabled", havingValue = "true", matchIfMissing = false)
public class AIRateLimiterService {
    
    private final Semaphore rateLimiter;
    private final ScheduledExecutorService scheduler;
    private final int rateLimit;

    public AIRateLimiterService(Semaphore aiRateLimiter, 
                                       ScheduledExecutorService scheduledExecutorService,
                                        @Value("${ai.rate-limit:5}") int rateLimit) {
        this.rateLimiter = aiRateLimiter;
        this.scheduler = scheduledExecutorService;
        this.rateLimit = rateLimit;
    }
    
    @PostConstruct
    public void init() {
        // Replenish permits every minute
        this.scheduler.scheduleAtFixedRate(() -> {
            int permitsToRelease = rateLimit - rateLimiter.availablePermits();
            if (permitsToRelease > 0) {
                rateLimiter.release(permitsToRelease);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }
    
    public boolean acquirePermit() {
        try {
            return rateLimiter.tryAcquire(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    @PreDestroy
    public void cleanup() {
        scheduler.shutdown();
    }
}