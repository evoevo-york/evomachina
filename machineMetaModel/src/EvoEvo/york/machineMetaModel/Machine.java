package EvoEvo.york.machineMetaModel;

import java.io.IOException;
import java.util.List;
import java.util.logging.*;

/** A special sort of Structure that adds in some behaviour to a structure. It's to be expected that there will be many
 *  different sort of Machines implemeted as subclasses of this class. */
public abstract class Machine extends Structure {

    /** The main logger instance and its initialisation. It's here because everything is going to need a machine which
     *  will ensure that the static initialiser will get executed in all circumstances. */
    private static Logger _logger = Logger.getLogger("EvoEvo");
    private static Handler _Handler = null;

    static {
        try {
            _Handler = new FileHandler(Simulation.GetValue("logFileName", "evoevo.log"), false);
            _Handler.setFormatter(new SimpleFormatter());
            _logger.addHandler(_Handler);
            _logger.setLevel(Simulation.GetLevel(Level.FINE));
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    /** Flush the current logger buffer, usually before terminating */
    public static void FlushLogger() {
        _Handler.flush();
    }

    /** Construct a new machine with the given code, drawn from the given domain and in the given environment.
     * @param environment the space, likely  an individual, that contains the new object
     * @param code the code for the new machine's structure
     * @param domain the domain from which the code is drawn  */
    public Machine(Space environment, List<Pearl> code, Domain domain) throws MetaModelException {
        super(environment, code, domain);
    }

    /** For simplicity's sake, let's assume that machine enactment is done by calling a single operation on all machines.
     *  @return A new structure that is the result, in some way, of the operation, or this if no new structure is produced.  */
    public abstract Structure doIt();

    /** Method to force the Machine class to load */
    public static void Initialise() {
        Logger _log = Logger.getLogger("EvoEvo");
        _log.info("Initialised");

    }
}
