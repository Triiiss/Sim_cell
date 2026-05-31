package com.healthradar;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        DiseaseType diseaseType = parseDiseaseType(args);
        DiseaseProfile disease = DiseaseProfile.fromType(diseaseType);
        City city = City.createDefault(disease);

        if (hasDemoFlag(args)) {
            runDemo(city, 3);
            return;
        }

        runMenu(city);
    }

    private static void runMenu(City city) {
        Scanner scanner = new Scanner(System.in);
        int selectedPlaceIndex = -1;

        System.out.println("HealthRadar multi-scale local prototype");
        printGlobalStats(city);
        printSelectedView(city, selectedPlaceIndex);

        while (true) {
            System.out.println();
            System.out.println("Selected view: " + selectedViewName(city, selectedPlaceIndex));
            System.out.println("1. Advance one day");
            System.out.println("2. Select city map");
            System.out.println("3. Show global statistics");
            System.out.println("4. List places");
            System.out.println("5. Select place indoor map");
            System.out.println("6. Show selected view");
            System.out.println("7. Quit");
            System.out.print("> ");

            if (!scanner.hasNextLine()) {
                return;
            }

            String choice = scanner.nextLine().trim();

            if ("1".equals(choice)) {
                city.advanceOneDay();
                System.out.println("Day advanced.");
                printGlobalStats(city);
                printSelectedView(city, selectedPlaceIndex);
            } else if ("2".equals(choice)) {
                selectedPlaceIndex = -1;
                printSelectedView(city, selectedPlaceIndex);
            } else if ("3".equals(choice)) {
                printGlobalStats(city);
            } else if ("4".equals(choice)) {
                printPlaces(city);
            } else if ("5".equals(choice)) {
                selectedPlaceIndex = selectPlaceView(scanner, city, selectedPlaceIndex);
            } else if ("6".equals(choice)) {
                printSelectedView(city, selectedPlaceIndex);
            } else if ("7".equals(choice)) {
                return;
            } else {
                System.out.println("Unknown option.");
            }
        }
    }

    private static void runDemo(City city, int days) {
        System.out.println("HealthRadar multi-scale local prototype");
        printGrid("City map", city.getCityGrid(), true);

        for (int i = 0; i < days; i++) {
            city.advanceOneDay();
            printGlobalStats(city);
        }
    }

    private static void printGlobalStats(City city) {
        System.out.println("Day: " + city.getCurrentDay());
        System.out.println("Disease: " + city.getDisease().getDisplayName());
        System.out.println("Total population: " + city.getTotalPopulationCount());
        System.out.println("Infected population: " + city.getTotalInfectedPopulationCount());
    }

    private static void printPlaces(City city) {
        List<Place> places = city.getPlaces();

        for (int i = 0; i < places.size(); i++) {
            Place place = places.get(i);
            String gridInfo = place.hasIndoorGrid()
                    ? place.getIndoorGrid().getWidth() + "x" + place.getIndoorGrid().getHeight()
                    : "no indoor grid";

            System.out.println((i + 1) + ". " + place.getName() + " (" + place.getType() + ", " + gridInfo + ")");
        }
    }

    private static int selectPlaceView(Scanner scanner, City city, int currentPlaceIndex) {
        printPlaces(city);
        System.out.print("Place number: ");

        if (!scanner.hasNextLine()) {
            return currentPlaceIndex;
        }

        try {
            int index = Integer.parseInt(scanner.nextLine().trim()) - 1;
            Place place = city.getPlace(index);

            if (place == null) {
                System.out.println("Unknown place.");
                return currentPlaceIndex;
            }

            if (!place.hasIndoorGrid()) {
                System.out.println(place.getName() + " does not have an indoor grid yet.");
                return currentPlaceIndex;
            }

            printSelectedView(city, index);
            return index;
        } catch (NumberFormatException exception) {
            System.out.println("Invalid number.");
            return currentPlaceIndex;
        }
    }

    private static void printSelectedView(City city, int selectedPlaceIndex) {
        if (selectedPlaceIndex < 0) {
            printGrid("City map", city.getCityGrid(), true);
            return;
        }

        Place place = city.getPlace(selectedPlaceIndex);

        if (place == null || !place.hasIndoorGrid()) {
            printGrid("City map", city.getCityGrid(), true);
            return;
        }

        printGrid(place.getName() + " indoor map", place.getIndoorGrid(), false);
    }

    private static String selectedViewName(City city, int selectedPlaceIndex) {
        if (selectedPlaceIndex < 0) {
            return "City";
        }

        Place place = city.getPlace(selectedPlaceIndex);

        if (place == null) {
            return "City";
        }

        return place.getName();
    }

    private static void printGrid(String title, Grid grid, boolean showPlaces) {
        System.out.println();
        System.out.println(title);

        for (int y = 0; y < grid.getHeight(); y++) {
            StringBuilder line = new StringBuilder();

            for (int x = 0; x < grid.getWidth(); x++) {
                line.append(symbolFor(grid.getCell(x, y), showPlaces));
            }

            System.out.println(line);
        }
    }

    private static char symbolFor(Cell cell, boolean showPlaces) {
        if (showPlaces && cell.hasPlace()) {
            return placeSymbol(cell.getPlace().getType());
        }

        if (cell.getInfectedPopulationCount() > 0) {
            return '!';
        }

        if (cell.getPopulationCount() > 0) {
            return 'o';
        }

        return '.';
    }

    private static char placeSymbol(PlaceType type) {
        if (type == PlaceType.MALL) {
            return 'M';
        }
        if (type == PlaceType.TRAIN_STATION) {
            return 'T';
        }
        if (type == PlaceType.SCHOOL) {
            return 'S';
        }
        if (type == PlaceType.HOSPITAL) {
            return 'H';
        }
        if (type == PlaceType.WORKPLACE) {
            return 'W';
        }
        if (type == PlaceType.PARK) {
            return 'P';
        }

        return '?';
    }

    private static DiseaseType parseDiseaseType(String[] args) {
        for (String arg : args) {
            try {
                return DiseaseType.valueOf(arg.toUpperCase());
            } catch (IllegalArgumentException exception) {
                // Ignore non-disease arguments such as --demo.
            }
        }

        return DiseaseType.FLU;
    }

    private static boolean hasDemoFlag(String[] args) {
        for (String arg : args) {
            if ("--demo".equalsIgnoreCase(arg)) {
                return true;
            }
        }

        return false;
    }
}
