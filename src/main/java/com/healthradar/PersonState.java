package com.healthradar;

/**
 * Health state for one moving person in the agent-based simulation.
 */
public enum PersonState {
    HEALTHY,
    INCUBATING,
    INFECTIOUS,
    RECOVERED,
    DEAD
}
