package com.healthradar;

/**
 * Represents one inhabitant who can carry and transmit a disease.
 */
public class Person {
    private final String name;
    private PersonState state;
    private DiseaseProfile disease;
    private int infectedDays;

    public Person(String name) {
        this.name = name;
        this.state = PersonState.HEALTHY;
        this.disease = null;
        this.infectedDays = 0;
    }

    public String getName() {
        return name;
    }

    public PersonState getState() {
        return state;
    }

    public DiseaseProfile getDisease() {
        return disease;
    }

    public int getInfectedDays() {
        return infectedDays;
    }

    public boolean isContagious() {
        return state == PersonState.INFECTED;
    }

    public void infect(DiseaseProfile disease) {
        if (state != PersonState.HEALTHY || disease == null) {
            return;
        }

        this.state = PersonState.INFECTED;
        this.disease = disease;
        this.infectedDays = 0;
    }

    public void advanceOneDay() {
        if (state != PersonState.INFECTED || disease == null) {
            return;
        }

        infectedDays++;

        if (infectedDays >= disease.getInfectionDuration()) {
            state = PersonState.RECOVERED;
            disease = null;
            infectedDays = 0;
        }
    }
}
