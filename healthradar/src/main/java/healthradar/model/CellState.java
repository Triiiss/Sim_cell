package healthradar.model;

/**
 * Enumeration of all possible states a cell (person) can be in.
 *
 * <p>The simulation follows a SEIRD epidemiological model:</p>
 * <ul>
 *   <li>SUSCEPTIBLE  – healthy, can be infected</li>
 *   <li>EXPOSED      – incubating, not yet contagious</li>
 *   <li>INFECTED     – contagious, showing symptoms</li>
 *   <li>RECOVERED    – immune for a limited time</li>
 *   <li>DEAD         – no longer active</li>
 *   <li>EMPTY        – no person occupies this cell</li>
 * </ul>
 *
 * @author HealthRadar Team
 * @version 1.0
 */
public enum CellState {
    /** No person in this cell. */
    EMPTY,
    /** Healthy person who can be infected. */
    SUSCEPTIBLE,
    /** Person who has been exposed but is not yet contagious (incubation). */
    EXPOSED,
    /** Contagious person, actively spreading the disease. */
    INFECTED,
    /** Person who recovered and has temporary immunity. */
    RECOVERED,
    /** Person who died from the disease. */
    DEAD
}
