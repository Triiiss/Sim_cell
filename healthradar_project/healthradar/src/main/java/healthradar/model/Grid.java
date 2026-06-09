package healthradar.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The 2-D grid that holds all {@link Cell} objects and drives the simulation
 * rules at each time step.
 *
 * <h2>Topology</h2>
 * <p>The grid can operate in two modes:</p>
 * <ul>
 *   <li><b>Bounded</b>  – cells on the border have fewer neighbours.</li>
 *   <li><b>Toroidal</b> – the grid wraps around: left edge connects to the
 *       right edge, top to bottom.</li>
 * </ul>
 *
 * <h2>Simulation step</h2>
 * <ol>
 *   <li>Movement phase – alive cells optionally move to an adjacent empty slot.</li>
 *   <li>Infection phase – infected cells attempt to transmit to susceptible
 *       neighbours (radius depends on airborne / contact mode).</li>
 *   <li>Progression phase – each cell ages in its current state and transitions
 *       when the appropriate threshold is reached.</li>
 * </ol>
 *
 * @author HealthRadar Team
 * @version 1.0
 */
public class Grid implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Number of columns. */
    private final int width;

    /** Number of rows. */
    private final int height;

    /** Whether the grid wraps around at the edges. */
    private boolean toroidal;

    /** The cell array – row-major: cells[row][col]. */
    private Cell[][] cells;

    /** Shared random number generator for reproducible simulations. */
    private final Random rng;

    /** The active disease applied to the simulation. */
    private Disease disease;

    // ── Constructor ───────────────────────────────────────────────────────────

    /**
     * Creates a new Grid filled with EMPTY cells.
     *
     * @param width     number of columns
     * @param height    number of rows
     * @param toroidal  true for toroidal (wrap-around) topology
     * @param disease   disease parameters to use
     * @param seed      random seed (use 0 for a random seed)
     */
    public Grid(int width, int height, boolean toroidal, Disease disease, long seed) {
        this.width = width;
        this.height = height;
        this.toroidal = toroidal;
        this.disease = disease;
        this.rng = (seed == 0) ? new Random() : new Random(seed);
        this.cells = new Cell[height][width];
        for (int r = 0; r < height; r++)
            for (int c = 0; c < width; c++)
                cells[r][c] = new Cell();
    }

    // ── Population helpers ────────────────────────────────────────────────────

    /**
     * Places a single cell of the given state at (row, col).
     *
     * @param row   row index
     * @param col   column index
     * @param state desired state
     */
    public void setCell(int row, int col, CellState state) {
        if (!inBounds(row, col)) return;
        cells[row][col] = (state == CellState.EMPTY) ? new Cell() : new Cell(state, rng);
    }

    /**
     * Fills a rectangular area with cells of the given state.
     *
     * @param r1    top-left row (inclusive)
     * @param c1    top-left column (inclusive)
     * @param r2    bottom-right row (inclusive)
     * @param c2    bottom-right column (inclusive)
     * @param state state to assign
     */
    public void fillArea(int r1, int c1, int r2, int c2, CellState state) {
        for (int r = Math.min(r1, r2); r <= Math.max(r1, r2); r++)
            for (int c = Math.min(c1, c2); c <= Math.max(c1, c2); c++)
                setCell(r, c, state);
    }

    /**
     * Randomly populates the grid.
     *
     * @param susceptibleCount  number of SUSCEPTIBLE cells to place
     * @param infectedCount     number of INFECTED cells to place
     */
    public void randomPopulate(int susceptibleCount, int infectedCount) {
        // Build a shuffled list of all positions
        List<int[]> positions = new ArrayList<>(width * height);
        for (int r = 0; r < height; r++)
            for (int c = 0; c < width; c++)
                positions.add(new int[]{r, c});
        Collections.shuffle(positions, rng);

        int idx = 0;
        for (int i = 0; i < susceptibleCount && idx < positions.size(); i++, idx++)
            setCell(positions.get(idx)[0], positions.get(idx)[1], CellState.SUSCEPTIBLE);
        for (int i = 0; i < infectedCount && idx < positions.size(); i++, idx++)
            setCell(positions.get(idx)[0], positions.get(idx)[1], CellState.INFECTED);
    }

    // ── Simulation step ───────────────────────────────────────────────────────

    /**
     * Advances the simulation by one step.
     *
     * <p>The update is performed on a copy of the grid to avoid order-dependent
     * artefacts (all cells read the previous generation).</p>
     */
    public void step() {
        // Deep-copy current grid as the read-source
        Cell[][] next = deepCopy(cells);

        // --- Phase 1: movement ---
        movePhase(next);

        // --- Phase 2: infection spread ---
        infectionPhase(next);

        // --- Phase 3: state progression ---
        progressionPhase(next);

        cells = next;
    }

    /**
     * Phase 1 – alive cells randomly move to adjacent empty cells.
     *
     * @param grid the working copy of the grid
     */
    private void movePhase(Cell[][] grid) {
        // Iterate in random order to avoid directional bias
        List<int[]> coords = shuffledCoords();
        for (int[] pos : coords) {
            int r = pos[0], c = pos[1];
            Cell cell = grid[r][c];
            if (!cell.isAlive()) continue;
            if (rng.nextDouble() >= cell.getMoveProbability()) continue;

            // Pick a random orthogonal neighbour that is currently empty
            int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
            List<int[]> empty = new ArrayList<>();
            for (int[] d : dirs) {
                int nr = wrap(r + d[0], height);
                int nc = wrap(c + d[1], width);
                if (toroidal || inBounds(r + d[0], c + d[1]))
                    if (grid[nr][nc].isEmpty()) empty.add(new int[]{nr, nc});
            }
            if (empty.isEmpty()) continue;
            int[] dest = empty.get(rng.nextInt(empty.size()));
            // Swap
            Cell tmp = grid[dest[0]][dest[1]];
            grid[dest[0]][dest[1]] = grid[r][c];
            grid[r][c] = tmp;
        }
    }

    /**
     * Phase 2 – infected cells try to transmit to susceptible cells within
     * their influence radius.
     *
     * @param grid the working copy of the grid
     */
    private void infectionPhase(Cell[][] grid) {
        // Snapshot: only cells that were INFECTED before this phase spread
        boolean[][] wasInfected = new boolean[height][width];
        for (int r = 0; r < height; r++)
            for (int c = 0; c < width; c++)
                wasInfected[r][c] = (grid[r][c].getState() == CellState.INFECTED);

        int radius = disease.isAirborne() ? disease.getTransmissionRadius() : 1;

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (!wasInfected[r][c]) continue;

                for (int dr = -radius; dr <= radius; dr++) {
                    for (int dc = -radius; dc <= radius; dc++) {
                        if (dr == 0 && dc == 0) continue;
                        // Euclidean distance check for airborne
                        if (disease.isAirborne() && Math.sqrt(dr*dr + dc*dc) > radius) continue;

                        int nr = r + dr;
                        int nc = c + dc;
                        if (toroidal) { nr = wrap(nr, height); nc = wrap(nc, width); }
                        else if (!inBounds(nr, nc)) continue;

                        Cell target = grid[nr][nc];
                        if (target.getState() != CellState.SUSCEPTIBLE) continue;

                        double prob = target.effectiveInfectionProbability(
                                disease.getTransmissionRate());
                        if (rng.nextDouble() < prob) {
                            target.setState(CellState.EXPOSED);
                            target.resetStateAge();
                        }
                    }
                }
            }
        }
    }

    /**
     * Phase 3 – advance each cell's state age and trigger transitions when
     * the disease-defined thresholds are reached.
     *
     * @param grid the working copy of the grid
     */
    private void progressionPhase(Cell[][] grid) {
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                Cell cell = grid[r][c];
                switch (cell.getState()) {
                    case EXPOSED -> {
                        cell.incrementStateAge();
                        if (cell.getStateAge() >= disease.getIncubationPeriod()) {
                            cell.setState(CellState.INFECTED);
                            cell.resetStateAge();
                        }
                    }
                    case INFECTED -> {
                        cell.incrementStateAge();
                        if (cell.getStateAge() >= disease.getInfectionDuration()) {
                            if (rng.nextDouble() < disease.getMortalityRate()) {
                                cell.setState(CellState.DEAD);
                            } else {
                                cell.setState(CellState.RECOVERED);
                            }
                            cell.resetStateAge();
                        }
                    }
                    case RECOVERED -> {
                        cell.incrementStateAge();
                        if (cell.getStateAge() >= disease.getImmunityDuration()) {
                            cell.setState(CellState.SUSCEPTIBLE);
                            cell.resetStateAge();
                        }
                    }
                    default -> { /* EMPTY, DEAD, SUSCEPTIBLE – no progression */ }
                }
            }
        }
    }

    // ── Statistics ────────────────────────────────────────────────────────────

    /**
     * Counts cells in a given state.
     *
     * @param state the state to count
     * @return number of cells currently in that state
     */
    public int countState(CellState state) {
        int count = 0;
        for (int r = 0; r < height; r++)
            for (int c = 0; c < width; c++)
                if (cells[r][c].getState() == state) count++;
        return count;
    }

    /**
     * Returns the total number of non-empty cells (living + dead).
     *
     * @return total occupied cell count
     */
    public int totalPopulation() {
        int count = 0;
        for (int r = 0; r < height; r++)
            for (int c = 0; c < width; c++)
                if (cells[r][c].getState() != CellState.EMPTY) count++;
        return count;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    /** @return grid width (columns) */
    public int getWidth() { return width; }

    /** @return grid height (rows) */
    public int getHeight() { return height; }

    /** @return true if toroidal topology is active */
    public boolean isToroidal() { return toroidal; }

    /** @param toroidal true to enable toroidal mode */
    public void setToroidal(boolean toroidal) { this.toroidal = toroidal; }

    /**
     * Returns the cell at the given position.
     *
     * @param row row index
     * @param col column index
     * @return the Cell at that position, or null if out of bounds
     */
    public Cell getCell(int row, int col) {
        if (!inBounds(row, col)) return null;
        return cells[row][col];
    }

    /** @return the disease currently configured for this grid */
    public Disease getDisease() { return disease; }

    /** @param disease new disease to apply */
    public void setDisease(Disease disease) { this.disease = disease; }

    // ── Utility ───────────────────────────────────────────────────────────────

    /**
     * Checks if (row, col) is within the grid bounds.
     *
     * @param row row index
     * @param col column index
     * @return true if the position is valid
     */
    private boolean inBounds(int row, int col) {
        return row >= 0 && row < height && col >= 0 && col < width;
    }

    /**
     * Wraps an index to stay within [0, size) for toroidal topology.
     *
     * @param index the raw index (may be negative or ≥ size)
     * @param size  the dimension bound
     * @return wrapped index in [0, size)
     */
    private int wrap(int index, int size) {
        return ((index % size) + size) % size;
    }

    /**
     * Returns a shuffled list of all (row, col) coordinates.
     *
     * @return shuffled coordinate list
     */
    private List<int[]> shuffledCoords() {
        List<int[]> list = new ArrayList<>(width * height);
        for (int r = 0; r < height; r++)
            for (int c = 0; c < width; c++)
                list.add(new int[]{r, c});
        Collections.shuffle(list, rng);
        return list;
    }

    /**
     * Performs a deep copy of the cell grid.
     *
     * @param source the source grid to copy
     * @return a new 2-D array of independent Cell copies
     */
    private Cell[][] deepCopy(Cell[][] source) {
        Cell[][] copy = new Cell[height][width];
        for (int r = 0; r < height; r++)
            for (int c = 0; c < width; c++)
                copy[r][c] = source[r][c].copy();
        return copy;
    }

    /**
     * Clears the entire grid (fills with EMPTY cells).
     */
    public void clear() {
        for (int r = 0; r < height; r++)
            for (int c = 0; c < width; c++)
                cells[r][c] = new Cell();
    }
}
