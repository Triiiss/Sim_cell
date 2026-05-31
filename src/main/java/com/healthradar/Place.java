package com.healthradar;

/**
 * Important city location, optionally with its own indoor grid.
 */
public class Place {
    private final String name;
    private final PlaceType type;
    private final Grid indoorGrid;

    public Place(String name, PlaceType type, Grid indoorGrid) {
        this.name = name;
        this.type = type;
        this.indoorGrid = indoorGrid;
    }

    public String getName() {
        return name;
    }

    public PlaceType getType() {
        return type;
    }

    public Grid getIndoorGrid() {
        return indoorGrid;
    }

    public boolean hasIndoorGrid() {
        return indoorGrid != null;
    }
}
