package healthradar.model;

import java.io.Serializable;

/**
 * Represents a disease with all its epidemiological parameters.
 *
 * <p>Two transmission modes are supported:</p>
 * <ul>
 *   <li><b>CONTACT</b>  – spread only via direct (orthogonal or diagonal) neighbours</li>
 *   <li><b>AIRBORNE</b> – spread via a configurable radius around the infected cell</li>
 * </ul>
 *
 * <p>All probability values must be in [0.0, 1.0].</p>
 *
 * @author HealthRadar Team
 * @version 1.0
 */
public class Disease implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Human-readable name of the disease (e.g. "Influenza"). */
    private String name;

    /**
     * Transmission mode: true = airborne, false = contact only.
     */
    private boolean airborne;

    /**
     * Probability per step that a susceptible neighbour becomes exposed when
     * it is within range of an infected cell.
     */
    private double transmissionRate;

    /**
     * Number of simulation steps before an exposed cell becomes infected.
     */
    private int incubationPeriod;

    /**
     * Number of simulation steps an infected cell stays infectious before
     * either recovering or dying.
     */
    private int infectionDuration;

    /**
     * Probability per step that an infected cell dies rather than recovers
     * when its {@code infectionDuration} expires.
     */
    private double mortalityRate;

    /**
     * Number of simulation steps a recovered cell keeps its immunity before
     * becoming susceptible again.
     */
    private int immunityDuration;

    /**
     * For airborne diseases, the maximum Euclidean radius (in cells) within
     * which an infected cell can transmit to susceptible cells.
     * Ignored for contact-mode diseases.
     */
    private int transmissionRadius;

    /**
     * If true, cells in the EXPOSED (incubating) state can also transmit the
     * disease, but at a reduced rate defined by {@link #exposedTransmissionFactor}.
     * This models pre-symptomatic contagion (e.g. COVID-19).
     */
    private boolean contagiousInExposed;

    /**
     * When {@link #contagiousInExposed} is true, the effective transmission rate
     * of an EXPOSED cell equals:
     * <pre>transmissionRate * exposedTransmissionFactor</pre>
     * A value of 1.0 means the same rate as INFECTED; 0.5 means half as contagious.
     * Must be in [0.0, 1.0].
     */
    private double exposedTransmissionFactor;

    /**
     * Constructs a Disease with all parameters specified.
     *
     * @param name                      display name
     * @param airborne                  true if airborne, false if contact-only
     * @param transmissionRate          probability of infection per step per neighbour
     * @param incubationPeriod          steps spent in EXPOSED state
     * @param infectionDuration         steps spent in INFECTED state
     * @param mortalityRate             probability of dying instead of recovering
     * @param immunityDuration          steps of immunity after recovery
     * @param transmissionRadius        radius used in airborne mode
     */
    public Disease(String name, boolean airborne, double transmissionRate,
                   int incubationPeriod, int infectionDuration,
                   double mortalityRate, int immunityDuration,
                   int transmissionRadius) {
        this(name, airborne, transmissionRate, incubationPeriod, infectionDuration,
             mortalityRate, immunityDuration, transmissionRadius, false, 0.5);
    }

    /**
     * Constructs a Disease with full control over pre-symptomatic contagion.
     *
     * @param name                      display name
     * @param airborne                  true if airborne, false if contact-only
     * @param transmissionRate          probability of infection per step per neighbour
     * @param incubationPeriod          steps spent in EXPOSED state
     * @param infectionDuration         steps spent in INFECTED state
     * @param mortalityRate             probability of dying instead of recovering
     * @param immunityDuration          steps of immunity after recovery
     * @param transmissionRadius        radius used in airborne mode
     * @param contagiousInExposed       true if EXPOSED cells can also transmit
     * @param exposedTransmissionFactor fraction of transmissionRate used by EXPOSED cells
     */
    public Disease(String name, boolean airborne, double transmissionRate,
                   int incubationPeriod, int infectionDuration,
                   double mortalityRate, int immunityDuration,
                   int transmissionRadius,
                   boolean contagiousInExposed, double exposedTransmissionFactor) {
        this.name = name;
        this.airborne = airborne;
        this.transmissionRate = transmissionRate;
        this.incubationPeriod = incubationPeriod;
        this.infectionDuration = infectionDuration;
        this.mortalityRate = mortalityRate;
        this.immunityDuration = immunityDuration;
        this.transmissionRadius = transmissionRadius;
        this.contagiousInExposed = contagiousInExposed;
        this.exposedTransmissionFactor = Math.max(0.0, Math.min(1.0, exposedTransmissionFactor));
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    /** @return the disease name */
    public String getName() { return name; }

    /** @return true if airborne transmission mode is active */
    public boolean isAirborne() { return airborne; }

    /** @return probability of transmission per tick per neighbour */
    public double getTransmissionRate() { return transmissionRate; }

    /** @return incubation period in simulation steps */
    public int getIncubationPeriod() { return incubationPeriod; }

    /** @return infection duration in simulation steps */
    public int getInfectionDuration() { return infectionDuration; }

    /** @return probability of dying at end of infection */
    public double getMortalityRate() { return mortalityRate; }

    /** @return immunity duration in simulation steps after recovery */
    public int getImmunityDuration() { return immunityDuration; }

    /** @return transmission radius for airborne mode */
    public int getTransmissionRadius() { return transmissionRadius; }

    /** @return true if EXPOSED cells can transmit the disease */
    public boolean isContagiousInExposed() { return contagiousInExposed; }

    /**
     * Returns the effective transmission rate for an EXPOSED cell.
     * Returns 0 if pre-symptomatic contagion is disabled.
     *
     * @return transmissionRate * exposedTransmissionFactor, or 0
     */
    public double getExposedTransmissionRate() {
        return contagiousInExposed ? transmissionRate * exposedTransmissionFactor : 0.0;
    }

    /** @return the factor applied to transmissionRate for EXPOSED cells */
    public double getExposedTransmissionFactor() { return exposedTransmissionFactor; }

    // ── Setters ──────────────────────────────────────────────────────────────

    /** @param name new disease name */
    public void setName(String name) { this.name = name; }

    /** @param airborne true to enable airborne mode */
    public void setAirborne(boolean airborne) { this.airborne = airborne; }

    /** @param rate new transmission rate [0,1] */
    public void setTransmissionRate(double rate) { this.transmissionRate = rate; }

    /** @param period new incubation period */
    public void setIncubationPeriod(int period) { this.incubationPeriod = period; }

    /** @param duration new infection duration */
    public void setInfectionDuration(int duration) { this.infectionDuration = duration; }

    /** @param rate new mortality rate [0,1] */
    public void setMortalityRate(double rate) { this.mortalityRate = rate; }

    /** @param duration new immunity duration */
    public void setImmunityDuration(int duration) { this.immunityDuration = duration; }

    /** @param radius new transmission radius */
    public void setTransmissionRadius(int radius) { this.transmissionRadius = radius; }

    /** @param contagious true to enable pre-symptomatic transmission */
    public void setContagiousInExposed(boolean contagious) { this.contagiousInExposed = contagious; }

    /**
     * @param factor fraction of transmissionRate applied to EXPOSED cells, clamped to [0,1]
     */
    public void setExposedTransmissionFactor(double factor) {
        this.exposedTransmissionFactor = Math.max(0.0, Math.min(1.0, factor));
    }

    /**
     * Returns a preset disease configuration for Influenza (contact mode).
     *
     * @return a Disease instance representing Influenza
     */
    public static Disease influenza() {
        return new Disease("Influenza", false, 0.30, 3, 7, 0.01, 30, 1);
    }

    /**
     * Returns a preset disease configuration for a COVID-like airborne virus.
     *
     * @return a Disease instance representing COVID-like virus
     */
    public static Disease covidLike() {
        // COVID-like: airborne, AND contagious during incubation at 40% of full rate
        return new Disease("COVID-Like", true, 0.20, 5, 14, 0.02, 60, 3, true, 0.4);
    }

    @Override
    public String toString() {
        String base = name + (airborne ? " [Airborne r=" + transmissionRadius + "]" : " [Contact]");
        if (contagiousInExposed)
            base += " [Pre-symptomatic x" + String.format("%.1f", exposedTransmissionFactor) + "]";
        return base;
    }
}
