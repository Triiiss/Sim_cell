# HealthRadar

## Project Overview

HealthRadar is a Java / JavaFX project developed for the PGL end-of-year project in ING1-GI.

The goal is to simulate disease propagation in a 2D city grid. Cells represent urban zones, and people can later move inside the grid while carrying their own health state. This makes it possible to observe how diseases spread depending on population density, immunity, incubation, recovery, mortality, and local clusters.

The project is based on the official theme: **2D cell simulation**.

---

## Main Idea

The target simulation is based on:

- a 2D city grid where each cell can represent an outdoor zone or the entrance to a special place;
- special places such as a mall, train station, school, hospital, park, or workplace;
- optional indoor grids inside important places, so the simulation can show disease spread inside one building and across the whole city;
- several disease profiles with different infection behavior;
- people moving between city cells and indoor place cells as agents;
- cell risk computed from local population, infected people, density, and capacity;
- disease-specific vaccines that can increase immunity only against targeted diseases;
- a command-line simulation with a menu first, then a JavaFX interface on top of the same model.

This repository currently keeps a lighter base so the team can agree on tasks before implementing the full model.

---

## Multi-Scale Simulation Idea

The final idea is not only to show one flat grid. HealthRadar should be able to simulate disease propagation at two scales:

- the city scale, where the user sees the full city map;
- the place scale, where the user can enter a specific location such as a mall, station, school, or hospital and inspect its interior.

The city would keep one main `Grid`. Some cells in this grid may point to a `Place`. A `Place` can contain its own indoor `Grid`.

Example:

```text
City
└── cityGrid
    ├── street cell
    ├── park cell
    ├── station entrance cell -> TrainStation
    │                         └── indoorGrid
    └── mall entrance cell -> Mall
                         └── indoorGrid
                             ├── entrance cell
                             ├── shop cell
                             └── food court cell
```

People are the objects that carry diseases. A person can be located:

- directly in a city cell;
- inside a place, in one of its indoor cells.

At each simulated day, the engine should update the whole world, not only the grid currently displayed by the user. For example, the user may be viewing the city map while infections are still being calculated inside the mall and train station in the background.

A command-line menu can later make this interactive:

```text
1. Advance one day
2. Select city map
3. Show global statistics
4. List places
5. Select place indoor map
6. Show selected view
7. Quit
```

The selected view stays active. If the user selects the mall, every future `Advance one day` updates the full simulation and then automatically shows the mall indoor map again. If the user selects the city, the city map is shown after each day.

This keeps the project close to the official 2D cell simulation theme while making the model more interesting than a single flat grid.

---

## Current Implemented Base

The current code contains:

- Maven project structure;
- `CellState` with `HEALTHY`, `INFECTED`, and `RECOVERED`;
- `Cell`, representing one position in a grid and optionally containing people or a special place;
- `Grid`, storing the 2D map;
- `DiseaseType`, currently with `FLU` and `COVID_LIKE`;
- `DiseaseProfile`, storing basic disease parameters;
- `Person` and `PersonState`, storing who carries disease inside cells;
- `Place` and `PlaceType`, representing locations such as a mall, station, school, hospital, workplace, or park;
- `City`, coordinating one city grid, several places, and indoor grids;
- `Main`, a command-line menu that can advance the simulation, show the city map, show global statistics, list places, and open indoor place maps.

The current multi-scale implementation is still a local prototype. It validates the architecture idea before adding a richer simulation engine and JavaFX.

---

## Target Core Model

### City

`City` should represent the full simulation world.

It may contain:

- a main city `Grid`;
- a list of important `Place` objects;
- the list of available diseases;
- global settings such as mask policy;
- population statistics helpers.

`City` should not create a second grid structure that replaces `Grid`. It should own and coordinate the existing `Grid` object.

### Place

`Place` would represent an important location in the city.

Examples:

- mall;
- train station;
- school;
- workplace;
- hospital;
- park.

A place may contain:

- a name;
- a place type;
- an optional indoor `Grid`;
- capacity and density information;
- statistics for the people currently inside.

Some places can stay simple and only exist as one city cell. More important places, such as the mall or train station, can have an indoor grid so the user can inspect disease propagation inside them.

### Grid and Cell

`Grid` stores a 2D map. It can be used for the main city map or for the indoor map of a specific place.

Each `Cell` currently stores a simple cell state. Later, a cell may also contain:

- a `ZoneType`, for example residential, metro, park, school, workplace, hospital;
- a `PopulationDensity` multiplier;
- a maximum capacity;
- a list of `Person` objects currently inside the cell.
- an optional link to a `Place`, when the cell is an entrance or important location.

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

1. alive people may move to a neighboring cell, enter a place, leave a place, or stay in place;
2. each healthy person checks the disease risk in their current cell;
3. infection probability depends on disease infection rate, cell density, local risk, personal immunity, and disease-specific vaccines;
4. incubating people become sick after the incubation period;
5. sick people may recover, die, or remain sick;
6. recovered people gain immunity, then immunity may slowly decrease over time;
7. statistics are recalculated for the city and for each place.

A possible infection formula is:

```text
infectionProbability = diseaseInfectionRate * cellDensity * cellRisk * (1 - effectiveImmunity)
```

`cellRisk` can be based on the number of contagious people in the cell. If the cell is a cluster, the risk can be increased. `effectiveImmunity` may include natural immunity, mask protection, and vaccine protection for the current disease.

The simulation engine should update every active grid each day: the city grid and the indoor grids of places. The displayed view is only a view; it should not decide which parts of the simulation are active.

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

Quick non-interactive demo:

```bash
java -cp target/classes com.healthradar.Main --demo
```

---

## Planned JavaFX Application

The future JavaFX application should use the same model and engine.

Planned interactions:

- create a normal city in manual mode;
- change simulation parameters while the simulation is running;
- pause, resume, step forward, and change speed;
- click a cell to inspect zone information, people, diseases, and local risk;
- click a special place to open its indoor grid;
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
- add `Place` and indoor grids for important locations such as malls, stations, schools, and hospitals;
- make the CLI menu able to switch between city view and place view;
- add mutation as a bonus feature;
- build the JavaFX interface after the model is stable.
