package com.yask.rate_limiter.domain;

import com.yask.rate_limiter.time.FakeTimeSource;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class TokenBucketTest {

    @Test
    public void shouldConsumeTokensWhenBalanceIsSufficient(){
        FakeTimeSource clock = new FakeTimeSource();
        TokenBucket tokenBucket = new TokenBucket(5, 1, clock);

        boolean result = tokenBucket.tryConsume(3);
        assertTrue(result);
        assertEquals(2.0, tokenBucket.getAvailableTokens(), 0.000001);
    }

    @Test
    public void shouldRejectConsumptionWhenBalanceIsInsufficient(){
        FakeTimeSource clock = new FakeTimeSource();
        TokenBucket tokenBucket = new TokenBucket(5, 1, clock);

        boolean result = tokenBucket.tryConsume(4);
        assertTrue(result);
        assertFalse(tokenBucket.tryConsume(2));
        assertEquals(1, tokenBucket.getAvailableTokens(), 0.000001);
    }

    @Test
    public void shouldRefillTokensAfterElapsedTime(){
        FakeTimeSource clock = new FakeTimeSource();
        TokenBucket tokenBucket = new TokenBucket(5, 1, clock);

        assertTrue(tokenBucket.tryConsume(4));
        clock.advance(Duration.ofSeconds(2));
        assertTrue(tokenBucket.tryConsume(1));
        assertEquals(2, tokenBucket.getAvailableTokens(),0.000001);
    }

    @Test
    public void shouldNotExceedMaximumCapacity(){
        FakeTimeSource clock = new FakeTimeSource();
        TokenBucket tokenBucket = new TokenBucket(5, 2, clock);

        assertTrue(tokenBucket.tryConsume(4));
        clock.advance(Duration.ofSeconds(10));
        assertTrue(tokenBucket.tryConsume(1));
        assertEquals(4, tokenBucket.getAvailableTokens(), 0.000001);
    }

    @Test
    public void shouldRefillFractionalTokens(){
        FakeTimeSource clock = new FakeTimeSource();
        TokenBucket tokenBucket = new TokenBucket(5, 2, clock);

        assertTrue(tokenBucket.tryConsume(4));
        clock.advance(Duration.ofMillis(250));
        assertTrue(tokenBucket.tryConsume(1));
        assertEquals(0.5, tokenBucket.getAvailableTokens(), 0.000001);
    }
}
