package EvoEvo.york.machinaClust;

import EvoEvo.york.machineMetaModel.Domain;
import EvoEvo.york.machineMetaModel.Pearl;
import EvoEvo.york.machineMetaModel.Space;
import EvoEvo.york.machineMetaModel.Structure;
import EvoEvo.york.machineMetaModel.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/** Useful functions for clustering tests */
public class ClusteringTestUtil {

    static Clustering CreateRandomClustering(Optional<Space> world, ClusteringType clustererDomain,
                                             Domain reproducerDomain, Domain transcriberDomain, Domain translatorDomain, Domain klonerDomain,
                                             double initialDeletionMutationRate, double initialDuplicationMutationRate, double initialTranslocationMutationRate, double initialPointMutationRate,
                                             int minPearls, int maxPearls,
                                             int numDimensions, double minValue, double valueRange) {
        // Create new individual:
        Clustering c = new Clustering(world);

        // Create a new kloner genome:
        List<Pearl> klonerCode = new ArrayList<>();
        klonerCode.add(new KlonerPearl(klonerDomain, initialDeletionMutationRate));
        klonerCode.add(new KlonerPearl(klonerDomain, initialDuplicationMutationRate));
        klonerCode.add(new KlonerPearl(klonerDomain, initialTranslocationMutationRate));
        klonerCode.add(new KlonerPearl(klonerDomain, initialPointMutationRate));

        // Add the basic machines to the individual:
        Util.AddTTMachines(c, transcriberDomain, translatorDomain, reproducerDomain, klonerDomain, klonerCode);

        // Create an initial random genome with between 10 and 20 random pearls in it:
        Random r = ThreadLocalRandom.current();
        int numPearls = r.nextInt(minPearls) + maxPearls - minPearls;

        List<Pearl> clusteringGenome = new ArrayList<>();
        for (int i = 0; i < numPearls; i++) {
            clusteringGenome.add(ClusterPearl.MakeRandom(clustererDomain, r, clustererDomain.getNumCorepoints(), numDimensions, valueRange, minValue));
        }

        c.addMachineTemplate(new Structure(c, clusteringGenome, clustererDomain));
        return c;
    }
}
