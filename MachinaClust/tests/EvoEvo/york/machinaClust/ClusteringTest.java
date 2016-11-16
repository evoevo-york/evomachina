package EvoEvo.york.machinaClust;

import EvoEvo.york.machineMetaModel.Domain;
import EvoEvo.york.machineMetaModel.Kloner;
import EvoEvo.york.machineMetaModel.MicrobialGASpace;
import EvoEvo.york.machineMetaModel.Pearl;
import EvoEvo.york.machineMetaModel.Reproducer;
import EvoEvo.york.machineMetaModel.Simulation;
import EvoEvo.york.machineMetaModel.Space;
import EvoEvo.york.machineMetaModel.Transcriber;
import EvoEvo.york.machineMetaModel.Translator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static org.testng.Assert.*;

/** Clustering tests */
public class ClusteringTest {

    private Domain _reproducerDomain;
    private Domain _transcriberDomain;
    private Domain _translatorDomain;
    private ClusteringType _clustererDomain;
    private KlonerType _klonerDomain;
    private ClusterableDataset _testData;

    @BeforeMethod
    public void setUp() throws Exception {
        _testData = new ClusterableDataset(5);

        _clustererDomain = new ClusteringType("Clusterer domain", _testData, 10, -2, 4);

        _reproducerDomain = new Domain("Reproducer domain", Reproducer.class);
        _transcriberDomain = new Domain("Transcriber domain", Transcriber.class);
        _translatorDomain = new Domain("Translater domain", Translator.class);
        _klonerDomain = new KlonerType("MachinaClust Kloner domain", Kloner.class);

        Simulation.Initialise();
    }

    @Test (invocationCount = 20)
    public void compareTwoRandomClusterings() throws Exception {
        // Create two clusterings over the same sample dataset
        Clustering c1 = ClusteringTestUtil.CreateRandomClustering(Optional.empty(), _clustererDomain, _reproducerDomain, _transcriberDomain, _translatorDomain, _klonerDomain,
                                                                  0.01, 0.01, 0.01, 0.01, 10, 20, _testData.getNumDimensions(), 0, 1);
        Clustering c2 = ClusteringTestUtil.CreateRandomClustering(Optional.empty(), _clustererDomain, _reproducerDomain, _transcriberDomain, _translatorDomain, _klonerDomain,
                                                                  0.01, 0.01, 0.01, 0.01, 10, 20, _testData.getNumDimensions(), 0, 1);

        List<Pearl> c1Code = c1.locateMachine(ClusterCalculator.class).getCode();
        List<Pearl> c2Code = c2.locateMachine(ClusterCalculator.class).getCode();
        assertFalse(c1Code.equals(c2Code), "random clusterers are seldom equal");
    }

    @Test
    public void calculateFitnessProvidesANumberAndACollectionOfPopulatedCorepoints() throws Exception {
        // Create a suitable container with a random dataset:
        ClusterableDataset data = new ClusterableDataset(5);

        double x = 0.5;
        for (int i = 0; i < 10; i++) {
            x = 0.01*i;
            data.add(new Observation(new Double[]{x, x, x, x, x}, data));
        }
        Space space = new MicrobialGASpace(Optional.of(data));

        // Create random clustering
        Clustering c1 = ClusteringTestUtil.CreateRandomClustering(Optional.of(space), _clustererDomain, _reproducerDomain, _transcriberDomain, _translatorDomain, _klonerDomain,
                                                                  0.01, 0.01, 0.01, 0.01, 10, 20, data.getNumDimensions(), 2, -1);

        double fitness = c1.fitness();
        assertNotEquals(fitness, -123.45, "Default value returned");
        assertTrue(fitness < 0, "Fitness less than zero");

        ClusterCalculator cc = (ClusterCalculator)c1.locateMachine(ClusterCalculator.class);
        assertTrue(cc.numCorePoints() <= 10 && cc.numCorePoints() > 0, "Number of core points");

        int attachedObservations = 0;
        for (CorePoint cp : cc._corePoints.values()) attachedObservations += cp.getNumObservations();

        assertEquals(attachedObservations, 10, "number of observations attached to core points");
    }

}