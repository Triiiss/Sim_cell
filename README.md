# HealthRadar

## Project Overview

HealthRadar is a Java / JavaFX project developed for the PGL end-of-year project in ING1-GI.

The goal is to simulate the spread of diseases in an urban environment using a 2D grid.  
Each cell of the grid represents a simplified urban zone or individual state. The simulation allows users to observe how different diseases spread over time depending on infection probability, recovery, protection, distance, and user actions.

The project is based on the official theme: **2D cell simulation**.

---

## Main Idea

The city is represented as a 2D grid.

Each cell can have one of several states:

- `HEALTHY`
- `INFECTED`
- `RECOVERED`
- `DEAD`
- `PROTECTED`

The disease spreads from infected cells to nearby healthy cells according to rules and probabilities.  
The user can interact with the grid, modify cells, launch the simulation, pause it, and observe statistics in real time.

---

## Disease Types

The application will allow the user to simulate different types of diseases.  
Each disease has its own behavior and parameters.

### Flu

A common disease with medium propagation.

Characteristics:

- medium infection rate
- medium recovery rate
- low death rate
- short influence radius
- useful as the first simple disease for the MVP

### Covid-like Virus

A more contagious disease with stronger propagation.

Characteristics:

- high infection rate
- longer infection duration
- possible immunity after recovery
- medium death rate
- larger influence radius

### Stomach Virus

A disease that spreads quickly in local areas.

Characteristics:

- high local infection rate
- short duration
- low death rate
- limited propagation radius
- creates strong local outbreaks

### Lice

Not a virus, but useful to represent a different type of spread.

Characteristics:

- spreads only through direct contact
- no death rate
- can be removed by treatment
- useful for school or close-contact simulations

---

## Simulation Rules

At each simulation step:

1. infected cells may infect nearby healthy cells;
2. infected cells may recover after a certain time;
3. some infected cells may die depending on the disease;
4. protected cells reduce infection risk;
5. statistics are updated.

A simplified infection formula could be:

```text
infectionRisk = baseInfectionRate + infectedNeighbors * factor - resistance - protection
