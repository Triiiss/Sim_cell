package com.healthradar;

/**
 * Represents one position of the simulation grid.
 */
public class Cell {
    private CellState state;
    private int infectedSteps;

    public Cell() {
        this.state = CellState.HEALTHY;
        this.infectedSteps = 0;
    }

    public Cell(CellState state) {
        this.state = state;
        this.infectedSteps = 0;
    }

    public CellState getState() {
        return state;
    }

    public void setState(CellState state) {
        this.state = state;
    }

    public int getInfectedSteps() {
        return infectedSteps;
    }

    public void setInfectedSteps(int infectedSteps) {
        this.infectedSteps = infectedSteps;
    }
}
