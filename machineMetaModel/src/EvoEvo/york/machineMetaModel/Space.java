package EvoEvo.york.machineMetaModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/** A space is the abstract super class of all spaces whether they represent piece of physical space, an individual or even a collection of
 *  individuals that have some sort of relationship. A space can contain a collection of structures with no indication what purpose these have. Spaces,
 *  especially if they represent individuals, should always implement clonable as the clone() method will be used to create new objects without
 *  the need for reflection. */
public abstract class Space implements Cloneable {
    /** The containing space; empty if no such container */
    protected Optional<Space> _container;

    /** The set of structures that are the structure contents of this space, if any. These structures will be things like
     *  Transcription Units but will NOT be any machines inside an individual.
     *  todo: this seems vaguely non-Liskov, needs some thought. */
    protected Set<Structure> _contents;

    /** The spaces that are, hierarchically, contained by this space. This is private as it's likely that many of
     *  the subspaces will have their own threads and we want to synchronise access to this data. This is a list so that an
     *  index can be used if necessary */
    private List<Space> _subspaces;

    /** Construct a new space specifying the space that contains the new one, if any */
    public Space(Optional<Space> container) {
        _container = container;
        _contents = Collections.synchronizedSet(new HashSet<>());
        _container.ifPresent((s) -> s.addSubspace(this));
        _subspaces = Collections.synchronizedList(new ArrayList<>());
    }

    public Optional<Space> container() {
        return _container;
    }

    /** Add a structure as a new member of the receiver's contents. */
    public synchronized void addMember(Structure st) {
        _contents.add(st);
        st.setEnvironment(this);
    }

    /** Add a subspace to the receiver */
    public synchronized void addSubspace(Space s) {
        s.setContainer(this);
        _subspaces.add(s);
    }

    /** Add a subspace at a predefined index to the receiver. This allows things like 2d and 3d spaces to be constructed. */
    public synchronized void addSubspace(Space s, int index) {
        s.setContainer(this);
        _subspaces.add(index, s);
    }

    public Space getSubspace(int index) {
        return _subspaces.get(index);
    }

    public List<Space> subSpaces() {
        return new ArrayList<>(_subspaces);
    }

    public Optional<Space> getContainer() {
        return _container;
    }

    /** Method to clone the receiver, albeit without exposing the unchecked exception */
    protected synchronized Space klone() {
        try {
            return (Space)this.clone();
        } catch (CloneNotSupportedException e) {
           throw new MetaModelException("Cloned space does not support cloning");
        }
    }

    @Override
    protected synchronized Object clone() throws CloneNotSupportedException {
        Space result = (Space)super.clone();

        // Clone() will actually copy the contents of the space, so ensure that it's a blank canvas:
        result._contents = Collections.synchronizedSet(new HashSet<>());
        result._subspaces = Collections.synchronizedList(new ArrayList<>());

        List<Space> subspaces = new ArrayList<>(_subspaces);
        subspaces.stream().forEach((s) -> result.addSubspace(s.klone()));

        _container.ifPresent((c) -> c.addSubspace(result));

        return result;
    }

    /** Answer the number of subspaces "inside" the receiver */
    public int numSubspaces() {
        return _subspaces.size();
    }

    /** Answer the number of content structures that are in the space */
    public int numContents() {
        return _contents.size();
    }

    /** Answer a collection of the subspaces that exist "inside" the receiver */
    public List<Space> getSubspaces() {
        return new ArrayList<>(_subspaces);
    }

    public synchronized void removeSubspace(Space space) {
        _subspaces.remove(space);
    }

    /** Set the container of the receiver to a possibly new space. (Not nothing, note.) */
    public synchronized void setContainer(Space container) {
        _container.ifPresent((c) -> c.removeSubspace(this));
        _container = Optional.of(container);
    }

    /** Answer the first of the receiver's subspaces. This is included just because
     *  it makes programming easier. */
    public Space getASubspace() {
        return _subspaces.stream().findFirst().get();
    }

    /** Remove all of the receiver's subspaces */
    public synchronized void empty() {
        _subspaces = new ArrayList<>();
    }

    /** True if the receiver has no subspaces */
    public boolean isEmpty() {
        return this.numSubspaces() == 0;
    }

    /** Answer the ordinal position of the receiver in the receiver's list of subspaces. If the space is not found answer -1. */
    public int findPosition(Space space) {
        int pos = _subspaces.indexOf(space);
        return pos;
    }

    public long numIndividuals() {
        return this.subSpaces()
                   .stream()
                   .filter(s -> !s.isEmpty())
                   .count();
    }
}
