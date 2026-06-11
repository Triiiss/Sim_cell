package healthradar.controller;

import healthradar.io.ChartExporter;
import healthradar.io.SimulationSerializer;
import healthradar.model.*;
import healthradar.view.ConfigPanel;
import healthradar.view.EditMode;
import healthradar.view.GridView;
import healthradar.view.StatsPanel;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * Main application controller.
 *
 * <p>Builds the complete JavaFX scene graph, wires all event handlers, and
 * drives the simulation loop via a {@link AnimationTimer}.</p>
 *
 * <h2>Layout</h2>
 * <pre>
 * ┌──────────────────────────────────────────────────────┐
 * │  Top toolbar (disease selector, mode, controls)      │
 * ├───────────────────────────────────┬──────────────────┤
 * │  ScrollPane → GridView (canvas)   │  StatsPanel      │
 * └───────────────────────────────────┴──────────────────┘
 * </pre>
 *
 * @author HealthRadar Team
 * @version 1.0
 */
public class MainController {

    // ── Simulation objects ────────────────────────────────────────────────────

    private Grid             grid;
    private SimulationEngine engine;

    // ── View objects ──────────────────────────────────────────────────────────

    private GridView   gridView;
    private StatsPanel statsPanel;
    private Stage      primaryStage;

    // ── Animation loop ────────────────────────────────────────────────────────

    private AnimationTimer animationTimer;
    /** Nanoseconds between automatic steps (default = 200 ms). */
    private long stepIntervalNanos = 200_000_000L;
    private long lastStepTime = 0;
    private boolean running = false;

    // ── Edit state ────────────────────────────────────────────────────────────

    private EditMode   editMode       = EditMode.BRUSH;
    private CellState  paintState     = CellState.SUSCEPTIBLE;
    /** When true, painting toggles the mask flag instead of changing the state. */
    private boolean    maskMode       = false;

    // ── UI controls (kept as fields for cross-method access) ──────────────────

    private Label      statusLabel;
    private Slider     speedSlider;
    private ComboBox<String> diseaseCombo;
    private ComboBox<String> paintStateCombo;
    private ComboBox<ZoneType> zoneTypeCombo;

    // ── Disease presets ───────────────────────────────────────────────────────

    private Disease currentDisease = Disease.influenza();

    // ── Random populate sliders ───────────────────────────────────────────────

    private Slider susceptibleSlider;
    private Slider infectedSlider;
    private Slider moveProbSlider;

    // ── Disease parameter sliders ─────────────────────────────────────────────

    private Slider transmissionSlider;
    private Slider mortalitySlider;
    private Slider radiusSlider;
    private Slider incubationSlider;
    private Slider infectionDurSlider;
    private Slider immunitySlider;
    private Slider vaccineEfficacySlider;
    private Slider vaccineImmunitySlider;
    private Slider maskInwardSlider;
    private Slider maskOutwardSlider;
    private CheckBox airborneCheck;

