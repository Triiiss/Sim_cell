package com.healthradar;

/**
 * Stores the basic behavior parameters of one disease.
 */
public class DiseaseProfile {
    private final DiseaseType type;
    private final String displayName;
    private final double infectionProbability;
    private final int infectionRadius;
    private final int incubationDuration;
    private final int infectionDuration;
    private final double recoveryProbability;
    private final double mortalityProbability;

    public DiseaseProfile(
            DiseaseType type,
            String displayName,
            double infectionProbability,
            int infectionRadius,
            int incubationDuration,
            int infectionDuration,
            double recoveryProbability,
            double mortalityProbability
    ) {
        this.type = type;
        this.displayName = displayName;
        this.infectionProbability = infectionProbability;
        this.infectionRadius = infectionRadius;
        this.incubationDuration = incubationDuration;
        this.infectionDuration = infectionDuration;
        this.recoveryProbability = recoveryProbability;
        this.mortalityProbability = mortalityProbability;
    }

    public static DiseaseProfile fromType(DiseaseType type) {
        if (type == DiseaseType.COVID_LIKE) {
            return new DiseaseProfile(DiseaseType.COVID_LIKE, "Covid-like virus", 0.14, 2, 2, 7, 0.70, 0.04);
        }

        return new DiseaseProfile(DiseaseType.FLU, "Flu", 0.18, 1, 1, 4, 0.85, 0.01);
    }

    public DiseaseType getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getInfectionProbability() {
        return infectionProbability;
    }

    public int getInfectionRadius() {
        return infectionRadius;
    }

    public int getIncubationDuration() {
        return incubationDuration;
    }

    public int getInfectionDuration() {
        return infectionDuration;
    }

    public double getRecoveryProbability() {
        return recoveryProbability;
    }

    public double getMortalityProbability() {
        return mortalityProbability;
    }
}
