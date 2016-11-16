package EvoEvo.york.machineMetaModel;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/** A Searchable space that implements an algorithm where the best member of the space is retained at each generation
 *  and all others are replicat3ed */
public class ElitistSpace extends Space implements SearchableSpace {
    private final static Logger _logger = Logger.getLogger("EvoEvo");

    private Optional<Dataset> _dataset;

    public ElitistSpace(Optional<Dataset> dataset) {
        super(Optional.empty());
        _dataset = dataset;
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

    /** Calculate the fitness of all contained individuals, throw away the worst half of them and allow the best
     *  half to reproduce. Answer the best individual.. */
    public Optional<Individual> search() {
        int size = this.numSubspaces();

        // Delete the worst half of the individuals:
        int proportionToLeave = Simulation.GetValue("eliteProportion", 2);
        int numToLeave = this.numSubspaces()/proportionToLeave;
        Collection<Space> subspaces = this.getSubspaces();
        subspaces
                .parallelStream()
                .sorted((j1,j2) -> ((Individual)j1).compareTo((Individual)j2))
                .skip(numToLeave)
                .forEach((s) -> this.removeSubspace(s));

        // Replicate each of the remaining individuals sufficientsly to get back to the original population:
        Collection<Space> individuals = this.getSubspaces();
        individuals
                .parallelStream()
                .forEach((i) -> this.addReplicant(proportionToLeave, (Individual)i));

        // Return the new best individual:
        Individual result = (Individual)this.getSubspaces()
                                      .parallelStream()
                                      .min((j1, j2) -> ((Individual)j1).compareTo((Individual)j2))
                                      .orElseThrow(() -> new MetaModelException("Cannot find best individual"));
        if (this.numSubspaces() == size) {
            return Optional.of(result);
        } else {
            throw new MetaModelException(String.format("Wrong number of individuals was %d and now is %d", size, this.numSubspaces()));
        }
    }

    private void addReplicant(int proportionToLeave, Individual i) {
        for (int c = 1; c < proportionToLeave; c++) {
            Individual replicant = i.replicate();
            this.addSubspace(replicant);
        }
    }
}
