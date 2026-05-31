package com.healthradar;

/**
 * Stores disease parameters used by both simulation engines.
 */
public class Disease {
    private final String name;
    private final DiseaseType type;
    private final int infectionRate;
    private final int recoveryRate;
    private final int mortalityRate;
    private final int infectionRadius;
    private final int infectionDays;
    private final int incubationDays;

    /**
     * Creates a complete disease profile.
     *
     * @param type disease identifier
     * @param name readable name shown in the CLI
     * @param infectionRate base infection probability
     * @param infectionDays number of days before automatic recovery
     * @param infectionRadius radius used by the V1 cell-based engine
     * @param incubationDays days before an incubating person becomes sick
     * @param recoveryRate daily recovery probability for sick people
     * @param mortalityRate daily mortality probability for sick people
     */
    public Disease(String name, DiseaseType type, int infectionRate, int recoveryRate, int mortalityRate, int infectionRadius, int infectionDays, int incubationDays) {
        if(name == null){
            throw new IllegalArgumentException("name cannot be null");
        }

        if (infectionRate <= 0 || infectionRate > 100 || recoveryRate <= 0 || recoveryRate > 100 || mortalityRate <= 0 || mortalityRate > 100){
            throw new IllegalArgumentException("rates need to be between 0 (excluded) and 100");
        }

        if (infectionRadius < 0){
            throw new IllegalArgumentException("The infection radius needs to be positive");
        }

        if (incubationDays < 0 || infectionDays < 0){
            throw new IllegalArgumentException("The days need to be positive");
        }
        if (incubationDays >= infectionDays){
            throw new IllegalArgumentException("The incubation time need to be smaller than the total days of the infection");
        }

        this.type = type;
        this.name = name;
        this.infectionRate = infectionRate;
        this.infectionDays = infectionDays;
        this.infectionRadius = infectionRadius;
        this.incubationDays = incubationDays;
        this.recoveryRate = recoveryRate;
        this.mortalityRate = mortalityRate;
    }

    /**
     * Consctructor without a type
     */
    public Disease(String name, int infectionRate, int recoveryRate, int mortalityRate, int infectionRadius, int infectionDays, int incubationDays){
        this(name,DiseaseType.UNKNOWN,infectionRate, recoveryRate,mortalityRate,infectionRadius, infectionDays,incubationDays);
    }

    /**
     * Builds the default profile for a disease type.
     *
     * @param type selected disease type
     * @return configured disease profile
     */
    public static Disease getDiseaseFromType(DiseaseType type) {
        if (type == DiseaseType.COVID_LIKE) {
            return new Disease("Covid-like virus",DiseaseType.COVID_LIKE, 14, 50, 20, 20, 22, 3);
        }

        return new Disease("Flu", DiseaseType.FLU, 18, 30, 10, 10, 35, 1);
    }

    public DiseaseType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getInfectionRate() {
        return infectionRate;
    }

    public int getRecoveryRate() {
        return recoveryRate;
    }

    public int getMortalityRate() {
        return mortalityRate;
    }

    public int getInfectionRadius() {
        return infectionRadius;
    }

    public int getInfectionDays() {
        return infectionDays;
    }

    public int getIncubationDays() {
        return incubationDays;
    }
}
