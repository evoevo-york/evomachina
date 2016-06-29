package EvoEvo.york.machineMetaModel;

import java.util.Optional;

/** An interface to say what a space means to be searchable. That is, a realising Space is an experiment that is being run.
 *  The realising class has to implement a search method that returns the best result so far. The notion of "best" will depend on what the
 *  class is that implements this interface.
 *
 *  This interface also extends Runnable. It's expected that the run method of a realising class will evolve  the solution so that successive
 *  invocations of the methods that realise the best operation will answer "better" and better solutions to the problem.
 *
 *  To some extent search() and run() are alternatives.
 *  todo: Should there therefore be two interfaces? */
public interface SearchableSpace extends Runnable {
    /** Answer the space, contained by the receiver, that is the best current individual solution contained by the receiver. This method is
     *  expected to be invoked repetitively as a solution, hopefully, converges. */
    Optional<Individual> search();

    /** The given site is inside this space and is empty. Replicate the supplied Individual into that space */
    void replicateInto(Site target, Individual parent);

    /** Answer the current "best" individual within the space. This does not attempt to converge on a solution, it merely
     *  provides the current best solution that has been found. */
    Optional<Individual> best();

    /** Register the notion that the given Site is a suitable target for replication */
    void replicationRequest(Site source, Site destination);

    /** Answer the number of individuals that this searchable space contains */
    long numIndividuals();
}
