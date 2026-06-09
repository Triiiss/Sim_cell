package com.healthradar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents one position of the simulation grid.
 */
public class Cell {
    private CellState state;
    private int infectedSteps;
    private final List<Person> people;
    private Place place;

    public Cell() {
        this.state = CellState.HEALTHY;
        this.infectedSteps = 0;
        this.people = new ArrayList<>();
    }

    public Cell(CellState state) {
        this.state = state;
        this.infectedSteps = 0;
        this.people = new ArrayList<>();
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

    public List<Person> getPeople() {
        return Collections.unmodifiableList(people);
    }

    public void addPerson(Person person) {
        if (person == null) {
            return;
        }

        people.add(person);
        refreshStateFromPeople();
    }

    public void removePerson(Person person) {
        people.remove(person);
        refreshStateFromPeople();
    }

    public int getPopulationCount() {
        return people.size();
    }

    public int getInfectedPopulationCount() {
        int count = 0;

        for (Person person : people) {
            if (person.isContagious()) {
                count++;
            }
        }

        return count;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public boolean hasPlace() {
        return place != null;
    }

    public void refreshStateFromPeople() {
        boolean hasRecovered = false;

        for (Person person : people) {
            if (person.isContagious()) {
                state = CellState.INFECTED;
                return;
            }

            if (person.getState() == PersonState.RECOVERED) {
                hasRecovered = true;
            }
        }

        state = hasRecovered ? CellState.RECOVERED : CellState.HEALTHY;
    }
}
