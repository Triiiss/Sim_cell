package com.healthradar;

/**
 * Represents one inhabitant moving through the city.
 */
public class Person {
    private static final double MASK_IMMUNITY_BONUS = 0.15;

    private final String name;
    private final int age;
    private final boolean wearsMask;
    private PersonState state;
    private Disease disease;
    private int infectedDays;
    private int immunity;

    /**
     * Creates a healthy person with the given biological parameters.
     *
     * @param name display name used in future UI details
     * @param age age used by fragility calculations
     * @param immunity base immunity between 0 and 100
     * @param wearsMask true when the person has mask protection
     */
    public Person(String name, int age, int immunity, boolean wearsMask){
        if (name == null){
            throw new IllegalArgumentException("name cannot be null");
        }
        if (age < 0){
            throw new IllegalArgumentException("age has to be positive");
        }
        this.name = name;
        this.age = age;
        this.immunity = clamp(immunity,wearsMask);
        this.wearsMask = wearsMask;
        
        this.state = PersonState.HEALTHY;
        this.infectedDays = -1;
        this.disease = null;
    }

    public Person(String name, int age, int immunity, boolean wearsMask, Disease disease, int infectedDays){
        this(name,age,immunity,wearsMask);
        
        if (disease != null && infectedDays >= 0 && infectedDays < disease.getInfectionDays()){
            this.disease = disease;
            this.infectedDays = infectedDays;
            this.state = getStateFromDays();
        }
    }

    /**
     * Infects the person and starts incubation.
     *
     * @param disease disease caught by the person
     */
    public void infect(Disease disease) {
        if (this.state != PersonState.HEALTHY || disease == null) {
            return;
        }

        this.disease = disease;
        this.infectedDays = 0;
        this.state = PersonState.INCUBATING;
    }

    /**
     * Advances the disease duration by one day when the person is infected.
     */
    public void incrementInfectedDays() {
        if ((state == PersonState.INCUBATING || state == PersonState.INFECTIOUS) && this.infectedDays >= 0) {
            infectedDays++;
        }
    }

    /**
     * Decreases immunity after recovery without going below the selected floor.
     *
     * @param dailyLoss amount removed each simulated day
     * @param minimumImmunity lower immunity limit after recovery
     */
    public void decreaseImmunity(int dailyLoss){
        if (dailyLoss < 0 || dailyLoss > 100){
            return;
        }
        if (this.wearsMask) {
            immunity = Math.max(15, this.immunity - dailyLoss);
        }
        else{
            immunity = Math.max(0, this.immunity - dailyLoss);
        }
    }

    /**
     * Moves an incubating person to the visibly sick state.
     */
    public void becomeSick() {
        if (state == PersonState.INCUBATING && this.infectedDays >= this.disease.getIncubationDays()) {
            state = PersonState.INFECTIOUS;
        }
    }

    /**
     * Recovers the person and gives temporary full immunity.
     */
    public void recover() {
        if (state == PersonState.INCUBATING || state == PersonState.INFECTIOUS){
            state = PersonState.RECOVERED;
            disease = null;
            infectedDays = -1;
            immunity = 100;
        }
    }

    /**
     * Marks the person as dead and removes any active disease reference.
     */
    public void die() {
        if (this.state == PersonState.INFECTIOUS){        // You can only die if you are infectious (not incubated nor healthy)
            state = PersonState.DEAD;
            infectedDays = -1;
            immunity = 0;
        }
    }

    public boolean isAlive() {
        return state != PersonState.DEAD;
    }

    public boolean isContagious() {
        return state == PersonState.INCUBATING || state == PersonState.INFECTIOUS;
    }

    /**
     * Computes the person's fragility from immunity and age.
     *
     * @return fragility between 0.0 and 1.0
     */
    public double getFragility() {
        int immuneFragility = 100 - immunity;
        int ageFragility = getAgeFragility();
        return clamp((int)(0.6 * immuneFragility + (0.4 * ageFragility)));
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public boolean wearsMask() {
        return wearsMask;
    }

    public PersonState getState() {
        return state;
    }

    public Disease getDisease() {
        return disease;
    }

    public int getInfectedDays() {
        return infectedDays;
    }

    public int getImmunity() {
        return immunity;
    }

    private int getAgeFragility() {
        if (age < 10) {
            return 50;
        }
        if (age < 20) {
            return 20;
        }
        if (age < 40) {
            return 40;
        }
        if (age < 60) {
            return 60;
        }
        if (age < 75) {
            return 80;
        }
        return 100;
    }

    private PersonState getStateFromDays(){
        if (this.infectedDays<-1 || this.disease == null){
            this.infectedDays = -1;
            this.disease = null;
        }
        if (this.infectedDays == -1){
            return PersonState.HEALTHY;
        }
        if (this.infectedDays < this.disease.getIncubationDays()){
            return PersonState.INCUBATING;
        }
        if (infectedDays >= disease.getInfectionDays()){
            return PersonState.RECOVERED;
        }
        else{
            return PersonState.INFECTIOUS;
        }
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private static int clamp(int value, boolean wearsMask) {
        if (wearsMask){
            return Math.max(15, Math.min(100, value));
        }
        return clamp(value);
    }
}