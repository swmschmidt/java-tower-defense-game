package com.swmschmidt.td.core.input;

import java.util.Optional;

public interface InputService {
    void poll();

    boolean isExitRequested();

    boolean consumePlaceTowerRequested();

    Optional<PointerClick> consumeSelectRequested();

    Optional<PointerClick> consumeContextCommandRequested();
}
