package com.gmail.altakey.mint.timer.util;

public class TimerReader {
    public long seconds;
    public long minutes;
    public long remaining;

    public TimerReader(long remaining) {
        remaining = ((long)Math.ceil(remaining / 1000.0)) * 1000;

        seconds = remaining / 1000 % 60;
        minutes = remaining / 60000;
        this.remaining = remaining;
    }

    public long getElapsed(final long due) {
        return due - remaining;
    }
}