    // ─────────────────────────────────────────────────────────────────────────
    //  Public entry point
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds and displays the main application window.
     *
     * @param stage the primary JavaFX stage provided by the Application class
     */
    public void start(Stage stage) {
        this.primaryStage = stage;

        // Default grid: 60 × 45 cells, bounded, Influenza
        grid   = new Grid(60, 45, false, currentDisease, 0);
        engine = new SimulationEngine(grid);

        gridView   = new GridView(grid, 14);
        statsPanel = new StatsPanel(engine);

        // ── Top toolbar ───────────────────────────────────────────────────────
        HBox toolbar = buildToolbar();
// ── Centre: scrollable grid canvas ───────────────────────────────────
        ScrollPane scrollPane = new ScrollPane(gridView);
        scrollPane.setStyle("-fx-background-color: #a7a7a8; -fx-background: #d4d2d2;");
        scrollPane.setFitToWidth(true);   
        scrollPane.setFitToHeight(true);  
        
        scrollPane.setPannable(false);     

        gridView.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            if (e.isMiddleButtonDown()) {
                scrollPane.setPannable(true);
            }
        });
        gridView.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            scrollPane.setPannable(false);
        });

        // ── Right sidebar ────────────────────────────────────────────────────
        VBox sidebarContent = buildSidebar(); 
        
        ScrollPane sidebarScrollPane = new ScrollPane(sidebarContent);
        sidebarScrollPane.setStyle("-fx-background-color: #16213e; -fx-background: #16213e;");
        sidebarScrollPane.setFitToWidth(true);
        sidebarScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebarScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // ── Status bar ────────────────────────────────────────────────────────
        statusLabel = new Label("Ready – draw cells, then press Play.");
        statusLabel.setFont(Font.font("Monospaced", 11));
        statusLabel.setTextFill(Color.LIGHTGRAY);
        HBox statusBar = new HBox(statusLabel);
        statusBar.setStyle("-fx-background-color: #0d0d1e;");
        statusBar.setPadding(new Insets(4, 8, 4, 8));

        // ── Root layout ───────────────────────────────────────────────────────
        BorderPane root = new BorderPane();
        root.setTop(toolbar);
        root.setCenter(scrollPane);
        root.setRight(sidebarScrollPane); // <── ON MET LE SCROLLPANE ICI AU LIEU DU VBOX
        root.setBottom(statusBar);
        root.setStyle("-fx-background-color: #12122a;");

        // ── Mouse events on grid canvas ───────────────────────────────────────
        wireMouseEvents();

        // ── Animation timer ───────────────────────────────────────────────────
        buildAnimationTimer();

        Scene scene = new Scene(root, 1100, 720);
        scene.setFill(Color.rgb(18, 18, 42));
        stage.setTitle("HealthRadar – Disease Propagation Simulator");
        stage.setScene(scene);
        stage.show();

        gridView.redraw();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UI builders
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds the top toolbar containing simulation controls and mode selectors.
     *
     * @return the constructed HBox toolbar
     */
    private HBox buildToolbar() {
        HBox bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(6, 10, 6, 10));
        bar.setStyle("-fx-background-color: #16213e;");

        // ── Play / Pause ──────────────────────────────────────────────────────
        Button playBtn  = styledButton("▶ Play",  "#2ecc71");
        Button pauseBtn = styledButton("⏸ Pause", "#e67e22");
        Button stepBtn  = styledButton("⏭ Step",  "#3498db");
        Button resetBtn = styledButton("↺ Reset", "#e74c3c");

        playBtn.setOnAction(e  -> startSimulation());
        pauseBtn.setOnAction(e -> pauseSimulation());
        stepBtn.setOnAction(e  -> { pauseSimulation(); doStep(); });
        resetBtn.setOnAction(e -> resetSimulation());

        // ── Speed ─────────────────────────────────────────────────────────────
        Label speedLbl = whiteLabel("Speed:");
        speedSlider = new Slider(1, 20, 5);
        speedSlider.setPrefWidth(100);
        speedSlider.setMajorTickUnit(5);
        speedSlider.setShowTickMarks(true);
        speedSlider.valueProperty().addListener((obs, o, n) ->
                stepIntervalNanos = (long)(1_000_000_000L / n.doubleValue()));

        // ── Disease selector ──────────────────────────────────────────────────
        Label diseaseLbl = whiteLabel("Disease:");
        diseaseCombo = new ComboBox<>();
        diseaseCombo.getItems().addAll("Influenza", "COVID-Like", "Custom");
        diseaseCombo.setValue("Influenza");
        diseaseCombo.setOnAction(e -> onDiseaseSelected());

        // ── Edit mode ─────────────────────────────────────────────────────────
        Label modeLbl = whiteLabel("Mode:");
        ToggleGroup modeGroup = new ToggleGroup();
        ToggleButton brushBtn  = modeToggle("Brush",  EditMode.BRUSH,      modeGroup);
        ToggleButton zoneBtn   = modeToggle("Zone",   EditMode.ZONE,        modeGroup);
        ToggleButton indivBtn  = modeToggle("Individual", EditMode.INDIVIDUAL, modeGroup);
        ToggleButton zoneTypeBtn = modeToggle("Zone Type", EditMode.ZONETYPE, modeGroup);
        brushBtn.setSelected(true);

        // ── Paint state selector ──────────────────────────────────────────────
        Label paintLbl = whiteLabel("Paint:");
        paintStateCombo = new ComboBox<>();
        paintStateCombo.getItems().addAll("Susceptible","Vaccinated","Exposed","Infected","Recovered","Dead","Empty");
        paintStateCombo.setValue("Susceptible");
        paintStateCombo.setOnAction(e -> onPaintStateSelected());

        // ── Mask toggle ───────────────────────────────────────────────────────
        ToggleButton maskBtn = new ToggleButton("Mask");
        maskBtn.setStyle("-fx-background-color:#2c3e50; -fx-text-fill:white; -fx-font-size:11px;");
        maskBtn.selectedProperty().addListener((obs, o, sel) -> {
            maskMode = sel;
            maskBtn.setStyle(sel
                ? "-fx-background-color:#1a6b8a; -fx-text-fill:white; -fx-font-size:11px;"
                : "-fx-background-color:#2c3e50; -fx-text-fill:white; -fx-font-size:11px;");
            setStatus(sel ? "Mask mode ON — paint to toggle mask on cells"
                          : "Mask mode OFF");
        });

        // ── Zone Type selector ────────────────────────────────────────────────
        Label zoneTypeLbl = whiteLabel("Zone:"); // <── AJOUT D'UN LABEL POUR LE MENU
        zoneTypeCombo = new ComboBox<>();
        zoneTypeCombo.getItems().addAll(ZoneType.values());
        zoneTypeCombo.setValue(ZoneType.RESIDENTIAL);
        
        

        Button saveBtn = styledButton("Save", "#8e44ad");
        Button loadBtn = styledButton("Load", "#2980b9");
        saveBtn.setOnAction(e -> saveSimulation());
        loadBtn.setOnAction(e -> loadSimulation());

        // ── Settings button ───────────────────────────────────────────────────
        Button settingsBtn = styledButton("⚙ Settings", "#34495e");
        settingsBtn.setOnAction(e -> openSettings());

        Button printBtn = styledButton("📊 Print chart", "#16a085");
        printBtn.setOnAction(e -> exportChart());

        bar.getChildren().addAll(
            playBtn, pauseBtn, stepBtn, resetBtn,
                new Separator(Orientation.VERTICAL),
                speedLbl, speedSlider,
                new Separator(Orientation.VERTICAL),
                diseaseLbl, diseaseCombo,
                new Separator(Orientation.VERTICAL),
                modeLbl, brushBtn, zoneBtn, indivBtn, zoneTypeBtn,
                new Separator(Orientation.VERTICAL),
                paintLbl, paintStateCombo,
                maskBtn,
                new Separator(Orientation.VERTICAL),
                zoneTypeLbl, zoneTypeCombo,
                new Separator(Orientation.VERTICAL),
                saveBtn, loadBtn,
                settingsBtn,
                new Separator(Orientation.VERTICAL),
                printBtn
        );
        return bar;
    }

    /**
     * Builds the right sidebar containing disease parameters and random
     * population controls.
     *
     * @return the constructed VBox sidebar
     */
    private VBox buildSidebar() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10, 20, 10, 10));
        box.setPrefWidth(350);
        box.setMaxWidth(Double.MAX_VALUE);
        box.setStyle("-fx-background-color: #16213e;");

        // ── Stats panel ───────────────────────────────────────────────────────
        box.getChildren().add(statsPanel);

        box.getChildren().add(separator());

        // ── Disease parameters ────────────────────────────────────────────────
        box.getChildren().add(sectionLabel("Disease Parameters"));

        airborneCheck = new CheckBox("Airborne transmission");
        airborneCheck.setTextFill(Color.WHITE);
        airborneCheck.setFont(Font.font(12));
        airborneCheck.setOnAction(e -> applyDiseaseParams());

        transmissionSlider = labelledSlider("Transmission rate  (0.01–1.0)",
                0.01, 1.0, currentDisease.getTransmissionRate(), box);
        mortalitySlider    = labelledSlider("Mortality rate     (0.0-0.5)",
                0.0,  0.5, currentDisease.getMortalityRate(),    box);
        radiusSlider       = labelledSlider("Airborne radius    (1-10 cells)",
                1, 10, currentDisease.getTransmissionRadius(),   box);
        incubationSlider   = labelledSlider("Incubation steps   (1-30)",
                1, 30, currentDisease.getIncubationPeriod(),     box);
        infectionDurSlider = labelledSlider("Infection steps    (1-60)",
                1, 60, currentDisease.getInfectionDuration(),    box);
        immunitySlider     = labelledSlider("Immunity steps     (1-120)",
                1, 120, currentDisease.getImmunityDuration(),    box);

        transmissionSlider.valueProperty().addListener((o, v1, v2) -> applyDiseaseParams());
        mortalitySlider   .valueProperty().addListener((o, v1, v2) -> applyDiseaseParams());
        radiusSlider      .valueProperty().addListener((o, v1, v2) -> applyDiseaseParams());
        incubationSlider  .valueProperty().addListener((o, v1, v2) -> applyDiseaseParams());
        infectionDurSlider.valueProperty().addListener((o, v1, v2) -> applyDiseaseParams());
        immunitySlider    .valueProperty().addListener((o, v1, v2) -> applyDiseaseParams());

        box.getChildren().add(sectionLabel("Population Parameters"));
        vaccineEfficacySlider = labelledSlider("Vaccine efficacy    (0.0-1.0)",
                0.0, 1.0, currentDisease.getVaccineEfficacy(), box);
        vaccineImmunitySlider = labelledSlider("Vaccine immunity steps (1-500)",
                1, 500, currentDisease.getVaccineImmunityDuration(), box);
        maskInwardSlider  = labelledSlider("Mask inward efficacy  (0.0-1.0)",
                0.0, 1.0, currentDisease.getMaskInwardEfficacy(), box);
        maskOutwardSlider = labelledSlider("Mask outward efficacy (0.0-1.0)",
                0.0, 1.0, currentDisease.getMaskOutwardEfficacy(), box);
        moveProbSlider = labelledSlider("Global Move Probability (0.0–1.0)", 
                0.0, 1.0, 0.25, box);

        vaccineEfficacySlider.valueProperty().addListener((o, v1, v2) -> applyDiseaseParams());
        vaccineImmunitySlider.valueProperty().addListener((o, v1, v2) -> applyDiseaseParams());
        maskInwardSlider     .valueProperty().addListener((o, v1, v2) -> applyDiseaseParams());
        maskOutwardSlider    .valueProperty().addListener((o, v1, v2) -> applyDiseaseParams());
        moveProbSlider.valueProperty().addListener((o, v1, v2) -> applyMovementProbability(v2.doubleValue()));

        box.getChildren().add(airborneCheck);

        box.getChildren().add(separator());

        // ── Random populate ───────────────────────────────────────────────────
        box.getChildren().add(sectionLabel("Random Populate"));
        Label popNote = new Label("% of total grid cells");
        popNote.setTextFill(Color.GRAY);
        popNote.setFont(Font.font(10));
        box.getChildren().add(popNote);

        susceptibleSlider = labelledSlider("Susceptible % of grid", 0, 100, 40, box);
        infectedSlider    = labelledSlider("Infected %    of grid", 0, 100,  5, box);

        Button populateBtn = styledButton("🌐 Random Populate", "#16a085");
        populateBtn.setMaxWidth(Double.MAX_VALUE);
        populateBtn.setOnAction(e -> randomPopulate());

        Button clearBtn = styledButton("🗑 Clear Grid", "#c0392b");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            grid.clear();
            engine = new SimulationEngine(grid);
            statsPanel.setEngine(engine);
            gridView.redraw();
            setStatus("Grid cleared.");
        });

        box.getChildren().addAll(populateBtn, clearBtn);

        box.getChildren().add(separator());

        // ── Toroidal toggle ───────────────────────────────────────────────────
        CheckBox toroidalCheck = new CheckBox("Toroidal topology (wrap edges)");
        toroidalCheck.setTextFill(Color.WHITE);
        toroidalCheck.setFont(Font.font(12));
        toroidalCheck.setSelected(false);
        toroidalCheck.setOnAction(e -> {
            grid.setToroidal(toroidalCheck.isSelected());
            setStatus("Topology: " + (grid.isToroidal() ? "Toroidal" : "Bounded"));
        });
        box.getChildren().add(toroidalCheck);

        return box;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Mouse event wiring
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Attaches all mouse event handlers to the {@link GridView} canvas.
     */
    private void wireMouseEvents() {
        gridView.setOnMousePressed(this::onMousePressed);
        gridView.setOnMouseDragged(this::onMouseDragged);
        gridView.setOnMouseReleased(this::onMouseReleased);
        gridView.setOnMouseMoved(e -> gridView.drawHoverHighlight(e.getX(), e.getY()));
        gridView.setOnMouseExited(e -> gridView.redraw());
    }

    /**
     * Handles a mouse-pressed event on the grid canvas.
     *
     * @param e the mouse event
     */
    private void onMousePressed(MouseEvent e) {
        int row = gridView.pixelToRow(e.getY());
        int col = gridView.pixelToCol(e.getX());
        if (row < 0 || col < 0) return;

        switch (editMode) {
            case BRUSH, INDIVIDUAL -> {
                if (!maskMode) {
                    grid.setCell(row, col, paintState);
                } else {
                    if (paintState != CellState.EMPTY) {
                        grid.setCell(row, col, paintState);
                    }
                }
                if (maskMode) {
                    var cell = grid.getCell(row, col);
                    if (cell != null && cell.isAlive()) {
                        cell.setMasked(!cell.isMasked()); // Toggle le masque au clic
                    }
                }
                gridView.redraw();
            }
            case ZONE -> {
                gridView.setDragStartRow(row);
                gridView.setDragStartCol(col);
            }
            case ZONETYPE -> { // <-- Nouveau cas
                var cell = grid.getCell(row, col);
                ZoneType selectedZone = zoneTypeCombo.getValue();
                if (cell != null && selectedZone != null) {
                    cell.setZoneType(selectedZone);
                }
                gridView.redraw();
            }
        }
    }

    /**
     * Handles a mouse-dragged event on the grid canvas.
     *
     * @param e the mouse event
     */private void onMouseDragged(MouseEvent e) {
        int row = gridView.pixelToRow(e.getY());
        int col = gridView.pixelToCol(e.getX());
        if (row < 0 || col < 0) return;

        switch (editMode) {
            case BRUSH -> {
                // 1. Applique d'abord l'état si nécessaire
                if (!maskMode) {
                    grid.setCell(row, col, paintState);
                } else {
                    if (paintState != CellState.EMPTY) {
                        grid.setCell(row, col, paintState);
                    }
                }

                // 2. Force le masque à 'true' pendant le glisser de souris (drag)
                if (maskMode) {
                    var cell = grid.getCell(row, col);
                    if (cell != null && cell.isAlive()) {
                        cell.setMasked(true); 
                    }
                }
                gridView.redraw();
            }
            case ZONE -> gridView.drawZoneSelection(row, col);
            case INDIVIDUAL -> { /* handled on press only */ }
            case ZONETYPE -> { // <-- Nouveau cas
                var cell = grid.getCell(row, col);
                ZoneType selectedZone = zoneTypeCombo.getValue();
                if (cell != null && selectedZone != null) {
                    cell.setZoneType(selectedZone);
                }
                gridView.redraw();
            }
        }
    }
