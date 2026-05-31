# HealthRadar

## Project Overview

HealthRadar is a Java / JavaFX project developed for the PGL end-of-year project in ING1-GI.

The goal is to simulate disease propagation in a 2D city grid. Cells represent urban zones, and people can later move inside the grid while carrying their own health state. This makes it possible to observe how diseases spread depending on population density, immunity, incubation, recovery, mortality, and local clusters.

The project is based on the official theme: **2D cell simulation**.

---

## Main Idea

The target simulation is based on:

- a 2D grid where each cell can represent a place in the city;
- several disease profiles with different infection behavior;
- people moving between cells as agents;
- cell risk computed from local population, infected people, density, and capacity;
- disease-specific vaccines that can increase immunity only against targeted diseases;
- a command-line simulation first, then a JavaFX interface on top of the same model.

This repository currently keeps a lighter base so the team can agree on tasks before implementing the full model.

---

## Current Implemented Base

The current code contains:

- Maven project structure;
- `CellState` with `HEALTHY`, `INFECTED`, and `RECOVERED`;
- `Cell`, representing one position in the grid;
- `Grid`, storing the 2D map;
- `DiseaseType`, currently with `FLU` and `COVID_LIKE`;
- `DiseaseProfile`, storing basic disease parameters;
- `Main`, a small command-line entry point that loads and displays a disease profile.

The advanced city/person simulation is the objective, not fully implemented in this lighter base yet.

---

## Target Core Model

### City

`City` should represent the full simulation world.

It may contain:

- a `Grid` of cells;
- the list of available diseases;
- global settings such as mask policy;
- population statistics helpers.

### Grid and Cell

`Grid` stores the 2D map.

Each `Cell` currently stores a simple cell state. Later, a cell may also contain:

- a `ZoneType`, for example residential, metro, park, school, workplace, hospital;
- a `PopulationDensity` multiplier;
- a maximum capacity;
- a list of `Person` objects currently inside the cell.

If the number of alive people is greater than or equal to the cell capacity, the cell could become a cluster and local infection risk could increase.

### Person

`Person` would represent one inhabitant of the city.

A person could have:

- a name;
- an age;
- an immunity value;
- a mask flag;
- a health state;
- an optional disease;
- optional disease-specific vaccination records;
- a counter for infected days;
- a fragility score based on age and immunity.

Possible person states:

- `HEALTHY`
- `INCUBATING`
- `SICK`
- `RECOVERED`
- `DEAD`

Dead people should not move, infect others, or count as active population.

### DiseaseProfile

`DiseaseProfile` stores the behavior of a disease.

It currently includes:

- disease type;
- display name;
- infection probability;
- infection radius;
- incubation duration;
- infection duration;
- recovery probability;
- mortality probability.

### Vaccination Model

Vaccination should be modeled in a generic way, because the simulation may contain several diseases and several vaccines.

Instead of adding fixed fields such as `vaccinatedAgainstCovid`, the project can later use a vaccine profile:

- vaccine name;
- target disease type;
- immunity boost;
- optional protection duration;
- optional protection decay over time.

A person may receive several vaccines. During infection risk calculation, only vaccines targeting the current disease should increase immunity.

Example:

- a person has base immunity `0.30`;
- the person wears a mask, adding `0.15`;
- the person received a Covid-like vaccine, adding `0.40` only against `COVID_LIKE`;
- effective immunity against `COVID_LIKE` becomes `0.85`;
- effective immunity against `FLU` stays `0.45`, because the Covid-like vaccine does not target Flu.

This keeps the system extensible for future diseases and vaccines.

---

## Disease Types

The current implemented disease types are:

### Flu

A common disease with medium propagation.

Characteristics:

- medium infection rate;
- short incubation;
- short infection duration;
- low mortality;
- useful as the first test disease.

### Covid-like Virus

A stronger disease with wider propagation.

Characteristics:

- different infection rate;
- longer incubation;
- longer infection duration;
- higher mortality than flu;
- larger influence radius.

Future disease profiles may include stomach virus, lice, or custom diseases created by the user.

---

## Target Simulation Rules

The full simulation should run step by step or day by day.

At each simulated step:

1. alive people may move to a neighboring cell or stay in place;
2. each healthy person checks the disease risk in their current cell;
3. infection probability depends on disease infection rate, cell density, local risk, personal immunity, and disease-specific vaccines;
4. incubating people become sick after the incubation period;
5. sick people may recover, die, or remain sick;
6. recovered people gain immunity, then immunity may slowly decrease over time;
7. statistics are recalculated.

A possible infection formula is:

```text
infectionProbability = diseaseInfectionRate * cellDensity * cellRisk * (1 - effectiveImmunity)
```

`cellRisk` can be based on the number of contagious people in the cell. If the cell is a cluster, the risk can be increased. `effectiveImmunity` may include natural immunity, mask protection, and vaccine protection for the current disease.

---

## How to Run

Compile with Java, then run the main class.

```bash
javac -d target/classes src/main/java/com/healthradar/*.java
```

Default disease:

```bash
java -cp target/classes com.healthradar.Main
```

Specific disease:

```bash
java -cp target/classes com.healthradar.Main COVID_LIKE
```

---

## Planned JavaFX Application

The future JavaFX application should use the same model and engine.

Planned interactions:

- create a normal city in manual mode;
- change simulation parameters while the simulation is running;
- pause, resume, step forward, and change speed;
- click a cell to inspect zone information, people, diseases, and local risk;
- click a person to inspect age, immunity, state, disease, and fragility;
- click a disease to inspect its parameters;
- show disease colors on the grid;
- display clusters differently;
- show live statistics and charts.

---

## Future Improvements

Possible next steps:

- implement the first simulation step logic;
- add statistics;
- support several diseases at the same time;
- add user-created disease parameters;
- add save/load of simulation states;
- store a simulation history for preview and rewind;
- implement `VaccineProfile` and vaccination records for disease-specific immunity;
- add treatment actions;
- improve urban movement with home, work, school, leisure, and transport locations;
- add mutation as a bonus feature;
- build the JavaFX interface after the model is stable.
