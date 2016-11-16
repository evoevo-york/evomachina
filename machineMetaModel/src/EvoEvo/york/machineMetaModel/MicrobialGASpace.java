package EvoEvo.york.machineMetaModel;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/** A Searchable space that implements the Microbial GA algorithm for finding machina solutions */
public class MicrobialGASpace extends Space implements SearchableSpace {
    private final static Logger _logger = Logger.getLogger("EvoEvo");

    private Optional<Dataset> _dataset;

    public MicrobialGASpace(Optional<Dataset> dataset) {
        super(Optional.empty());
        _dataset = dataset;
    }

    @Override
    /** Implement the microbial GA algorithm which is to:
     *    1) Randomly select a pair of individuals
     *    2) Remove the least fit of that pair and replicate the other
     *  which leaves the population of the space the same as it was at the start of the process  */
    public Optional<Individual> search() {
        // Select two individuals:
        Individual i1 = (Individual)this.getSubspace(ThreadLocalRandom.current().nextInt(this.numSubspaces()));
        Individual i2;
        do {
            i2 = (Individual)this.getSubspace(ThreadLocalRandom.current().nextInt(this.numSubspaces()));
        } while (i1.equals(i2));

        // Work out which is the best and which the worst of the two:
        Individual best = i1.compareTo(i2) < 0 ? i1 : i2;
        Individual worst = best.equals(i1) ? i2 : i1;

        // Remove the worst from the container and replicate the best:
        int worstPos = this.findPosition(worst);
        int bestPos = this.findPosition(best);
        String wDesc = String.format("[%d:%d]", worstPos, worstPos);
        String bDesc = String.format("[%d:%d]", bestPos, bestPos);
        this.removeSubspace(worst);
        Space replicant = best.replicate();
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine(String.format("{%d} Murdering: %s %s", System.currentTimeMillis(), wDesc, worst));
            _logger.fine(String.format("{%d} Replicated parent %s %s into child %s %s", System.currentTimeMillis(), bDesc, best, wDesc, replicant));
        }

        this.addSubspace(replicant);

        // Answer the best, which may now be better than when we started:
        return this.best();
    }

    @Override
    public void replicateInto(Site target, Individual parent) {

    }

    @Override
    public Optional<Dataset> getDataset() {
        return _dataset;
    }

    @Override
    public Optional<Individual> best() {
        Optional<Space> best = this.getSubspaces()
                                        .stream()
                                        .min((j1, j2) -> ((Individual)j1).compareTo((Individual)j2));
        return Optional.of((Individual)best.get());
    }

    @Override
    /** This space is just a collection of indivduals, so answer the number of subspaces. */
    public long numIndividuals() {
        return this.numSubspaces();
    }

    @Override
    public void replicationRequest(Site source, Site destination) {

    }

    @Override
    public void run() {

    }
}
