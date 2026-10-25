package com.yask.rate_limiter.time;

import java.time.Duration;
import java.util.Objects;

public class FakeTimeSource implements TimeSource{
    private long currentNanos = 0;

    @Override
    public long nanoTime(){
        return currentNanos;
    }

    public void advance(Duration duration){
        Objects.requireNonNull(duration,"duration must not be null");

        if(duration.isNegative()){
            throw new IllegalArgumentException("duration must not be a negative number");
        }

        currentNanos = Math.addExact(currentNanos, duration.toNanos());
    }

}
