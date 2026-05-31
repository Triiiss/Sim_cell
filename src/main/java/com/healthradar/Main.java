package com.healthradar;

public class Main {
    public static void main(String[] args) {

        DiseaseType diseaseType = parseDiseaseType(args);
        Disease disease = Disease.getDiseaseFromType(diseaseType);

        System.out.println("HealthRadar V1 - project started");
        System.out.println("Selected disease: " + disease.getName());
        System.out.println("Infection rate: " + disease.getInfectionRate());
        System.out.println("Recovery rate: " + disease.getRecoveryRate());
        System.out.println("Mortality rate: " + disease.getMortalityRate());
        System.out.println("Infection radius: " + disease.getInfectionRadius());
        System.out.println("Incubation days: " + disease.getIncubationDays());
        System.out.println("Infection days: " + disease.getInfectionDays());

        City city = new City(
                20,
                20,
                300,
                10,
                true
        );

        city.addDisease(disease);

        System.out.println("\nCity created:");
        System.out.println("Population: " + city.getPopulationCount());
        System.out.println("Sick population: " + city.getSickPopulationCount());
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