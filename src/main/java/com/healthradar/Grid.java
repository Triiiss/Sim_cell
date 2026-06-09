package com.healthradar;

/**
 * Stores the two-dimensional simulation map.
 */
public class Grid {
    private final int width;
    private final int height;
    private final Cell[][] cells;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[height][width];
        initializeCells();
    }

    private void initializeCells() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells[y][x] = new Cell();
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Cell getCell(int x, int y) {
        return cells[y][x];
    }

    public void setCell(int x, int y, Cell cell) {
        cells[y][x] = cell;
    }

    public Cell[][] getCells() {
        return cells;
    }

    public boolean isInside(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public int getPopulationCount() {
        int count = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                count += cells[y][x].getPopulationCount();
            }
        }

        return count;
    }

    public int getInfectedPopulationCount() {
        int count = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                count += cells[y][x].getInfectedPopulationCount();
            }
        }

        return count;
    }
}
