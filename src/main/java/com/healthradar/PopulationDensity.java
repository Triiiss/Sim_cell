package com.healthradar;

/**
 * Population density multiplier used by infection probability formulas.
 */
public enum PopulationDensity {
    VERY_LOW(0.2),
    LOW(0.5),
    MEDIUM(1.0),
    HIGH(1.2);

    private final double multiplier;

    PopulationDensity(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public static PopulationDensity defaultDensity(ZoneType zoneType){
        if (zoneType == ZoneType.RESIDENTIAL){
            return PopulationDensity.LOW;
        }
        if (zoneType == ZoneType.METRO){
            return PopulationDensity.HIGH;
        }
        if (zoneType == ZoneType.PARK){
            return PopulationDensity.LOW;
        }
        if (zoneType == ZoneType.WORKPLACE){
            return PopulationDensity.MEDIUM;
        }
        if (zoneType == ZoneType.SCHOOL){
            return PopulationDensity.MEDIUM;
        }
        if (zoneType == ZoneType.HOSPITAL){
            return PopulationDensity.HIGH;
        }

        return PopulationDensity.MEDIUM;
    }
}
