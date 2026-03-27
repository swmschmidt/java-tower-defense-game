package com.swmschmidt.td.core.input;

public interface InputService {
    void poll();

    boolean isExitRequested();
}
