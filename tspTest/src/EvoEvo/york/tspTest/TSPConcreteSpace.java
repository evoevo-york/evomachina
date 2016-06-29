package EvoEvo.york.tspTest;

import EvoEvo.york.machineMetaModel.Individual;
import EvoEvo.york.machineMetaModel.MetaModelException;
import EvoEvo.york.machineMetaModel.SearchableSpace;
import EvoEvo.york.machineMetaModel.Site;
import EvoEvo.york.machineMetaModel.Space;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/** A concrete space used for TSP testing. That is, this space is the world in which journey instances live. */
public class TSPConcreteSpace extends Space implements SearchableSpace {
    Optional<Journey> _best;
    Optional<Journey> _worst;

    public TSPConcreteSpace(Optional<Space> container) {
        super(container);
        _best = Optional.empty();
        _worst = Optional.empty();
    }

    /** Iterate through all the members of the space, telling their constructors to make new individuals */
    public void replicate() {
        Set<Space> intermediateCollection = new HashSet<>(this.getSubspaces());
        intermediateCollection.stream().forEach((s) -> ((Journey)s).replicate());
    }

    /** Answer the current "best" individual within the space */
    @Override
    public Optional<Individual> best() {
        return _best.isPresent() ? Optional.of(_best.get()) : Optional.empty();
    }

    /** Unimplemented as this is just a test class */
    @Override
    public void replicateInto(Site target, Individual parent) {

    }

    /** Unimplemented as this is just a test class */
    @Override
    public void replicationRequest(Site source, Site destination) {

    }

    /** Unimplemented as this is just a test class */
    @Override
    public void run() {

    }

    public Journey getAnotherSpace(Journey j1) {
        return (Journey)this.getSubspaces().stream().filter((j2) -> j1 != j2).findFirst()
                                .orElseThrow(() -> new TSPTestException("Journey Not Found"));
    }

    /** Calculate all the journey times, delete the worst journey and replicate the best one. Answer the journey time
     *  of the best one after the replication */
    public Journey generate() {
        if (!_best.isPresent()) {
            calculateBest();
        }

        if (_best.get() == _worst.get())
            _best = Optional.of(this.getAnotherSpace(_worst.get()));
        this.removeSubspace(_worst.get());
        _worst = Optional.empty();
        _best.get().replicate();
        this.calculateBest();
        return _best.get();
    }

    private void calculateBest() {
        for (Space s : this.getSubspaces()) {
            this.updateBest((Journey)s);
        }
    }

    private void updateBest(Journey journey) {
        if (!_best.isPresent())
            _best = Optional.of(journey);
        else if (_best.get().journeyTime() > journey.journeyTime()) {
            _best = Optional.of(journey);
        }

        if (!_worst.isPresent())
            _worst = Optional.of(journey);
        else if (_worst.get().journeyTime() < journey.journeyTime()) {
            _worst = Optional.of(journey);
        }
    }

    /** Calculate the journey times of all the journeys, throw away the worst half of them and allow the best
     *  half to reproduce. Answer the best journey. */
    public Optional<Individual> search() {
        int size = this.numSubspaces();

        // Delete the worst half of the journeys:
        int numToLeave = this.numSubspaces()/2;
        Collection<Space> subspaces = this.getSubspaces();
        subspaces
                .parallelStream()
                .sorted((j1,j2) -> (int)(((Journey)j1).journeyTime() - ((Journey)j2).journeyTime()))
                .skip(numToLeave)
                .forEach((s) -> removeSubspace(s));

        // Prune any spaces that have the same genome but ensure we've still got half of the total as the number
        // of individual journeys
        subspaces = this.getSubspaces();
        Object[] sortedSpaces = subspaces.stream().sorted((j1,j2) -> (int)(((Journey)j1).journeyTime() - ((Journey)j2).journeyTime())).toArray();
        for (int i = 1; i < sortedSpaces.length-2; i++) {
            if (sortedSpaces[i].equals(sortedSpaces[i+1]))
                this.removeSubspace((Space)sortedSpaces[i]);
        }
        int numNewJourneysNeeded = (size/2) - this.numSubspaces();
        for (int i=0; i<numNewJourneysNeeded; i++) {
            ((Journey)sortedSpaces[0]).replicate();
        }

        // Replicate each of the remaining journeys:
        Collection<Space> journeys = this.getSubspaces();
        journeys
                .parallelStream()
                .forEach((j) -> ((Journey)j).replicate());

        // Return the new best journey:
        Journey result = (Journey)this.getSubspaces()
                                      .parallelStream()
                                      .min((j1, j2) -> (int)(((Journey)j1).journeyTime() - ((Journey)j2).journeyTime()))
                                      .get();
        if (this.numSubspaces() == size) {
            return Optional.of(result);
        } else {
            throw new MetaModelException("Wrong number of journeys was " + size + " and now is " + this.numSubspaces());
        }
    }
}

