package com.healthradar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Complete simulation world: one city grid plus important places.
 */
public class City {
    private final Grid cityGrid;
    private final List<Place> places;
    private final DiseaseProfile disease;
    private final Random random;
    private int currentDay;

    public City(Grid cityGrid, DiseaseProfile disease) {
        this.cityGrid = cityGrid;
        this.disease = disease;
        this.places = new ArrayList<>();
        this.random = new Random(42);
        this.currentDay = 0;
    }

    public static City createDefault(DiseaseProfile disease) {
        City city = new City(new Grid(9, 6), disease);

        Place mall = new Place("Mall", PlaceType.MALL, new Grid(5, 3));
        Place station = new Place("Train station", PlaceType.TRAIN_STATION, new Grid(4, 3));
        Place school = new Place("School", PlaceType.SCHOOL, new Grid(4, 2));
        Place hospital = new Place("Hospital", PlaceType.HOSPITAL, new Grid(3, 2));
        Place park = new Place("Central park", PlaceType.PARK, null);

        city.addPlace(2, 2, mall);
        city.addPlace(6, 1, station);
        city.addPlace(1, 4, school);
        city.addPlace(7, 4, hospital);
        city.addPlace(4, 3, park);

        city.seedCityPeople();
        city.seedIndoorPeople(mall);
        city.seedIndoorPeople(station);
        city.seedIndoorPeople(school);
        city.seedIndoorPeople(hospital);

        return city;
    }

    public Grid getCityGrid() {
        return cityGrid;
    }

    public List<Place> getPlaces() {
        return Collections.unmodifiableList(places);
    }

    public int getCurrentDay() {
        return currentDay;
    }

    public DiseaseProfile getDisease() {
        return disease;
    }

    public Place getPlace(int index) {
        if (index < 0 || index >= places.size()) {
            return null;
        }

        return places.get(index);
    }

    public int getTotalPopulationCount() {
        int count = cityGrid.getPopulationCount();

        for (Place place : places) {
            if (place.hasIndoorGrid()) {
                count += place.getIndoorGrid().getPopulationCount();
            }
        }

        return count;
    }

    public int getTotalInfectedPopulationCount() {
        int count = cityGrid.getInfectedPopulationCount();

        for (Place place : places) {
            if (place.hasIndoorGrid()) {
                count += place.getIndoorGrid().getInfectedPopulationCount();
            }
        }

        return count;
    }

    public void advanceOneDay() {
        currentDay++;
        simulateGrid(cityGrid);

        for (Place place : places) {
            if (place.hasIndoorGrid()) {
                simulateGrid(place.getIndoorGrid());
            }
        }
    }

    private void addPlace(int x, int y, Place place) {
        cityGrid.getCell(x, y).setPlace(place);
        places.add(place);
    }

    private void seedCityPeople() {
        addPerson(cityGrid, 0, 0, new Person("City-P0"), true);
        addPerson(cityGrid, 1, 0, new Person("City-P1"), false);
        addPerson(cityGrid, 1, 1, new Person("City-P2"), false);
        addPerson(cityGrid, 3, 2, new Person("City-P3"), false);
        addPerson(cityGrid, 4, 3, new Person("City-P4"), false);
        addPerson(cityGrid, 5, 3, new Person("City-P5"), false);
        addPerson(cityGrid, 8, 5, new Person("City-P6"), false);
    }

    private void seedIndoorPeople(Place place) {
        if (!place.hasIndoorGrid()) {
            return;
        }

        Grid indoorGrid = place.getIndoorGrid();
        addPerson(indoorGrid, 0, 0, new Person(place.getName() + "-P0"), true);
        addPerson(indoorGrid, 1, 0, new Person(place.getName() + "-P1"), false);
        addPerson(indoorGrid, 1, 1, new Person(place.getName() + "-P2"), false);

        if (indoorGrid.isInside(2, 1)) {
            addPerson(indoorGrid, 2, 1, new Person(place.getName() + "-P3"), false);
        }
    }

    private void addPerson(Grid grid, int x, int y, Person person, boolean infected) {
        if (infected) {
            person.infect(disease);
        }

        grid.getCell(x, y).addPerson(person);
    }

    private void simulateGrid(Grid grid) {
        movePeople(grid);
        spreadInfections(grid);
        advancePeople(grid);
    }

    private void movePeople(Grid grid) {
        List<Move> moves = new ArrayList<>();

        for (int y = 0; y < grid.getHeight(); y++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                Cell cell = grid.getCell(x, y);

                for (Person person : cell.getPeople()) {
                    if (random.nextDouble() < 0.35) {
                        int[][] directions = {
                                {0, 0},
                                {1, 0},
                                {-1, 0},
                                {0, 1},
                                {0, -1}
                        };
                        int[] direction = directions[random.nextInt(directions.length)];
                        int nextX = x + direction[0];
                        int nextY = y + direction[1];

                        if (grid.isInside(nextX, nextY)) {
                            moves.add(new Move(person, cell, grid.getCell(nextX, nextY)));
                        }
                    }
                }
            }
        }

        for (Move move : moves) {
            move.from.removePerson(move.person);
            move.to.addPerson(move.person);
        }
    }

    private void spreadInfections(Grid grid) {
        List<Person> newlyInfected = new ArrayList<>();

        for (int y = 0; y < grid.getHeight(); y++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                Cell cell = grid.getCell(x, y);
                int infectedCount = cell.getInfectedPopulationCount();

                if (infectedCount == 0) {
                    continue;
                }

                double risk = 1 - Math.pow(1 - disease.getInfectionProbability(), infectedCount);

                for (Person person : cell.getPeople()) {
                    if (person.getState() == PersonState.HEALTHY && random.nextDouble() < risk) {
                        newlyInfected.add(person);
                    }
                }
            }
        }

        for (Person person : newlyInfected) {
            person.infect(disease);
        }
    }

    private void advancePeople(Grid grid) {
        for (int y = 0; y < grid.getHeight(); y++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                Cell cell = grid.getCell(x, y);

                for (Person person : cell.getPeople()) {
                    person.advanceOneDay();
                }

                cell.refreshStateFromPeople();
            }
        }
    }

    private static class Move {
        private final Person person;
        private final Cell from;
        private final Cell to;

        private Move(Person person, Cell from, Cell to) {
            this.person = person;
            this.from = from;
            this.to = to;
        }
    }
}
