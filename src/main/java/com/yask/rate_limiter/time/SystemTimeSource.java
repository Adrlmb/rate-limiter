package com.yask.rate_limiter.time;

public class SystemTimeSource implements TimeSource {
    @Override
    public long nanoTime(){
        return System.nanoTime();
    }
}
