package EvoEvo.york.machineMetaModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Various utility functions packaged as static methods. */
public class Util {

    /** Add default implementations of basic machines to a given Individual */
    public static void AddTTMachines(Individual container, Domain transcriberDomain, Domain translatorDomain, Domain reproducerDomain, Domain klonerDomain, List<Pearl> klonerSequence) {
        Structure transcriberTemplate = new Structure(container, new ArrayList<>(), transcriberDomain);
        container.addMachineTemplate(transcriberTemplate);
        container.addMachine(transcriberDomain.constructMachine(container, new ArrayList<>()));

        Structure translatorTemplate = new Structure(container, new ArrayList<>(), translatorDomain);
        container.addMachineTemplate(translatorTemplate);
        container.addMachine(translatorDomain.constructMachine(container, new ArrayList<>()));

        Structure reproducerTemplate = new Structure(container, new ArrayList<>(), reproducerDomain);
        container.addMachineTemplate(reproducerTemplate);

        Structure klonerTemplate = new Structure(container, klonerSequence, klonerDomain);
        container.addMachineTemplate(klonerTemplate);
    }

    public static void LoadProperties(String propertiesFileName) {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(propertiesFileName));
            Simulation.SetProperties(properties);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }
    }
}
