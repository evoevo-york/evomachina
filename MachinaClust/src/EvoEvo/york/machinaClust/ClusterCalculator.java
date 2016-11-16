package EvoEvo.york.machinaClust;

import EvoEvo.york.machineMetaModel.Domain;
import EvoEvo.york.machineMetaModel.Machine;
import EvoEvo.york.machineMetaModel.MetaModelException;
import EvoEvo.york.machineMetaModel.Pearl;
import EvoEvo.york.machineMetaModel.SearchableSpace;
import EvoEvo.york.machineMetaModel.Space;
import EvoEvo.york.machineMetaModel.Structure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** The calculator machine for a clusterer that works out the fitness of the containing clusterer's genome. */
public class ClusterCalculator extends Machine {
    protected Optional<Double> _fitness;

    /** The set of core points found in this machine's environment, mapped from their id number */
    protected Map<Integer, CorePoint> _corePoints = new HashMap<>();

    public ClusterCalculator(Space environment, List<Pearl> code, Domain domain) throws MetaModelException {
        super(environment, code, domain);
        assert(ClusteringType.class.isAssignableFrom(domain.getClass()));
        _fitness = Optional.empty();
    }

    /** In the context of this particular machine, doIt is overridden to calculate the fitness of the container's genome. */
    @Override
    public Structure doIt() {
        // Find the dataset provided by the containing individual's containing space:
        ClusterableDataset ds = (ClusterableDataset)((SearchableSpace)_environment.getContainer()
                                                                                  .orElseThrow(() -> new MetaModelException("No containing Space")))
                                                                                  .getDataset()
                                                                                  .orElseThrow(() -> new MetaModelException("Space does not contain dataset"));

        // Find the corepoints in the containing clusterer's genome and for each allocate the closest observations:
        this.findCorePoints(ds);

        // Sum the total mismatch to calculate the fitness:
        double mismatch = 0.0;
        if (_corePoints.size() == 0) {
            // There are no corepoints, probably because the enture genome is non-coding.
            _fitness = Optional.of(Double.NEGATIVE_INFINITY);
        } else {
            for (CorePoint cp : _corePoints.values()) {
                mismatch += cp.totalMismatch();
            }
            _fitness = Optional.of(-mismatch);
        }

        // As there's nothing else, answer this:
        return this;
    }

    /** Create the set of core points that are described by the containing individual's genome and attach each of them to their nearest
     *  observations, calculating the mismatch between those observations and the corepoints. */
    private void findCorePoints(ClusterableDataset ds) {
        List<Pearl> genome = this.getCode();
        genome.forEach(cp -> this.accumulateClusterPearl((ClusterPearl)cp));

        for (Observation o : ds) {
            // Calculate mismatches between o and all of the corepoints:
            CorePoint best = null;
            double bestMismatch = 0.0;
            for (CorePoint cpt : _corePoints.values()) {
                double mismatch = cpt.calculateMismatch(o);
                if (best == null || mismatch < bestMismatch) {
                    best = cpt;
                    bestMismatch = mismatch;
                }
            }

            // Add the observation mismatch to the "best" corepoint
            if (best != null) best.add(bestMismatch);
        }
    }

    /** The provided cluster pearl is part of this machine's structure. Add the associated core point information in to the receiver's state.
     *  Note that we don't need to worry about the coding/non coding aspect of the pearl because that will have been stripped out  */
    private void accumulateClusterPearl(ClusterPearl p) {
        int cpId = p.getCorePoint();
        CorePoint cp = _corePoints.get(cpId);
        if (cp == null) {
            cp = new CorePoint(cpId);
            _corePoints.put(cpId, cp);
        }

        cp.addContributionToDimension(p.getDimension(), p.getValue());
    }

    public Double getFitness() {
        if (!_fitness.isPresent()) this.doIt();
        return _fitness.get();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(_code.size());
        _corePoints.values()
                   .stream()
                   .sorted((c1, c2) -> c2.getNumObservations() - c1.getNumObservations())
                   .forEach(cp -> b.append(String.format("-[%s]: ", cp)));
        b.append("///:  ");
        _code.forEach(p -> b.append(p + ", "));
        if (b.length() >= 10) {
            b.delete(b.length() - 2, b.length());
        }
        return b.toString();
    }

    public int numCorePoints() {
        return _corePoints.size();
    }

    public int numCorepoints() {
        return _corePoints.size();
    }
}
