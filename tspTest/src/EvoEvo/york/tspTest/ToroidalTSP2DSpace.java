package EvoEvo.york.tspTest;

import EvoEvo.york.machineMetaModel.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/** A Toroidal space within the cells of which, individual journeys can live */
public class ToroidalTSP2DSpace extends Toroidal2DSpace implements SearchableSpace {
    private final static Logger _logger = Logger.getLogger("EvoEvo");

    /** The set of sites that individuals would like to replicate into */
    private Set<Toroid2DSite> _replications;

    /** Field used for synchronisation */
    private Map<Journey2DSite, Journey> _replicants;

    /** Construct the most likely situation with defined x and y size and no containing space: */
    public ToroidalTSP2DSpace(int xSize, int ySize) {
        super(Optional.empty(), xSize, ySize);
        for (int x = 0; x < xSize; x++ ) {
            for (int y = 0; y < ySize; y++) {
                this.addSubspace(new Journey2DSite(Optional.of(this), x, y));
            }
        }

        _replications = new HashSet<>();
    }

    /** Search around the toroidal space looking for the currently best solution */
    @Override
    public Optional<Individual> search() {
        deleteWorstJourneys();

        // Look at all cells and, if they're empty, allow the "best" of the neighbours to replicate into it.
        // First find all the empty cells
        Set<Space> emptyCells = this.getSubspaces()
                                    .parallelStream()
                                    .filter((s) -> s.isEmpty())
                                    .collect(Collectors.toCollection(HashSet::new));

        // For each empty cell, find its neighbours build up a collection of journeys to replicate:
        _replicants = new HashMap<>();
        emptyCells.parallelStream()
                  .forEach((s) -> findReplications((Journey2DSite)s));

        // Now replicate the found journeys:
        _replicants.keySet()
                   .stream()
                   .forEach((s) -> this.replicateInto(s, _replicants.get(s)));

        // Find the currently best journey and return that:
        return this.best();
    }

    /** Register the notion that the given Site is a suitable target for replication */
    @Override
    public synchronized void replicationRequest(Site source, Site destination) {
        if (_logger.isLoggable(Level.FINE))
            _logger.fine(String.format("{%d} Adding destination %s as replication target of source site %s", System.currentTimeMillis(), destination, source));
        _replications.add((Toroid2DSite)destination);
    }

    @Override
    public Optional<Dataset> getDataset() {
        return Optional.empty();
    }

    /** Run the receiver which, at the moment, means to carry out the requested replications */
    @Override
    public void run() {
        if (_replications.size() > 0) {
            Set<Toroid2DSite> targets;
            synchronized (this) {
                targets = _replications;
                _replications = new HashSet<>();
            }
            for (Toroid2DSite destination : targets) {
                if (destination.isEmpty()) {
                    Optional<Space> best;
                    try {
                        Set<Space> neighbours =  this.findNeighbours(destination.getXPosition(), destination.getYPosition(), s -> !s.isEmpty());
                        best = neighbours
                                   .stream()
                                   .map(s -> s.getASubspace())
                                   .min((j1, j2) -> ((Individual)j1).compareTo((Individual)j2));
                    } catch (Exception e) {
                        best = Optional.empty();
                    }
                    if (best.isPresent()) {
                        this.replicateInto(destination, (Individual)best.get());
                    }
                }
            }
        }
    }

    /** Answer the current "best" individual within the space. This is not memoized as that would imply serialising lots of things. Rather, it's calculated
     *  afresh each time.  */
    @Override
    public Optional<Individual> best() {
        Optional<Space> best;
        try {
            best = this.getSubspaces()
                       .stream()
                       .filter(s -> !s.isEmpty())
                       .map(sp -> sp.getASubspace())
                       .min((j1, j2) -> ((Individual)j1).compareTo((Individual)j2));
        } catch (Exception e) {
            // On occasions the space will not have a subspace by the time we get around to looking at it. Just press on regardless in that case:
            best = Optional.empty();
        }
        return best.isPresent() ? Optional.of((Individual)best.get()) : Optional.empty();
    }

    private void deleteWorstJourneys() {
        // Delete some journeys
        List<Space> orderedJourneys = this.getSubspaces()
                                          .parallelStream()
                                          .filter(s -> !s.isEmpty())
                                          .map((s) -> s.getASubspace())
                                          .sorted((j1, j2) -> ((Individual)j2).compareTo((Individual)j1))
                                          .collect(Collectors.toList());

        orderedJourneys.stream().limit(orderedJourneys.size()/2).forEach((j) -> j.getContainer().get().empty());
    }

    /** The supplied space is currently empty. Look at its neighbours and see if it's possible to find a decent
     *  journey to replicate into that space */
    private void findReplications(Journey2DSite s) {
        // Find the journeys that are neighbours of the space s:
        Set<Space> neighbours = this.findNeighbours(s.getXPosition(), s.getYPosition(), sp -> sp.numSubspaces() != 0);
        if (neighbours.size() == 1) {
            synchronized (_replicants) {
                _replicants.put(s, (Journey)neighbours.stream().findAny().get().getASubspace());
            }
        } else if (neighbours.size() != 0) {
            // Find the best journey of the possibilities and replicate that one, marking the others as less successful candidates:
            Journey bestMatch = (Journey)neighbours
                                                 .stream()
                                                 .map(sp -> sp.getASubspace())
                                                 .min((j1, j2) -> ((Individual)j1).compareTo((Individual)j2))
                                                 .get();
            synchronized (_replicants) {
                _replicants.put(s, bestMatch);
            }
        }
    }

    @Override
    public  void replicateInto(Site target, Individual parent) {
        synchronized (target) {
            Journey replicant = (Journey)parent.replicate();
            target.addSubspace(replicant);
            if (_logger.isLoggable(Level.FINE))
                _logger.fine(String.format("{%d} Replicated parent %s into child %s", System.currentTimeMillis(), parent.getContainer().get(), target));
        }
    }
}
