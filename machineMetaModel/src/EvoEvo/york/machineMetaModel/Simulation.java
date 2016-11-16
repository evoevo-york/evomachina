package EvoEvo.york.machineMetaModel;

import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;

/** A static class representing the entire simulation and, therefore, allowing the definition of run-wide parameters */
public class Simulation {
    private static Optional<Properties> _Properties = Optional.empty();

    public static void SetProperties(Properties props) {
        _Properties = Optional.of(props);
    }

    public static String GetValue(String key, String defaultValue) {
        EnsureProperties();
        if (!_Properties.get().containsKey(key)) {
            _Properties.get().put(key, defaultValue);
        }
        return _Properties.get().getProperty(key);
    }

    public static int GetValue(String key, int defaultValue) {
        EnsureProperties();
        if (!_Properties.get().containsKey(key)) {
            _Properties.get().put(key, String.valueOf(defaultValue));
        }
        return Integer.valueOf(_Properties.get().getProperty(key));
    }

    public static double GetValue(String key, double defaultValue) {
        EnsureProperties();
        if (!_Properties.get().containsKey(key)) {
            _Properties.get().put(key, String.valueOf(defaultValue));
        }
        return Double.valueOf(_Properties.get().getProperty(key));
    }

    public static Level GetLevel(Level defaultValue) {
        EnsureProperties();
        if (!_Properties.get().containsKey("logLevel")) {
            _Properties.get().put("logLevel", defaultValue.getName());
        }
        return Level.parse(_Properties.get().getProperty("logLevel"));
    }

    public static boolean GetValue(String key, boolean defaultValue) {
        EnsureProperties();
        if (!_Properties.get().containsKey(key)) {
            _Properties.get().put(key, String.valueOf(defaultValue));
        }
        boolean result = Boolean.valueOf(_Properties.get().getProperty(key));
        return result;
    }

    /** Ensure that we've got a properties object */
    private static void EnsureProperties() {
        if (!_Properties.isPresent()) {
            _Properties = Optional.of(new Properties());
        }
    }

    /** Answer the value with the given name. If  it does not exist, throw an exception */
    public static int GetValue(String key) {
        EnsureProperties();
        if (!_Properties.get().containsKey(key)) {
            throw new MetaModelException("Properties do not contain key " + key);
        }
        return Integer.valueOf(_Properties.get().getProperty(key));
    }

    /** Answer the value with the given name. If  it does not exist, throw an exception */
    public static String GetString(String key) {
        EnsureProperties();
        if (!_Properties.get().containsKey(key)) {
            throw new MetaModelException("Properties do not contain key " + key);
        }
        return _Properties.get().getProperty(key);
    }

    public static void SetValue(String key, int value) {
        EnsureProperties();
        _Properties.get().put(key, String.valueOf(value));
    }

    public static void Initialise() {
        _Properties = Optional.empty();
    }
}