/**
     * Handles a mouse-released event on the grid canvas (finalises zone fill).
     *
     * @param e the mouse event
     */
    private void onMouseReleased(MouseEvent e) {
        if (editMode != EditMode.ZONE) return;
        int row = gridView.pixelToRow(e.getY());
        int col = gridView.pixelToCol(e.getX());
        if (row < 0 || col < 0) return;
        
        int r1 = gridView.getDragStartRow(), c1 = gridView.getDragStartCol();
        if (r1 < 0) return;
        
        // 1. On remplit d'abord la zone avec l'état sélectionné (ex: Susceptible, Infected...)
        // Si on veut juste mettre un masque sur des cellules existantes sans changer leur état,
        // on évite de vider la grille en vérifiant si paintState n'est pas "Empty" par défaut.
        if (!maskMode || paintState != CellState.EMPTY) {
            grid.fillArea(r1, c1, row, col, paintState);
        }
        
        // 2. Si le mode masque est activé, on applique le masque sur toute la zone sélectionnée
        if (maskMode) {
            // Détermination des bornes min/max pour gérer le glisser de souris dans tous les sens
            int startRow = Math.min(r1, row);
            int endRow   = Math.max(r1, row);
            int startCol = Math.min(c1, col);
            int endCol   = Math.max(c1, col);
            
            for (int r = startRow; r <= endRow; r++) {
                for (int c = startCol; c <= endCol; c++) {
                    var cell = grid.getCell(r, c);
                    if (cell != null && cell.isAlive()) {
                        cell.setMasked(true); // Applique le masque dans la zone
                    }
                }
            }
        }
        
        gridView.setDragStartRow(-1);
        gridView.redraw();
        
        // Mise à jour du message de statut pour être plus précis
        if (maskMode && paintState != CellState.EMPTY) {
            setStatus("Zone filled with " + paintState + " and masked.");
        } else if (maskMode) {
            setStatus("Masks applied to the zone.");
        } else {
            setStatus("Zone filled with " + paintState);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Simulation control
    // ─────────────────────────────────────────────────────────────────────────

    /** Starts the automatic simulation loop. */
    private void startSimulation() {
        running = true;
        animationTimer.start();
        setStatus("Simulation running…");
    }

    /** Pauses the automatic simulation loop. */
    private void pauseSimulation() {
        running = false;
        animationTimer.stop();
        setStatus("Paused at step " + engine.getStepCount());
    }

    /** Advances one step and refreshes the UI. */
    private void doStep() {
        engine.step();
        gridView.redraw();
        statsPanel.refresh();
        setStatus("Step " + engine.getStepCount());
    }

    /** Resets the simulation to an empty grid. */
    private void resetSimulation() {
        pauseSimulation();
        engine.reset();
        gridView.redraw();
        statsPanel.refresh();
        setStatus("Reset. Draw cells and press Play.");
    }

    /**
     * Builds the animation timer that drives automatic stepping.
     */
    private void buildAnimationTimer() {
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastStepTime >= stepIntervalNanos) {
                    lastStepTime = now;
                    doStep();
                }
            }
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Disease / paint state callbacks
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Guard flag: true while a preset is being loaded into the UI controls.
     * Prevents the slider/checkbox listeners from overwriting the preset values
     * before they are fully applied.
     */
    private boolean updatingControls = false;

    /** Reacts to disease preset selection. */
    private void onDiseaseSelected() {
        String sel = diseaseCombo.getValue();
        currentDisease = switch (sel) {
            case "COVID-Like" -> Disease.covidLike();
            default           -> Disease.influenza();
        };
        grid.setDisease(currentDisease);
        // Block applyDiseaseParams() while we sync the controls to the preset
        updatingControls = true;
        transmissionSlider   .setValue(currentDisease.getTransmissionRate());
        mortalitySlider      .setValue(currentDisease.getMortalityRate());
        radiusSlider         .setValue(currentDisease.getTransmissionRadius());
        incubationSlider     .setValue(currentDisease.getIncubationPeriod());
        infectionDurSlider   .setValue(currentDisease.getInfectionDuration());
        immunitySlider       .setValue(currentDisease.getImmunityDuration());
        vaccineEfficacySlider.setValue(currentDisease.getVaccineEfficacy());
        vaccineImmunitySlider.setValue(currentDisease.getVaccineImmunityDuration());
        maskInwardSlider     .setValue(currentDisease.getMaskInwardEfficacy());
        maskOutwardSlider    .setValue(currentDisease.getMaskOutwardEfficacy());
        airborneCheck        .setSelected(currentDisease.isAirborne());
        updatingControls = false;
        setStatus("Disease set to: " + currentDisease);
    }

    /** Applies slider/checkbox values back to the current disease (Custom mode). */
    private void applyDiseaseParams() {
        // Skip if we are in the middle of loading a preset into the controls
        if (updatingControls) return;
        currentDisease.setTransmissionRate(transmissionSlider.getValue());
        currentDisease.setMortalityRate(mortalitySlider.getValue());
        currentDisease.setTransmissionRadius((int) radiusSlider.getValue());
        currentDisease.setIncubationPeriod((int) incubationSlider.getValue());
        currentDisease.setInfectionDuration((int) infectionDurSlider.getValue());
        currentDisease.setImmunityDuration((int) immunitySlider.getValue());
        currentDisease.setVaccineEfficacy(vaccineEfficacySlider.getValue());
        currentDisease.setVaccineImmunityDuration((int) vaccineImmunitySlider.getValue());
        currentDisease.setAirborne(airborneCheck.isSelected());
        grid.setDisease(currentDisease);
    }

    private void applyMovementProbability(double newProb) {
        if (grid == null) return;
        
        int width = grid.getWidth();
        int height = grid.getHeight();
        
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                healthradar.model.Cell cell = grid.getCell(r, c);
                if (cell != null && cell.isAlive()) {
                    cell.setMoveProbability(newProb);
                }
            }
        }
        // Optionnel : un petit message de statut pour l'utilisateur
        setStatus("Movement probability updated to " + String.format("%.2f", newProb));
    }

    /** Reacts to paint state combo selection. */
    private void onPaintStateSelected() {
        paintState = switch (paintStateCombo.getValue()) {
            case "Vaccinated" -> CellState.VACCINATED;
            case "Exposed"    -> CellState.EXPOSED;
            case "Infected"   -> CellState.INFECTED;
            case "Recovered"  -> CellState.RECOVERED;
            case "Dead"       -> CellState.DEAD;
            case "Empty"      -> CellState.EMPTY;
            default           -> CellState.SUSCEPTIBLE;
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Random populate
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Randomly fills the grid based on the susceptible/infected percentage sliders.
     */
    private void randomPopulate() {
        int total  = grid.getWidth() * grid.getHeight();
        int sCount = (int)(total * susceptibleSlider.getValue() / 100.0);
        int iCount = (int)(total * infectedSlider   .getValue() / 100.0);
        grid.clear();
        grid.randomPopulate(sCount, iCount);
        applyMovementProbability(moveProbSlider.getValue());
        engine = new SimulationEngine(grid);
        statsPanel.setEngine(engine);
        gridView.redraw();
        statsPanel.refresh();
        setStatus("Grid randomly populated: " + sCount + " susceptible, " + iCount + " infected.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Save / Load
    // ─────────────────────────────────────────────────────────────────────────

    /** Saves the current simulation to a binary file chosen by the user. */
    private void saveSimulation() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Simulation");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("HealthRadar save (*.hrs)", "*.hrs"));
        File file = fc.showSaveDialog(primaryStage);
        if (file == null) return;
        try {
            SimulationSerializer.save(engine, file.toPath());
            setStatus("Saved to " + file.getName());
        } catch (IOException ex) {
            alert("Save error", ex.getMessage());
        }
    }

    /** Loads a simulation from a binary file chosen by the user. */
    private void loadSimulation() {
        pauseSimulation();
        FileChooser fc = new FileChooser();
        fc.setTitle("Load Simulation");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("HealthRadar save (*.hrs)", "*.hrs"));
        File file = fc.showOpenDialog(primaryStage);
        if (file == null) return;
        try {
            engine = SimulationSerializer.load(file.toPath());
            grid   = engine.getGrid();
            gridView.setGrid(grid);
            statsPanel.setEngine(engine);
            gridView.redraw();
            statsPanel.refresh();
            setStatus("Loaded from " + file.getName() + " (step " + engine.getStepCount() + ")");
        } catch (IOException ex) {
            alert("Load error", ex.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UI helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a styled action button.
     *
     * @param text  button label
     * @param color CSS hex background colour
     * @return styled Button
     */
    private Button styledButton(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + color + "; -fx-text-fill:white;"
                + "-fx-font-size:11px; -fx-background-radius:4;");
        return b;
    }

    /**
     * Creates a white-text label.
     *
     * @param text label text
     * @return styled Label
     */
    private Label whiteLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.WHITE);
        l.setFont(Font.font(11));
        return l;
    }

    /**
     * Creates a section header label for the sidebar.
     *
     * @param text section title
     * @return styled Label
     */
    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.LIGHTBLUE);
        l.setFont(Font.font("System Bold", 13));
        return l;
    }

    /**
     * Creates a styled toggle button bound to an {@link EditMode}.
     *
     * @param text  button label
     * @param mode  the EditMode this button represents
     * @param group the ToggleGroup to add this button to
     * @return styled ToggleButton
     */
    private ToggleButton modeToggle(String text, EditMode mode, ToggleGroup group) {
        ToggleButton tb = new ToggleButton(text);
        tb.setToggleGroup(group);
        tb.setStyle("-fx-background-color:#2c3e50; -fx-text-fill:white; -fx-font-size:11px;");
        tb.selectedProperty().addListener((obs, o, selected) -> {
            if (selected) {
                editMode = mode;
                tb.setStyle("-fx-background-color:#3498db; -fx-text-fill:white; -fx-font-size:11px;");
            } else {
                tb.setStyle("-fx-background-color:#2c3e50; -fx-text-fill:white; -fx-font-size:11px;");
            }
        });
        return tb;
    }

    /**
     * Adds a labelled slider to a VBox and returns the slider.
     *
     * @param label label text
     * @param min   minimum slider value
     * @param max   maximum slider value
     * @param init  initial value
     * @param box   VBox to add the control to
     * @return the created Slider
     */
    private Slider labelledSlider(String label, double min, double max, double init, VBox box) {
        Label lbl = new Label(label);
        lbl.setTextFill(Color.LIGHTGRAY);
        lbl.setFont(Font.font(11));
        Slider s = new Slider(min, max, init);
        s.setShowTickMarks(false);
        s.setPrefWidth(240);
        box.getChildren().addAll(lbl, s);
        return s;
    }

    /** @return a thin horizontal separator styled for dark background */
    private Separator separator() {
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:#2c3e50;");
        return sep;
    }

    /**
     * Updates the bottom status bar text.
     *
     * @param msg status message
     */
    private void setStatus(String msg) {
        statusLabel.setText(msg);
    }

    /**
     * Exports the simulation statistics history as a PNG chart file.
     * The file is saved in the working directory with a name based on the
     * current step count. A FileChooser lets the user pick a custom location.
     */
    private void exportChart() {
        if (engine == null || engine.getHistory().isEmpty()) {
            alert("No data", "Start and run the simulation first to generate chart data.");
            return;
        }
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Export chart as PNG");
        fc.setInitialFileName(ChartExporter.defaultPath(engine).getFileName().toString());
        fc.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("PNG image (*.png)", "*.png"));
        java.io.File file = fc.showSaveDialog(primaryStage);
        if (file == null) return;
        try {
            ChartExporter.export(engine, file.toPath());
            setStatus("Chart saved: " + file.getName());
            // Offer to open the file
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Chart exported");
            info.setHeaderText(null);
            info.setContentText("Chart saved to:\n" + file.getAbsolutePath());
            info.showAndWait();
        } catch (java.io.IOException ex) {
            alert("Export error", ex.getMessage());
        }
    }

    /**
     * Opens the Settings modal and applies changes if the user clicks Apply.
     * Pauses the simulation while the dialog is open.
     */
    private void openSettings() {
        pauseSimulation();
        // Convert sidebar % sliders to actual counts for the dialog
        int totalCells = grid.getWidth() * grid.getHeight();
        int curS = (int)(totalCells * susceptibleSlider.getValue() / 100.0);
        int curI = (int)(totalCells * infectedSlider.getValue()    / 100.0);
        ConfigPanel.ConfigResult current = new ConfigPanel.ConfigResult(
                grid.getWidth(), grid.getHeight(), grid.isToroidal(),
                currentDisease,
                curS, curI,
                (int)(stepIntervalNanos / 1_000_000),
                (int) gridView.getCellSize(),
                false);

        ConfigPanel.show(primaryStage, current, result -> {
            currentDisease = result.disease();

            if (result.restart()) {
                // ── Apply & Restart : recrée la grille et repeuple ────────
                grid = new Grid(result.gridWidth(), result.gridHeight(),
                        result.toroidal(), currentDisease, 0);
                grid.randomPopulate(result.susceptibleCount(), result.infectedCount());
                engine = new SimulationEngine(grid);
                gridView.setGrid(grid);
                statsPanel.setEngine(engine);
                setStatus("Grid restarted: " + result.gridWidth() + "×"
                        + result.gridHeight() + "  " + currentDisease.getName());
            } else {
                // ── Apply Only : garde la grille actuelle ─────────────────
                if (result.gridWidth()  != grid.getWidth()
                 || result.gridHeight() != grid.getHeight()
                 || result.toroidal()   != grid.isToroidal()) {
                    // Dimensions changées : nouvelle grille vide
                    grid = new Grid(result.gridWidth(), result.gridHeight(),
                            result.toroidal(), currentDisease, 0);
                    engine = new SimulationEngine(grid);
                    gridView.setGrid(grid);
                    statsPanel.setEngine(engine);
                    setStatus("Grid resized to " + result.gridWidth() + "×"
                            + result.gridHeight() + " — use Populate to fill it.");
                } else {
                    // Mêmes dimensions : applique la maladie sur la grille en cours
                    grid.setDisease(currentDisease);
                    setStatus("Settings applied (grid preserved): "
                            + currentDisease.getName());
                }
            }

            // ── Toujours : taille cellule + vitesse + sync sliders ────────
            gridView.setCellSize(result.cellSize());
            stepIntervalNanos = (long)(result.stepDelayMs() * 1_000_000L);
            speedSlider.setValue(1000.0 / result.stepDelayMs());

            updatingControls = true;
            transmissionSlider   .setValue(result.disease().getTransmissionRate());
            mortalitySlider      .setValue(result.disease().getMortalityRate());
            radiusSlider         .setValue(result.disease().getTransmissionRadius());
            incubationSlider     .setValue(result.disease().getIncubationPeriod());
            infectionDurSlider   .setValue(result.disease().getInfectionDuration());
            immunitySlider       .setValue(result.disease().getImmunityDuration());
            vaccineEfficacySlider.setValue(result.disease().getVaccineEfficacy());
            vaccineImmunitySlider.setValue(result.disease().getVaccineImmunityDuration());
            maskInwardSlider     .setValue(result.disease().getMaskInwardEfficacy());
            maskOutwardSlider    .setValue(result.disease().getMaskOutwardEfficacy());
            airborneCheck        .setSelected(result.disease().isAirborne());
            updatingControls = false;

            int _total = result.gridWidth() * result.gridHeight();
            susceptibleSlider.setValue(_total == 0 ? 0 : result.susceptibleCount() * 100.0 / _total);
            infectedSlider   .setValue(_total == 0 ? 0 : result.infectedCount()    * 100.0 / _total);

            gridView.redraw();
            statsPanel.refresh();
        });
    }

    /**
     * Shows a simple error alert dialog.
     *
     * @param title   dialog title
     * @param message error message
     */
    private void alert(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setContentText(message);
        a.showAndWait();
    }
}
