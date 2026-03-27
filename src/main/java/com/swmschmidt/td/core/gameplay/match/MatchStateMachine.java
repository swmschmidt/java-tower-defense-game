package com.swmschmidt.td.core.gameplay.match;

public final class MatchStateMachine {
    private final int totalWaves;
    private final double preWaveDelaySeconds;
    private final double postWaveDelaySeconds;

    private MatchPhase phase;
    private int currentWaveIndex;
    private double phaseTimerSeconds;

    public MatchStateMachine(int totalWaves, double preWaveDelaySeconds, double postWaveDelaySeconds) {
        if (totalWaves < 1) {
            throw new IllegalArgumentException("totalWaves must be at least 1");
        }
        if (preWaveDelaySeconds < 0.0) {
            throw new IllegalArgumentException("preWaveDelaySeconds must be at least zero");
        }
        if (postWaveDelaySeconds < 0.0) {
            throw new IllegalArgumentException("postWaveDelaySeconds must be at least zero");
        }

        this.totalWaves = totalWaves;
        this.preWaveDelaySeconds = preWaveDelaySeconds;
        this.postWaveDelaySeconds = postWaveDelaySeconds;
        this.phase = MatchPhase.PRE_WAVE;
        this.currentWaveIndex = 0;
        this.phaseTimerSeconds = 0.0;
    }

    public void updateBeforeWave(double deltaSeconds) {
        if (phase != MatchPhase.PRE_WAVE) {
            return;
        }
        phaseTimerSeconds += deltaSeconds;
        if (phaseTimerSeconds >= preWaveDelaySeconds) {
            phase = MatchPhase.IN_WAVE;
            phaseTimerSeconds = 0.0;
        }
    }

    public void onWaveCompleted() {
        if (phase != MatchPhase.IN_WAVE) {
            return;
        }
        phase = MatchPhase.POST_WAVE;
        phaseTimerSeconds = 0.0;
    }

    public void updateAfterWave(double deltaSeconds) {
        if (phase != MatchPhase.POST_WAVE) {
            return;
        }

        phaseTimerSeconds += deltaSeconds;
        if (phaseTimerSeconds < postWaveDelaySeconds) {
            return;
        }

        currentWaveIndex++;
        phaseTimerSeconds = 0.0;
        if (currentWaveIndex >= totalWaves) {
            phase = MatchPhase.VICTORY;
            return;
        }

        phase = MatchPhase.PRE_WAVE;
    }

    public void onDefeat() {
        phase = MatchPhase.DEFEAT;
    }

    public MatchPhase phase() {
        return phase;
    }

    public int currentWaveNumber() {
        return Math.min(currentWaveIndex + 1, totalWaves);
    }

    public int totalWaves() {
        return totalWaves;
    }

    public boolean isInWave() {
        return phase == MatchPhase.IN_WAVE;
    }

    public boolean isTerminal() {
        return phase == MatchPhase.VICTORY || phase == MatchPhase.DEFEAT;
    }
}
