package com.healthradar;

public class Main {
    public static void main(String[] args) {
        DiseaseType diseaseType = parseDiseaseType(args);
        DiseaseProfile disease = DiseaseProfile.fromType(diseaseType);

        System.out.println("HealthRadar V1 - project started");
        System.out.println("Selected disease: " + disease.getDisplayName());
        System.out.println("Infection probability: " + disease.getInfectionProbability());
        System.out.println("Infection radius: " + disease.getInfectionRadius());
        System.out.println("Incubation duration: " + disease.getIncubationDuration());
        System.out.println("Infection duration: " + disease.getInfectionDuration());
    }

    private static DiseaseType parseDiseaseType(String[] args) {
        if (args.length == 0) {
            return DiseaseType.FLU;
        }

        try {
            return DiseaseType.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException exception) {
            return DiseaseType.FLU;
        }
    }
}
