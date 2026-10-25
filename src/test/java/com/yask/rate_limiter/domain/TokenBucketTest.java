package com.yask.rate_limiter.domain;

import com.yask.rate_limiter.time.FakeTimeSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class TokenBucketTest {

    @Test
    void shouldConsumeTokensWhenBalanceIsSufficient(){
        FakeTimeSource clock = new FakeTimeSource();
        TokenBucket tokenBucket = new TokenBucket(5, 1, clock);

        boolean result = tokenBucket.tryConsume(3);
        assertTrue(result);
        assertEquals(2.0, tokenBucket.getAvailableTokens(), 0.000001);
    }

    @Test
    void shouldRejectConsumptionWhenBalanceIsInsufficient(){
        FakeTimeSource clock = new FakeTimeSource();
        TokenBucket tokenBucket = new TokenBucket(5, 1, clock);

        boolean result = tokenBucket.tryConsume(4);
        assertTrue(result);
        assertFalse(tokenBucket.tryConsume(2));
        assertEquals(1, tokenBucket.getAvailableTokens(), 0.000001);
    }

    @Test
    void shouldRefillTokensAfterElapsedTime(){
        FakeTimeSource clock = new FakeTimeSource();
        TokenBucket tokenBucket = new TokenBucket(5, 1, clock);

        assertTrue(tokenBucket.tryConsume(4));
        clock.advance(Duration.ofSeconds(2));
        assertTrue(tokenBucket.tryConsume(1));
        assertEquals(2, tokenBucket.getAvailableTokens(),0.000001);
    }

    @Test
    void shouldNotExceedMaximumCapacity(){
        FakeTimeSource clock = new FakeTimeSource();
        TokenBucket tokenBucket = new TokenBucket(5, 2, clock);

        assertTrue(tokenBucket.tryConsume(4));
        clock.advance(Duration.ofSeconds(10));
        assertTrue(tokenBucket.tryConsume(1));
        assertEquals(4, tokenBucket.getAvailableTokens(), 0.000001);
    }

    @Test
    void shouldRefillFractionalTokens(){
        FakeTimeSource clock = new FakeTimeSource();
        TokenBucket tokenBucket = new TokenBucket(5, 2, clock);

        assertTrue(tokenBucket.tryConsume(4));
        clock.advance(Duration.ofMillis(250));
        assertTrue(tokenBucket.tryConsume(1));
        assertEquals(0.5, tokenBucket.getAvailableTokens(), 0.000001);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, 0, Double.NaN, Double.POSITIVE_INFINITY, 6})
    void shouldRejectInvalidTokensCost(double tokenCost){
        FakeTimeSource clock = new FakeTimeSource();
        TokenBucket tokenBucket = new TokenBucket(5, 1, clock);
        assertThrows(IllegalArgumentException.class, () ->
                tokenBucket.tryConsume(tokenCost));
    }

    @Test
    void shouldHandleConcurrentConsumption() throws Exception {
        FakeTimeSource clock = new FakeTimeSource();
        TokenBucket tokenBucket = new TokenBucket(1, 1, clock);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        try {
            Future<Boolean> result1 = executor.submit(() -> {
                readyLatch.countDown();
                startLatch.await();
                return tokenBucket.tryConsume(1);
            });
            Future<Boolean> result2 = executor.submit(() -> {
                readyLatch.countDown();
                startLatch.await();
                return tokenBucket.tryConsume(1);
            });

            assertTrue(readyLatch.await(2, TimeUnit.SECONDS));
            startLatch.countDown();
            boolean consumed1 = result1.get(2, TimeUnit.SECONDS);
            boolean consumed2 = result2.get(2, TimeUnit.SECONDS);

            assertTrue(consumed1 ^ consumed2);
            assertEquals(0, tokenBucket.getAvailableTokens(), 0.000001);
        } finally {
            startLatch.countDown();
            executor.shutdownNow();
        }
    }
}
