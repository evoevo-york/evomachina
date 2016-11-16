package EvoEvo.york.machinaClust;

import EvoEvo.york.machineMetaModel.Domain;
import EvoEvo.york.machineMetaModel.ElitistSpace;
import EvoEvo.york.machineMetaModel.Individual;
import EvoEvo.york.machineMetaModel.Kloner;
import EvoEvo.york.machineMetaModel.Machine;
import EvoEvo.york.machineMetaModel.MicrobialGASpace;
import EvoEvo.york.machineMetaModel.Reproducer;
import EvoEvo.york.machineMetaModel.SearchableSpace;
import EvoEvo.york.machineMetaModel.Simulation;
import EvoEvo.york.machineMetaModel.Space;
import EvoEvo.york.machineMetaModel.Transcriber;
import EvoEvo.york.machineMetaModel.Translator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Optional;
import java.util.logging.Logger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

/** A set of clustering tests using real data as supplied by Sergio */
public class ClusteringTestWithFakeData {
    private Logger _logger = Logger.getLogger("EvoEvo");

    private Domain _reproducerDomain;
    private Domain _transcriberDomain;
    private Domain _translatorDomain;
    private ClusteringType _clustererDomain;
    private KlonerType _klonerDomain;
    private ClusterableDataset _testData;

    @BeforeMethod
    public void setUp() throws Exception {
        _testData = new ClusterableDataset(5);
        this.populateTestData(_testData);

        _clustererDomain = new ClusteringType("Clusterer domain", _testData, 15, _testData.minValue(), _testData.valueRange());

        _reproducerDomain = new Domain("Reproducer domain", Reproducer.class);
        _transcriberDomain = new Domain("Transcriber domain", Transcriber.class);
        _translatorDomain = new Domain("Translater domain", Translator.class);
        _klonerDomain = new KlonerType("MachinaClust Kloner domain", Kloner.class);

        Simulation.Initialise();
    }

    private void populateTestData(ClusterableDataset td) {
        for (double d = -1; d < 1; d = d + 0.4) { // -1, -0.6, -0.2, 0.2, 0.6
            for (int i = 0; i < 200; i++) {
                Observation o = new Observation(new Double[]{d, d, d, d, d}, td);
                td.add(o);
            }
        }
    }

    @Test
    public void checkDatasetConfiguration() throws Exception {
        assertEquals(_testData.getNumDimensions(), 5, "Number of dimensions in dataset");
        assertEquals(_testData.getNumObservations(), 1000, "Number of observations in dataset");
    }

    @Test
    public void calculateFitnessProvidesANumberAndACollectionOfPopulatedCorepoints() throws Exception {
        Space space = new MicrobialGASpace(Optional.of(_testData));

        // Create random clustering
        Clustering c1 = ClusteringTestUtil.CreateRandomClustering(Optional.of(space), _clustererDomain,
                                                                  _reproducerDomain, _transcriberDomain, _translatorDomain, _klonerDomain,
                                                                  0.01, 0.01, 0.01, 0.01, 10, 20, _testData.getNumDimensions(), 2, -1);

        double fitness = c1.fitness();
        assertNotEquals(fitness, -123.45, "Default value returned");
        assertTrue(fitness < 0, "Fitness less than zero");

        ClusterCalculator cc = (ClusterCalculator)c1.locateMachine(ClusterCalculator.class);
        assertTrue(cc.numCorePoints() <= 10 && cc.numCorePoints() > 0, "Number of core points");

        int attachedObservations = 0;
        for (CorePoint cp : cc._corePoints.values()) attachedObservations += cp.getNumObservations();

        assertEquals(attachedObservations, 1000, "number of observations attached to core points");
    }

    @DataProvider(name = "numCorePoints")
    public static Object[][] NumCorePoints() {
        return new Object[][] {{5, 0.08}, {5, 0.05}, {5, 0.03}, {5, 0.02}, {5, 0.01}};
    }

    @Test (enabled = true, dataProvider = "numCorePoints")
    public void createSmallSetOfClusteringsAndCheckSomeConvergenceOverGenerations(int numCorePoints, double mutationRate) throws Exception {
        SearchableSpace space = new ElitistSpace(Optional.of(_testData));

        int numClusterings = 100;
        // Create clusterings inside the MGA space:
        _clustererDomain.setNumCorepoints(numCorePoints);

        for (int i = 0; i < numClusterings; i++) {
            ClusteringTestUtil.CreateRandomClustering(Optional.of((Space)space), _clustererDomain,
                                                      _reproducerDomain, _transcriberDomain, _translatorDomain, _klonerDomain,
                                                      mutationRate, mutationRate, mutationRate, mutationRate, 100, 200,
                                                      _testData.getNumDimensions(), _testData.minValue(), _testData.valueRange());
        }

        Optional<Individual> initialBest = space.best();

        assertTrue(initialBest.isPresent(), "Found best clustering");
        double initialFitness = ((Clustering)initialBest.get()).fitness();

        assertTrue(initialFitness < 0, "Fitness less than zero");

        Clustering best;
        long time = System.currentTimeMillis();
        int searchCount = 0;
        do {
            best = (Clustering)space.search().get();
            if (searchCount % 100 == 0) {
                String report = String.format("Best of %d after %d iterations is: %s%n", space.numIndividuals(), searchCount, best);
                System.out.printf(report);
                _logger.fine(report);
            }
            searchCount++;
        } while (best.fitness() < Simulation.GetValue("targetFitness", -100.0)
                 &&
                 best.getGeneration() < Simulation.GetValue("maxGeneration", 150)
                 &&
                 searchCount < Simulation.GetValue("maxIterations", 10000));

        Machine.FlushLogger();
        _logger.info(String.format("{%d} Completed, best is %s", System.currentTimeMillis(), best));
        Machine.FlushLogger();
        assertTrue(best.fitness() > initialFitness, "Fitness improves");
        System.out.println("Best: " + space.best().get());
    }

}