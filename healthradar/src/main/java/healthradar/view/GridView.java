package healthradar.view;

import healthradar.model.Cell;
import healthradar.model.CellState;
import healthradar.model.Grid;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * A JavaFX {@link Canvas} that visually renders the simulation {@link Grid}.
 *
 * <p>Each grid cell is drawn as a coloured square. The colour encodes the
 * cell's {@link CellState}:</p>
 * <ul>
 *   <li>EMPTY       – light grey background</li>
 *   <li>SUSCEPTIBLE – soft blue</li>
 *   <li>EXPOSED     – orange</li>
 *   <li>INFECTED    – red</li>
 *   <li>RECOVERED   – green</li>
 *   <li>DEAD        – dark grey</li>
 * </ul>
 *
 * <p>The view also handles mouse input for the three editing modes defined in
 * {@link EditMode}.</p>
 *
 * @author HealthRadar Team
 * @version 1.0
 */
public class GridView extends Canvas {

    // ── Colour palette ────────────────────────────────────────────────────────

    private static final Color COLOR_EMPTY       = Color.rgb(240, 240, 240);
    private static final Color COLOR_SUSCEPTIBLE = Color.rgb(100, 160, 220);
    private static final Color COLOR_VACCINATED  = Color.rgb(138,  43, 226); // purple
    private static final Color COLOR_EXPOSED     = Color.rgb(255, 165,  30);
    private static final Color COLOR_INFECTED    = Color.rgb(210,  50,  50);
    private static final Color COLOR_RECOVERED   = Color.rgb( 60, 180,  75);
    private static final Color COLOR_DEAD        = Color.rgb(100, 100, 100);
    private static final Color COLOR_GRID_LINE   = Color.rgb(200, 200, 200);

    // ── State ─────────────────────────────────────────────────────────────────

    /** The grid to render. */
    private Grid grid;

    /** Pixel size of each cell square. */
    private double cellSize;

    /** Whether to draw grid lines between cells. */
    private boolean showGridLines = true;

    // ── Drag selection for zone mode ──────────────────────────────────────────

    /** Row where the drag started (zone mode). */
    private int dragStartRow = -1;
    /** Column where the drag started (zone mode). */
    private int dragStartCol = -1;

    // ── Constructor ───────────────────────────────────────────────────────────

    /**
     * Creates a GridView for the given grid.
     *
     * @param grid     the grid to render
     * @param cellSize initial pixel size per cell
     */
    public GridView(Grid grid, double cellSize) {
        super(grid.getWidth() * cellSize, grid.getHeight() * cellSize);
        this.grid = grid;
        this.cellSize = cellSize;
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    /**
     * Redraws the entire grid onto this canvas.
     * Should be called after every simulation step or user edit.
     */
    public void redraw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        // Background
        gc.setFill(COLOR_EMPTY);
        gc.fillRect(0, 0, w, h);

        // Cells
        for (int r = 0; r < grid.getHeight(); r++) {
            for (int c = 0; c < grid.getWidth(); c++) {
                Cell cell = grid.getCell(r, c);
                Color fill = stateColor(cell.getState());
                gc.setFill(fill);
                gc.fillRect(c * cellSize, r * cellSize, cellSize, cellSize);
                // Draw a small white dot on masked cells (mask indicator)
                if (cell.isMasked() && cell.isAlive() && cellSize >= 6) {
                    gc.setFill(Color.WHITE);
                    double d = Math.max(2, cellSize * 0.25);
                    gc.fillOval(c * cellSize + cellSize - d - 1,
                                r * cellSize + 1, d, d);
                }
            }
        }

        // Grid lines (only for large enough cells)
        if (showGridLines && cellSize >= 4) {
            gc.setStroke(COLOR_GRID_LINE);
            gc.setLineWidth(0.5);
            for (int c = 0; c <= grid.getWidth(); c++)
                gc.strokeLine(c * cellSize, 0, c * cellSize, h);
            for (int r = 0; r <= grid.getHeight(); r++)
                gc.strokeLine(0, r * cellSize, w, r * cellSize);
        }
    }

