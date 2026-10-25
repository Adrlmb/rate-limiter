package com.yask.rate_limiter.domain;
import com.yask.rate_limiter.time.TimeSource;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class TokenBucket {
    private final double maxTokens;
    private double availableTokens;
    private long lastRefillNanos;
    private final double refillRate;
    private final TimeSource timeSource;

    public TokenBucket(double maxTokens, double refillRate, TimeSource timeSource) {
        if(!Double.isFinite(maxTokens) || maxTokens <= 0) {
            throw new IllegalArgumentException("maxTokens must be a positive number");
        }
        if(!Double.isFinite(refillRate) || refillRate <= 0) {
            throw new IllegalArgumentException("refillRate must be a positive number");
        }

        this.maxTokens = maxTokens;
        this.availableTokens = maxTokens;
        this.timeSource = Objects.requireNonNull(timeSource,  "timeSource must not be null");
        this.lastRefillNanos = timeSource.nanoTime();
        this.refillRate = refillRate;

    }

    public double getMaxTokens() {
        return maxTokens;
    }

    public synchronized double getAvailableTokens() {
        return availableTokens;
    }

    public synchronized long getLastRefillNanos() {
        return lastRefillNanos;
    }

    public double getRefillRate() {
        return refillRate;
    }

    public synchronized boolean tryConsume(double tokenCost) {
        if (!Double.isFinite(tokenCost) || tokenCost <= 0) {
            throw new IllegalArgumentException("tokenCost must be a positive number");
        }
        if(tokenCost > maxTokens) {
            throw new IllegalArgumentException("tokenCost must not exceed maxTokens");
        }

        refill();

        if(availableTokens >= tokenCost) {
            availableTokens-=tokenCost;
            return true;
        }
        return false;
    }

    private void refill(){
        long now = timeSource.nanoTime();
        long elapsedNanos = now - lastRefillNanos;

        if(elapsedNanos <= 0){
            return;
        }

        double elapsedSeconds = elapsedNanos/ 1_000_000_000.0;
        double tokensToAdd = elapsedSeconds * refillRate;

        availableTokens = Math.min(maxTokens, availableTokens + tokensToAdd);
        lastRefillNanos = now;
    }
}