# HealthRadar

HealthRadar is a Java / JavaFX project for the ING1-GI PGL 2D cells assignment.

The project simulates disease propagation on a configurable 2D grid using a
stochastic cellular automaton. Each grid slot is either empty or contains one
simulated person. People move, become exposed, infected, recovered, vaccinated,
or dead according to probabilistic epidemiological rules derived from the SVEIRD
model. Urban zone types modify local transmission risk.

## Assignment Scope

The official assignment asks for a graphical Java application that simulates
cells in a 2D plane. The simulation must evolve according to time, the
environment, and user actions.

For HealthRadar, the chosen context is disease propagation in a city:

- the grid is the city map;
- each non-empty cell represents one person on that map;
- disease transmission depends on nearby cells, disease parameters,
  protection, movement, and zone risk;
- the user can edit the grid, paint urban zones, and observe live statistics.

## Run with the Makefile (Linux / macOS)

Install OpenJFX if needed:

```bash
sudo apt install openjfx        # Debian / Ubuntu / Mint
```

From the `healthradar/` directory:

```bash
# Compile + run the JavaFX application
make run

# Compile + run the terminal application
make run-terminal

# Build JARs only
make jar
make jar-terminal

# Generate JavaDoc
make doc

# Clean build artefacts
make clean
```

If JavaFX is installed in a non-standard location, override the path:

```bash
export JAVAFX_LIB=/path/to/javafx-sdk/lib
make run
```

## Run with Maven (Windows / cross-platform)

From the repository root:

```bash
mvn clean compile

# JavaFX application
mvn javafx:run

# Terminal application
mvn clean compile
java -cp target/classes healthradar.TerminalApp

# Package
mvn clean package
```

## Features

### Epidemiological model (SVEIRD)

Cell states:

- `EMPTY` — no person
- `SUSCEPTIBLE` — healthy, can be infected
- `VACCINATED` — protected, reduced infection probability with waning immunity
- `EXPOSED` — incubating, not yet contagious (optionally pre-symptomatic)
- `INFECTED` — contagious
- `RECOVERED` — temporarily immune
- `DEAD` — no longer active

Disease parameters:

- transmission rate
- contact or airborne transmission mode
- airborne radius with **distance attenuation** (probability decreases as
  `baseRate / (distance × attenuationFactor)`)
- incubation period
- infection duration
- mortality rate
- temporary immunity duration
- pre-symptomatic contagion (EXPOSED cells can transmit at reduced rate)
- vaccine efficacy and vaccine immunity duration (waning immunity)
- mask inward and outward efficacy

### Urban zone layer

Each cell independently carries a zone type that multiplies the local
transmission rate:

| Zone | Multiplier |
|---|---|
| Residential | ×1.0 (reference) |
| Work | ×1.4 |
| Commercial | ×1.2 |
| Education | ×1.5 |
| Healthcare | ×1.8 |
| Transport | ×2.2 |
| Empty space | ×1.0 (neutral) |

### JavaFX graphical application

- Scrollable grid canvas with configurable cell size.
- Left rail with edit modes: **Brush**, **Fill rectangle**, **Single cell**,
  **Zone layer**, **Inspect** (click a cell to read its data without modifying it).
- Mask paint mode: toggle mask flag on any cell independently of its state.
- Right panel with live statistics (counts, percentage bar, time-series chart).
- Cell inspector: position, state, zone, state age, resistance, movement
  probability, mask status, zone risk multiplier.
- Settings dialog with two apply modes:
  - **Apply** — applies disease and display parameters without resetting the grid.
  - **Apply & Restart** — resets and repopulates the grid.
- Disease library: save and load custom disease configurations (CSV, upsert by name).
- Export statistics chart to PNG (1200 × 700 px, no external dependency).

### Terminal application

- Full simulation without JavaFX.
- ANSI 256-colour rendering: each cell displays **3 characters**
  (state letter + mask indicator `m` or zone character `r/w/c/e/h/T`).
- Interactive grid editor: place, remove, fill rectangles, vaccinate all,
  toggle mask, save layout.
- Step-by-step mode (`p` opens pause menu inline, `q` returns to auto-play).
- Load dialog lists all `.hrs` files found in the `save/` subdirectory;
  files can be selected by number.
- Watcher thread detects ENTER during auto-play without blocking simulation.

### Save / load

- `.hrs` files use a **JSON text format** (no external library).
- The full simulation state is preserved: grid dimensions, topology, disease
  parameters, each cell's state, state age, resistance, movement probability,
  mask flag, and **zone type**.
- Empty cells that carry a zone are also saved so the spatial zone layout is
  fully restored on load.
- Backward-compatible: missing fields fall back to sensible defaults so older
  `.hrs` files remain loadable.

### Simulation controls

- Play / Pause / Step / Reset.
- Step delay from 50 ms to 10 s.
- Toroidal or bounded grid topology (switchable at runtime).
- Random populate with configurable susceptible and infected counts.
- Clear grid.

## Project Structure

```
.
├── README.md
└── healthradar/
    ├── Makefile
    ├── MANIFEST.MF
    ├── MANIFEST-Terminal.MF
    ├── src/main/java/healthradar/
    │   ├── App.java               — JavaFX entry point
    │   ├── Launcher.java          — alternate entry point for IDEs / Maven
    │   ├── TerminalApp.java       — standalone terminal application
    │   ├── controller/
    │   │   └── MainController.java
    │   ├── model/
    │   │   ├── Cell.java
    │   │   ├── CellState.java
    │   │   ├── Disease.java
    │   │   ├── Grid.java
    │   │   ├── SimulationEngine.java
    │   │   └── ZoneType.java
    │   ├── view/
    │   │   ├── ConfigPanel.java
    │   │   ├── EditMode.java
    │   │   ├── GridView.java
    │   │   └── StatsPanel.java
    │   └── io/
    │       ├── ChartExporter.java
    │       ├── DiseaseLibrary.java
    │       └── SimulationSerializer.java
    └── src/main/resources/healthradar/
        └── app.css
```

## Requirements

- JDK 17 or later (JDK 21 recommended).
- **Linux / macOS (Makefile):** OpenJFX installed system-wide.
- **Windows / all platforms (Maven):** Maven resolves JavaFX automatically.


## IntelliJ Setup

Open the repository root (not the `healthradar/` subfolder). Reload Maven,
then run:

```
healthradar.Launcher
```

## Save Files

`.hrs` files are JSON text snapshots of the full simulation state. Place them
in a `scenario/` subdirectory next to the JAR so the terminal loader lists them
automatically. The JavaFX application uses a file chooser and accepts any path.

Scenario files demonstrating herd immunity, social distancing, disease waves,
and urban zone effects are provided separately.

## Known Limitations

- The Makefile assumes a Linux OpenJFX installation. Use Maven on Windows.
- JavaDoc should be generated and committed before the final defence.
- The team must be able to compile and run the project from the command line
  during the defence.
