package EvoEvo.york.machineMetaModel;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/** a special sort of space that is the subspace of another space within which one or more individuals can exist. Sites
 *  are runnables and, hence, in some implementations are the locus
 *  of concurrency. */
public abstract class Site extends Space
                           implements Runnable {
    private final static Logger _logger = Logger.getLogger("EvoEvo");

    /** record of the number of times the run() method has been invoked since a resident journey was
     *  last added to this object */
    protected int _runCount;

    /** Construct a new site. Sites are special in that all of the subspaces are required to be individuals */
    public Site(Optional<Space> container) {
        super(container);
    }

    /** Add a new subspace, which has to be an Individual. Initialise the runcount so that we count runs for that individual
     *  todo: fret about how this is going to work for sites that contain more than one individual. */
    @Override
    public synchronized void addSubspace(Space s) {
        this.checkTypeOfNewSubspace(s);
        _runCount = 0;
        super.addSubspace(s);
    }

    @Override
    public void addSubspace(Space s, int index) {
        checkTypeOfNewSubspace(s);
        super.addSubspace(s, index);
    }

    /** Ensure that the provided Space, which is a putative subspace, is actually an Individual */
    private void checkTypeOfNewSubspace(Space s) {
        if (!Individual.class.isAssignableFrom(s.getClass()))
            throw new MetaModelException(String.format("All subspaces of a Site are required to be Individuals. This one is a %s",
                                                       s.getClass().getName()));
    }

    /** This base version of the run method just asks any resident individuals to run. Other implementations, though, might have a different approach
     *  implemented by overriding this method. */
    @Override
    public void run() {
        // Look to see if we've got one or more subspaces which are individuals. If so, then ask them to run.
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest(String.format("{%d} Running %s", System.currentTimeMillis(), this));
        }
        this.getSubspaces()
            .stream()
            .forEach(s -> ((Individual)s).run());
    }

    /** Answer the set of neighbouring sites that have no contained individual subspaces. */
    public abstract Set<Space> emptyNeighbours();

    /** Answer the set of neighbouring sites that have contained individual subspaces */
    public abstract Set<Space> nonEmptyNeighbours();

    public int getRunCount() {
        return _runCount;
    }

    public void incRunCount() {
        _runCount++;
    }

    /** The supplied occupant, one of the receiver's subspaces, would like to replicate itself to the supplied destination Site. This behaviour is just
     *  handed off to the containing space. */
    public void replicationRequest(Site destination) {
        ((SearchableSpace)_container.get()).replicationRequest(this, destination);
    }
}