    /**
     * Highlights the currently hovered cell with a white border (brush preview).
     *
     * @param pixelX mouse X in canvas coordinates
     * @param pixelY mouse Y in canvas coordinates
     */
    public void drawHoverHighlight(double pixelX, double pixelY) {
        redraw();
        int col = pixelToCol(pixelX);
        int row = pixelToRow(pixelY);
        if (col < 0 || row < 0 || col >= grid.getWidth() || row >= grid.getHeight()) return;
        GraphicsContext gc = getGraphicsContext2D();
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRect(col * cellSize + 1, row * cellSize + 1, cellSize - 2, cellSize - 2);
    }

    /**
     * Highlights the rectangular selection during a zone-mode drag.
     *
     * @param endRow    current end row
     * @param endCol    current end column
     */
    public void drawZoneSelection(int endRow, int endCol) {
        redraw();
        if (dragStartRow < 0) return;
        int r1 = Math.min(dragStartRow, endRow);
        int c1 = Math.min(dragStartCol, endCol);
        int r2 = Math.max(dragStartRow, endRow);
        int c2 = Math.max(dragStartCol, endCol);
        GraphicsContext gc = getGraphicsContext2D();
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(2);
        gc.strokeRect(c1 * cellSize, r1 * cellSize,
                (c2 - c1 + 1) * cellSize, (r2 - r1 + 1) * cellSize);
    }

    // ── Coordinate conversion ─────────────────────────────────────────────────

    /**
     * Converts a pixel X coordinate to a grid column index.
     *
     * @param px pixel X
     * @return column index, or -1 if out of bounds
     */
    public int pixelToCol(double px) {
        int c = (int)(px / cellSize);
        return (c >= 0 && c < grid.getWidth()) ? c : -1;
    }

    /**
     * Converts a pixel Y coordinate to a grid row index.
     *
     * @param py pixel Y
     * @return row index, or -1 if out of bounds
     */
    public int pixelToRow(double py) {
        int r = (int)(py / cellSize);
        return (r >= 0 && r < grid.getHeight()) ? r : -1;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    /** @param grid the new grid to display (used after load or resize) */
    public void setGrid(Grid grid) {
        this.grid = grid;
        setWidth(grid.getWidth() * cellSize);
        setHeight(grid.getHeight() * cellSize);
    }

    /** @return current pixel size of one cell */
    public double getCellSize() { return cellSize; }

    /**
     * Changes the cell size and resizes the canvas accordingly.
     *
     * @param cellSize new pixel size per cell
     */
    public void setCellSize(double cellSize) {
        this.cellSize = cellSize;
        setWidth(grid.getWidth() * cellSize);
        setHeight(grid.getHeight() * cellSize);
        redraw();
    }

    /** @param show true to draw grid lines */
    public void setShowGridLines(boolean show) { this.showGridLines = show; }

    /** @return drag start row for zone mode */
    public int getDragStartRow() { return dragStartRow; }

    /** @param dragStartRow drag start row */
    public void setDragStartRow(int dragStartRow) { this.dragStartRow = dragStartRow; }

    /** @return drag start column for zone mode */
    public int getDragStartCol() { return dragStartCol; }

    /** @param dragStartCol drag start column */
    public void setDragStartCol(int dragStartCol) { this.dragStartCol = dragStartCol; }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Returns the display colour for a given {@link CellState}.
     *
     * @param state the cell state
     * @return corresponding JavaFX Color
     */
    public static Color stateColor(CellState state) {
        return switch (state) {
            case SUSCEPTIBLE -> COLOR_SUSCEPTIBLE;
            case VACCINATED  -> COLOR_VACCINATED;
            case EXPOSED     -> COLOR_EXPOSED;
            case INFECTED    -> COLOR_INFECTED;
            case RECOVERED   -> COLOR_RECOVERED;
            case DEAD        -> COLOR_DEAD;
            default          -> COLOR_EMPTY;
        };
    }
}
