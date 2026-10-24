package com.yask.rate_limiter.domain;
import ch.qos.logback.core.joran.conditional.IfAction;

import java.time.Instant;

public class TokenBucket {
    private final double maxTokens;
    private double availableTokens;
    private Instant lastRefillTime;
    private final double refillRate;

    public TokenBucket(double maxTokens, double refillRate) {
        if(!Double.isFinite(maxTokens) || maxTokens <= 0) {
            throw new IllegalArgumentException("maxTokens must be a positive number");
        }
        if(!Double.isFinite(refillRate) || refillRate <= 0) {
            throw new IllegalArgumentException("refillRate must be a positive number");
        }

        this.maxTokens = maxTokens;
        this.availableTokens = maxTokens;
        this.lastRefillTime = Instant.now();
        this.refillRate = refillRate;
    }

    public double getMaxTokens() {
        return maxTokens;
    }

    public double getAvailableTokens() {
        return availableTokens;
    }

    public Instant getLastRefillTime() {
        return lastRefillTime;
    }

    public double getRefillRate() {
        return refillRate;
    }

    public boolean tryConsume(double tokenCost) {
        if (!Double.isFinite(tokenCost) || tokenCost <= 0) {
            throw new IllegalArgumentException("tokenCost must be a positive number");
        }
        if(tokenCost > maxTokens) {
            throw new IllegalArgumentException("tokenCost must not exceed maxTokens");
        }

        if(availableTokens >= tokenCost) {
            availableTokens-=tokenCost;
            return true;
        }
        return false;
    }
}