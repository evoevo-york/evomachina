package EvoEvo.york.tspTest;


import EvoEvo.york.machineMetaModel.Individual;
import EvoEvo.york.machineMetaModel.Kloner;
import EvoEvo.york.machineMetaModel.Machine;
import EvoEvo.york.machineMetaModel.Simulation;
import EvoEvo.york.machineMetaModel.Site;
import EvoEvo.york.machineMetaModel.Space;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Math.abs;
import static java.lang.Math.max;

/** A test class representing an individual that is a single journey through the TSP world */
public class Journey extends Individual {

    private final static Logger _logger = Logger.getLogger("EvoEvo");

    public Journey(Optional<Space> container) {
        super(container);
    }

    /** Answer the time represented by the receiver. The time is calculated by a machine expressed into the
     *  receiver, the TSPCalculator machine. */
    public double journeyTime() {
        TSPCalculator calculatorMachine = (TSPCalculator)this.locateMachine(TSPCalculator.class);
        return calculatorMachine.getJourneyTime();
    }

    /** This journey is in a site of some form. If we're getting long in the tooth, commit suicide. If not then
     *  look to see if there is an adjacent empty site. If there is, then tell the enclosing environment
     *  that we're interested in replicating into that site. */
    @Override
    public void run() {
        // Remember the number of times the site's been run:
        Site site = ((Site)_container.get());
        site.incRunCount();
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest(String.format("{%d} Entered run for %s", System.currentTimeMillis(), site));
        }

        // First of all check to see if we should just commit suicide:
        int runCount = site.getRunCount();
        boolean shouldDie = false;
        if (Simulation.GetValue("programmedDeath", true)) {
            if (Simulation.GetValue("deathByOldAge", true)) {
                shouldDie = runCount > max(Simulation.GetValue("minRunCount", 400),
                                           Simulation.GetValue("replicationMultiplier", 0) * _replicationCount);
            } else {
                double suicideProbability = 1.0 / Simulation.GetValue("minRunCount", 600.0);
                shouldDie = ThreadLocalRandom.current().nextDouble() < suicideProbability;
            }
        }

        if (shouldDie) {
            if (_logger.isLoggable(Level.FINE))
                _logger.fine(String.format("{%d} Committing suicide: %s", System.currentTimeMillis(), site));
            site.empty();
        } else {
            // As we're still here, look to see if there's  a suitable site that we'd like spread our seed into:
            Set<Space> emptySites = ((Site)(_container.get())).emptyNeighbours();
            Optional<Space> neighbour = emptySites.parallelStream()
                                                  .findAny();
            if (neighbour.isPresent()) {
                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.finest(String.format("{%d} Found empty neighbour in %s", System.currentTimeMillis(), site));
                }
                // We've found an empty neighbour site, so tell our environment that we're interested in moving in:
                ((Site)(_container.get())).replicationRequest((Site)neighbour.get());
                if (_logger.isLoggable(Level.FINE))
                    _logger.fine(String.format("{%d} Requested replication of %s into %s", System.currentTimeMillis(), site, neighbour.get()));
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("Journey: [");
        b.append(this.getReplicationCount());
        b.append(", ");
        b.append((int)this.journeyTime());
        b.append(", ");
        Machine k = this.locateMachine(Kloner.class);
        b.append(k.toString());
        b.append("] (");
        TSPCalculator calculatorMachine = (TSPCalculator)this.locateMachine(TSPCalculator.class);
        b.append(calculatorMachine);
        b.append(")");
        return b.toString();
    }

}