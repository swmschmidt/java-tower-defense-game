package com.swmschmidt.td.core.gameloop;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GameLoopTest {

    @Test
    void fixedLoopInvokesUpdateAndRender() {
        AtomicInteger updates = new AtomicInteger();
        AtomicInteger renders = new AtomicInteger();

        GameLoop loop = new GameLoop(
            60,
            60,
            updates::incrementAndGet,
            renders::incrementAndGet,
            () -> updates.get() < 3
        );

        loop.run();

        assertTrue(updates.get() >= 3);
        assertTrue(renders.get() >= 1);
    }
}
