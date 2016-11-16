package EvoEvo.york.machinaClust;

import EvoEvo.york.machineMetaModel.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.testng.Assert.*;

/** A set of tests to test the genome mutation provided by the ClusteringType, the domain of cluster genomes. */
public class ClustererMutationTests {

    private ClusterableDataset _testData;

    @BeforeMethod
    public void setUp() throws Exception {
        _testData = new ClusterableDataset(5);

        Simulation.Initialise();
    }


    private List<Pearl> createTestGenome(Domain d, int initialValue, int length) {
        // Create test genome with 10 pearls in it:
        List<Pearl> testGenome = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            testGenome.add(new ClusterPearl(d, initialValue++));
        }
        return testGenome;
    }

    @Test (invocationCount = 20)
    public void checkLargeDeletionMutationOnlyEverReducesLengthOfGenomeAndOftenLeavesASingleMergedPearl() throws Exception {
        ClusteringType domain = new ClusteringType("Test cluster type", _testData, 5, -2, 4);

        // Create test genome with 10 pearls in it:
        List<Pearl> testGenome = this.createTestGenome(domain, 0, 10);

        List<Pearl> deletedGenome = domain.largeDeletion(testGenome, new StringBuilder());
        System.out.println("Initial: " + testGenome);
        System.out.println("Deleted: " + deletedGenome);

        assertEquals(deletedGenome.size(), testGenome.size() + domain.getLengthChange(), "deletion leaves wrong genome length");

        long numDiffPearls = deletedGenome.stream().filter(p -> !testGenome.contains(p)).count();
        assertTrue(numDiffPearls == 0 || numDiffPearls == 1, "all but [n]one  of the pearls was in the original list");
    }

    @Test (invocationCount = 20)
    public void checkLargeDuplicationsAlwaysCreatesALargerOrTheSameSizeGenomeWithNoNewMembers() throws Exception {
        ClusteringType domain = new ClusteringType("Test cluster type", _testData, 5, -2, 4);

        // Create test genome with 10 pearls in it:
        List<Pearl> testGenome = this.createTestGenome(domain, 0, 10);

        List<Pearl> duplicatedGenome = domain.largeDuplication(testGenome, new StringBuilder());
        System.out.println("Initial:    " + testGenome);
        System.out.println("Duplicated: " + duplicatedGenome);

        assertEquals(duplicatedGenome.size(), testGenome.size() + domain.getLengthChange(), "duplication leaves wrong length of genome");

        long newPearlCount = duplicatedGenome.stream().filter(p -> !testGenome.contains(p)).count();
        assertTrue(newPearlCount >=0 && newPearlCount <=2, "wrong number of new pearls found in duplicated genome");
    }

    @Test (invocationCount = 20)
    public void checkLargeTranslocationsAlwaysTheSameSizeGenomeWithAllTheSameMembers() throws Exception {
        ClusteringType domain = new ClusteringType("Test cluster type", _testData, 5, -2, 4);

        // Create test genome with 10 pearls in it:
        List<Pearl> testGenome = this.createTestGenome(domain, 0, 10);

        List<Pearl> translocatedGenome = domain.largeTranslocation(testGenome, new StringBuilder());
        System.out.println("Initial:      " + testGenome);
        System.out.println("Translocated: " + translocatedGenome);

        assertEquals(translocatedGenome.size(), testGenome.size()+ domain.getLengthChange(), "translocation leaves the same length of genome");
    }

    @Test (invocationCount = 20)
    public void mutateGenome() throws Exception {
        ClusteringType domain = new ClusteringType("Test cluster type", _testData, 5, -2, 4);
        KlonerType klonerDomain = new KlonerType("test kloner", Kloner.class);

        // Create the environment for the clusterer genome:
        Clustering c = new Clustering(Optional.empty());
        KlonerPearl deletionP = new KlonerPearl(klonerDomain, 0.01);
        KlonerPearl duplicationP = new KlonerPearl(klonerDomain, 0.01);
        KlonerPearl translocationP = new KlonerPearl(klonerDomain, 0.01);
        KlonerPearl pointP = new KlonerPearl(klonerDomain, 0.01);
        List<Pearl> klonerGenome = new ArrayList<>();
        klonerGenome.add(deletionP);
        klonerGenome.add(duplicationP);
        klonerGenome.add(translocationP);
        klonerGenome.add(pointP);
        Kloner k = new Kloner(c, klonerGenome, klonerDomain);

        // Create test genome with 100 pearls in it:
        List<Pearl> testGenome = this.createTestGenome(domain, 0, 100);
        Structure s = new Structure(c, testGenome, domain);

        Structure mutatedStructure = domain.mutate(s, k);

        System.out.println(String.format("Size before: %d; size after: %d", s.size(), mutatedStructure.size()));

        assertEquals(mutatedStructure.size(), s.size()+domain.getLengthChange(), "genome length");
    }

    @Test (invocationCount = 100)
    public void pointMutationDoesSomethingSomeOfTheTime() throws Exception {
        ClusteringType domain = new ClusteringType("Test cluster type", _testData, 5, -2, 4);
        List<Pearl> testGenome = this.createTestGenome(domain, 0, 100);
        Random r = ThreadLocalRandom.current();
        int differenceCount = 0;
        for (Pearl p : testGenome) {
            ClusterPearl cp = (ClusterPearl)p.klone();
            assertEquals(cp, p, "kloned pearl not equal");
            cp.pointMutate(r, 5, 5, 4, -2);
            if (!cp.equals(p)) differenceCount++;
        }

        System.out.println("Difference count is " + differenceCount);

        assertTrue(60 < differenceCount && differenceCount < 95, "Difference count not in expected range");
    }
}