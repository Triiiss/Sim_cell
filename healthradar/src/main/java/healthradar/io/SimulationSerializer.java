package healthradar.io;

import healthradar.model.SimulationEngine;

import java.io.*;
import java.nio.file.Path;

/**
 * Handles binary serialisation and deserialisation of a {@link SimulationEngine}
 * (which includes the {@link healthradar.model.Grid}, the
 * {@link healthradar.model.Disease}, all cell states, and the step history).
 *
 * <p>Files are written using Java's built-in {@link ObjectOutputStream} /
 * {@link ObjectInputStream}. The resulting file is <em>not</em> human-readable
 * but faithfully preserves every aspect of the running simulation.</p>
 *
 * @author HealthRadar Team
 * @version 1.0
 */
public class SimulationSerializer {

    /** Private constructor: this is a utility class. */
    private SimulationSerializer() {}

    /**
     * Serialises the given engine to a binary file at {@code path}.
     *
     * @param engine the engine to save
     * @param path   destination file path
     * @throws IOException if the file cannot be written
     */
    public static void save(SimulationEngine engine, Path path) throws IOException {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new BufferedOutputStream(
                             new FileOutputStream(path.toFile())))) {
            oos.writeObject(engine);
        }
    }

    /**
     * Deserialises a {@link SimulationEngine} from the binary file at {@code path}.
     *
     * @param path source file path
     * @return the restored simulation engine
     * @throws IOException            if the file cannot be read
     * @throws ClassNotFoundException if the serialised class is not on the classpath
     */
    public static SimulationEngine load(Path path) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois =
                     new ObjectInputStream(new BufferedInputStream(
                             new FileInputStream(path.toFile())))) {
            return (SimulationEngine) ois.readObject();
        }
    }
}
