package EvoEvo.york.machinaClust;

import EvoEvo.york.machineMetaModel.Domain;
import EvoEvo.york.machineMetaModel.ElitistSpace;
import EvoEvo.york.machineMetaModel.Individual;
import EvoEvo.york.machineMetaModel.Kloner;
import EvoEvo.york.machineMetaModel.Machine;
import EvoEvo.york.machineMetaModel.Reproducer;
import EvoEvo.york.machineMetaModel.SearchableSpace;
import EvoEvo.york.machineMetaModel.Simulation;
import EvoEvo.york.machineMetaModel.Space;
import EvoEvo.york.machineMetaModel.Transcriber;
import EvoEvo.york.machineMetaModel.Translator;
import EvoEvo.york.machineMetaModel.Util;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

public class Main {
    static Logger _Logger = Logger.getLogger("EvoEvo");

    static Domain _ReproducerDomain;
    static Domain _TranscriberDomain;
    static Domain _TranslatorDomain;
    static ClusteringType _ClustererDomain;
    static KlonerType _KlonerDomain;
    static ClusterableDataset _TestData;

    static void SetUp(String dataFileName) throws IOException {
        _TestData = new ClusterableDataset(Simulation.GetValue("numDimensions"));
        _TestData.loadFrom(dataFileName);

        _ClustererDomain = new ClusteringType("Clusterer domain", _TestData, Simulation.GetValue("numCorePoints", 8), _TestData.minValue(), _TestData.valueRange());

        _ReproducerDomain = new Domain("Reproducer domain", Reproducer.class);
        _TranscriberDomain = new Domain("Transcriber domain", Transcriber.class);
        _TranslatorDomain = new Domain("Translater domain", Translator.class);
        _KlonerDomain = new KlonerType("MachinaClust Kloner domain", Kloner.class);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println(String.format("Usage: java -classpath <classpath> %s <property file path>", Main.class.getName()));
            System.exit(1);
        }

        // Load the Properties files into the Simulation class that is referred to by the rest of the code:
        Util.LoadProperties(args[0]);
        Machine.Initialise();

        // Create the domain:
        String fileName = Simulation.GetString("dataFile");
        try {
            SetUp(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        SearchableSpace space = new ElitistSpace(Optional.of(_TestData));

        int numClusterings = Simulation.GetValue("numIndividuals", 100);

        for (int i = 0; i < numClusterings; i++) {
            ClusteringTestUtil.CreateRandomClustering(Optional.of((Space)space), _ClustererDomain,
                                                      _ReproducerDomain, _TranscriberDomain, _TranslatorDomain, _KlonerDomain,
                                                      Simulation.GetValue("initialDeletionMutationRate", 0.1),
                                                      Simulation.GetValue("initialDuplicationMutationRate", 0.1),
                                                      Simulation.GetValue("initialTranslocationMutationRate", 0.1),
                                                      Simulation.GetValue("initialPointMutationRate", 0.1),
                                                      Simulation.GetValue("minInitialNumPearls", 100), Simulation.GetValue("maxInitialNumPearls", 200),
                                                      _TestData.getNumDimensions(), _TestData.minValue(), _TestData.valueRange());
        }

        Optional<Individual> initialBest = space.best();

        double initialFitness = ((Clustering)initialBest.get()).fitness();

        Clustering best;
        long time = System.currentTimeMillis();
        int searchCount = 0;
        do {
            best = (Clustering)space.search().get();
            if (searchCount % 100 == 0) {
                String report = String.format("Best of %d after %d iterations is: %s%n ", space.numIndividuals(), searchCount, best);
                System.out.printf(report);
                _Logger.fine(report);
            }
            searchCount++;
        } while (best.fitness() < Simulation.GetValue("targetFitness", -100.0)
                 &&
                 best.getGeneration() < Simulation.GetValue("maxGeneration", 1000)
                 &&
                 searchCount < Simulation.GetValue("maxIterations", 1000000));

        _Logger.info(String.format("{%d} Completed, best is %s", System.currentTimeMillis(), best));
        Machine.FlushLogger();

        System.out.println("Best: " + space.best().get());
    }
}
