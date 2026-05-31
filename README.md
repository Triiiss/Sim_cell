# HealthRadar

## Project Overview

HealthRadar is a Java project developed for the PGL end-of-year project in ING1-GI.

The goal is to build a 2D cell simulation that shows how a disease can spread inside a simplified city. The project starts with a simple grid-based model, then can evolve step by step toward a more complete urban simulation.

The project is based on the official theme: **2D cell simulation**.

---

## Main Idea

The first version of the project is based on a 2D grid.

Each cell represents one position in the simulated city. A cell can currently be:

- `HEALTHY`
- `INFECTED`
- `RECOVERED`

The simulation will later use these cells to model disease propagation. The first objective is to keep a clean and understandable base before adding more advanced rules.

---

## Current Base

The current code contains the first technical base of the project:

- `CellState`: possible states of a cell;
- `Cell`: one position of the grid;
- `Grid`: the 2D map containing cells;
- `Main`: application entry point.

This base is intentionally simple. It allows the team to agree on the structure before adding the simulation engine, disease parameters, user actions, or the JavaFX interface.

---

## Merged Project Direction

The final project can combine two ideas:

- a simple cell-based simulation where disease spreads across a 2D grid;
- a more urban interpretation where cells can later represent places in a city, such as residential areas, schools, hospitals, parks, or transport zones.

For now, the repository keeps only the basic grid structure. The city and population behavior should be added later only after the team agrees on the tasks.

---

## Planned Disease Model

The application may support several disease profiles.

Examples:

### Flu

A first simple disease for testing the simulation.

Possible characteristics:

- medium infection rate;
- short duration;
- low danger level.

### Covid-like Virus

A more contagious disease for comparing different spread behaviors.

Possible characteristics:

- higher infection rate;
- longer duration;
- stronger propagation.

Other diseases can be added later if needed.

---

## Planned Simulation Rules

The simulation engine is not fully implemented yet in this light base.

The planned behavior is:

1. infected cells may contaminate nearby healthy cells;
2. infected cells may recover after several steps;
3. recovered cells may become resistant or immune depending on the chosen rules;
4. statistics can be updated after each simulation step.

A future simplified formula could be:

```text
infectionRisk = diseaseRate * neighborInfluence * protectionFactor
```

The exact rules should be defined by the team before implementation.

---

## Future Improvements

Possible next steps:

- implement the first simulation step logic;
- add disease profiles;
- add statistics;
- add user actions on the grid;
- add save/load later;
- create a JavaFX interface after the model is stable;
- optionally add city zones, population density, or person-based behavior in a later version.

---

## How to Run

Compile the project:

```bash
javac -d target/classes src/main/java/com/healthradar/*.java
```

Run the main class:

```bash
java -cp target/classes com.healthradar.Main
```
