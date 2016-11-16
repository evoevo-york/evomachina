package EvoEvo.york.tspTest;

import EvoEvo.york.machineMetaModel.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/** A concrete space used for TSP testing. That is, this space is the world in which journey instances live. */
public class TSPConcreteSpace extends ElitistSpace {
    Optional<Journey> _best;
    Optional<Journey> _worst;

    public TSPConcreteSpace() {
        super(Optional.empty());
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

    /** Unimplemented  */
    @Override
    public void replicateInto(Site target, Individual parent) {

    }

    /** Unimplemented as this is just a test class */
    @Override
    public void replicationRequest(Site source, Site destination) {

    }

    @Override
    public Optional<Dataset> getDataset() {
        return Optional.empty();
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
        else if (_best.get().compareTo(journey) > 0) {
            _best = Optional.of(journey);
        }

        if (!_worst.isPresent())
            _worst = Optional.of(journey);
        else if (_worst.get().compareTo(journey) < 0) {
            _worst = Optional.of(journey);
        }
    }

}

