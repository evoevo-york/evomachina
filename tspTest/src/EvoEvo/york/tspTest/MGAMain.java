package EvoEvo.york.tspTest;

import EvoEvo.york.machineMetaModel.Kloner;
import EvoEvo.york.machineMetaModel.Machine;
import EvoEvo.york.machineMetaModel.Pearl;
import EvoEvo.york.machineMetaModel.Simulation;

import java.util.List;
import java.util.logging.Logger;

public class MGAMain {
    private static Logger _Logger = Logger.getLogger("EvoEvo");

    /** Main method kicks off a TSP search in a bucket space using a microbial GA approach */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println(String.format("Usage: java -classpath <classpath> %s <property file path>", MGAMain.class.getName()));
            System.exit(1);
        }

        // Create the domain:
        Main.SetUp();

        // Load the Properties files into the Simulation class that is referred to by the rest of the code:
        TestUtil.LoadProperties(args[0]);

        // Create SearchableSpace for MGA approach:
        MicrobialGATSPSpace world = new MicrobialGATSPSpace();

        // Create a complete set of journeys in the new space:
        int numJourneys = Simulation.GetValue("numMGAJourneys", 100);
        Main._KDomain = new KlonerDomain("Kloner domain", Kloner.class);
        for (int i = 0; i < numJourneys; i++) {
            List<Pearl> route = TestUtil.CreateInitialRandomRoute(Main._CityDomain, Main._NUM_CITIES);
            TestUtil.MakeJourneyWithMutatingCopier(route, world, Main._CityDomain, Main._KDomain, Main._TranscriberDomain, Main._TranslatorDomain, Main._ReproducerDomain);
        }
        _Logger.info("Initial population: " + Simulation.GetValue("numMGAJourneys"));

        Journey best;
        long time = System.currentTimeMillis();
        int searchCount = 0;
        do {
            best = (Journey)world.search().get();
            if (searchCount++ % 100 == 0) {
                System.out.printf("Best of %d is: %s%n", world.numSubspaces(), best);
            }
        } while (best.journeyTime() > Simulation.GetValue("targetTime", 35500)
                 &&
                 System.currentTimeMillis() < (time + Simulation.GetValue("totalRunTimeInMilliseconds", 60000)));

        Machine.FlushLogger();
        _Logger.fine(String.format("{%d} Completed, best is %s", System.currentTimeMillis(), best));
        Machine.FlushLogger();

        System.out.println("Best: " + best);
    }

}
