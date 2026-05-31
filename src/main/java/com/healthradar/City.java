package com.healthradar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Holds the full city state used by the agent-based simulation.
 */
public class City {
    private final int width;
    private final int height;
    private Cell[][] grid;
    private boolean maskPolicyEnabled;
    private List<Disease> diseases;

    public City(int width, int height,int populationInitial,int infectedPopulationInitial, boolean maskPolicyEnabled) {
        if (width <= 0 || height <= 0){
            throw new IllegalArgumentException("width and height have to be strickly positive");
        }
        if (populationInitial <= 0 || infectedPopulationInitial < 0 || populationInitial<infectedPopulationInitial){
            throw new IllegalArgumentException("initial population has to be positive, and infected cannot be greater than the total amount of people");
        }
        this.width = width;
        this.height = height;
        initializeGrid(width, height,populationInitial,infectedPopulationInitial,maskPolicyEnabled);
        this.maskPolicyEnabled = maskPolicyEnabled;
        this.diseases = new ArrayList<>();
    }

    private void initializeGrid(int width, int height, int populationInitial, int infectedPopulationInitial, boolean maskPolicyEnabled){
        Random rand = new Random();
        
        this.grid = new Cell[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = rand.nextInt(100);
                ZoneType type = ZoneType.RESIDENTIAL;
                if (r<15){
                    type = ZoneType.METRO;
                }
                else if (r<39){
                    type = ZoneType.PARK;
                }
                else if (r<59){
                    type = ZoneType.WORKPLACE;
                }
                else if (r<64){
                    type = ZoneType.SCHOOL;
                }
                else if (r<65){
                    type = ZoneType.HOSPITAL;
                }
                grid[y][x] = new Cell(type,(int)(200*PopulationDensity.defaultDensity(type).getMultiplier()));
            }
        }
        List<Person> allPeople = new ArrayList<>();

        for (int i = 0; i < populationInitial; i++) {
            int age = rand.nextInt(90) + 1;
            int immunity = rand.nextInt(101);
            boolean wearsMask = maskPolicyEnabled && rand.nextDouble() < 0.5;

            Person p = new Person(
                    "P" + i,
                    age,
                    immunity,
                    wearsMask
            );

            allPeople.add(p);
        }

        // 3. Infection initiale
        for (int i = 0; i < infectedPopulationInitial && i < allPeople.size(); i++) {
            Disease randomDisease = null;
            if (diseases !=null) {
                randomDisease = diseases.get(rand.nextInt(diseases.size()));
            }

            if (randomDisease != null) {
                allPeople.get(i).infect(randomDisease);
            }
        }

        // 4. Placement aléatoire dans la grille
        for (Person p : allPeople) {

            int x = rand.nextInt(width);
            int y = rand.nextInt(height);

            Cell cell = grid[y][x];

            cell.addPerson(p);
        }
    }

    public int getWidth(){
        return this.width;
    }

    public int getHeight(){
        return this.height;
    }

    public Cell getCell(int x, int y){
        if (this.isInside(x,y)){
            return grid[y][x];
        }
        return null;
    }

    public Cell[][] getGrid(){
        return grid;
    }

    public boolean isMaskPolicyEnabled() {
        return maskPolicyEnabled;
    }

    public List<Disease> getDiseases() {
        return Collections.unmodifiableList(diseases);
    }

    public void addDisease(Disease disease) {
        diseases.add(disease);
    }


    public int getPopulationCount() {
        int count = 0;

        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                count += grid[y][x].getPopulationCount();
            }
        }

        return count;
    }

    public int getSickPopulationCount() {
        int count = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                count += grid[y][x].getInfectedPopulationCount();
            }
        }

        return count;
    }

    public boolean isInside(int x, int y) {
        return x >= 0 && x < this.width && y >= 0 && y < this.height;
    }
}
