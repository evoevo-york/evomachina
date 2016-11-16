package EvoEvo.york.machinaClust;

import EvoEvo.york.machineMetaModel.Individual;
import EvoEvo.york.machineMetaModel.Kloner;
import EvoEvo.york.machineMetaModel.Space;

import java.util.Optional;
import java.util.logging.Logger;

/** An individual for the machinaclust implementation. Here an individual represents a particular clustering of the source data into a set of
 *  subspace clusters. */
public class Clustering extends Individual {
    private final static Logger _logger = Logger.getLogger("EvoEvo");

    public Clustering(Optional<Space> container) {
        super(container);
    }

    @Override
    public void run() {

    }

    @Override
    public int compareTo(Individual that) {
        assert that instanceof Clustering;
        Double thisFit = this.fitness();
        Double thatFit = ((Clustering)that).fitness();

        return thatFit.compareTo(thisFit);
    }

    /** Answer the fitness of the receiver. This time is calculated (and memoised) by a specialised machine expressed by the receiver */
    /* package private */ Double fitness() {
            ClusterCalculator calculatorMachine = (ClusterCalculator)this.locateMachine(ClusterCalculator.class);
            return calculatorMachine.getFitness();
    }

    @Override
    public String toString() {
        ClusterCalculator cc = (ClusterCalculator)this.locateMachine(ClusterCalculator.class);
        Kloner k = (Kloner)this.locateMachine(Kloner.class);
        KlonerType kDomain = (KlonerType)k.getDomain();
        return String.format("Clustering[%d,%d,%d,%d,%d,%.8f,%.8f,%.8f,%.8f,%.8f]%s",
                             this.getGeneration(),this.getReplicationCount(),
                             this.totalCodeSize(ClusterCalculator.class), cc.getCode().size(),
                             cc.numCorepoints(),
                             kDomain.getDeletionMutationRate(k),
                             kDomain.getDuplicationMutationRate(k),
                             kDomain.getTranslocationMutationRate(k),
                             kDomain.getPointMutationRate(k),
                             this.fitness(), cc);
    }
}
