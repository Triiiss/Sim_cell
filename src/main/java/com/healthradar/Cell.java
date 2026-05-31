package com.healthradar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Represents one position of the simulation grid.
 *
 * The cell still keeps the simple V1 state used by the first engine, but it
 * also stores the agent-based data needed by the city simulation: zone type,
 * population density, capacity, and people currently present in the cell.
 */
public class Cell {
    private final ZoneType zoneType;
    private final PopulationDensity populationDensity;
    private final int capacity;
    private List<Person> people;

    public Cell(ZoneType zoneType, PopulationDensity populationDensity ,int capacity){
        if (capacity <= 0){
            throw new IllegalArgumentException("capacity has to be strickly positive");
        }
        this.zoneType = zoneType;
        this.populationDensity = populationDensity;
        this.capacity = capacity;
        this.people = new ArrayList<Person>();
    }

    public Cell(ZoneType zoneType, int capacity){
        this(zoneType,PopulationDensity.defaultDensity(zoneType),capacity);
    }

    public ZoneType getZoneType() {
        return zoneType;
    }

    public PopulationDensity getPopulationDensity() {
        return populationDensity;
    }

    public int getCapacity() {
        return capacity;
    }

    /**
     * Returns the people currently stored in this cell.
     *
     * @return read-only list of people
     */
    public List<Person> getPeople() {
        return Collections.unmodifiableList(people);
    }
    
    public Set<Disease> getAllDiseases() {
        Set<Disease> diseases = new HashSet<Disease>();

        for (Person person : people){
            if (person.getDisease() != null){
                diseases.add(person.getDisease());
            }
        }

        return diseases;
    }


    /**
     * Adds one person to this cell.
     *
     * @param person person entering the cell
     */
    public void addPerson(Person person) {
        if (person.getState() != PersonState.DEAD){
            people.add(person);
        }
    }

    /**
     * Removes one person from this cell.
     *
     * @param person person leaving the cell
     */
    public void removePerson(Person person) {
        if (person.getState() != PersonState.DEAD){
            people.remove(person);
        }
    }

    /**
     * Counts alive people only, because dead people do not move or spread disease.
     *
     * @return alive population in this cell
     */
    public int getPopulationCount() {
        int count = 0;

        for (Person person : people) {
            if (person.isAlive()) {
                count++;
            }
        }

        return count;
    }

    /**
     * Counts people who can currently spread any disease.
     *
     * @return contagious population in this cell
     */
    public int getInfectedPopulationCount() {
        int count = 0;

        for (Person person : people) {
            if (person.isContagious()) {
                count++;
            }
        }

        return count;
    }

    /**
     * Counts contagious people for one disease.
     *
     * @param disease disease to count
     * @return contagious population for the selected disease
     */
    public int getInfectedPopulationCount(Disease disease) {
        int count = 0;

        for (Person person : people) {
            if (person.isContagious() && disease.equals(person.getDisease())) {
                count++;
            }
        }

        return count;
    }

    /**
     * Computes the local risk for one disease.
     *
     * Risk is based on the local ratio of contagious people. If the cell is a
     * cluster, the risk is doubled and capped to 1.0.
     *
     * @param disease disease used for the risk calculation
     * @return risk between 0.0 and 1.0
     */
    public double getRisk(Disease disease) {
        int populationCount = getPopulationCount();

        if (populationCount == 0) {
            return 0;
        }

        double risk = (double) getInfectedPopulationCount(disease) / populationCount;

        if (isCluster()) {
            risk *= 2.0;
        }

        return Math.min(1.0, risk);
    }

    /**
     * Checks if this cell has reached or exceeded its capacity.
     *
     * @return true when the cell is overcrowded
     */
    public boolean isCluster() {
        return getPopulationCount() >= capacity;
    }

    /**
     * Finds the most represented contagious disease in this cell.
     *
     * @return dominant contagious disease, or null when the cell has no disease
     */
    public Disease getDominantDisease() {
        Set<Disease> diseases = getAllDiseases();

        if (diseases.size() <= 0){
            return null;
        }
        Disease dominantDisease = null;
        double dominantRisk = 0.0;

        for (Disease d : diseases) {
            double risk = (d.getInfectionRate()/100.0) * this.getRisk(d);

            if (dominantDisease == null || risk > dominantRisk){
                dominantDisease = d;
                dominantRisk = risk;
                continue;
            }
        }

        return dominantDisease;
    }
}
