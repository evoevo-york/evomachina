package EvoEvo.york.machineMetaModel;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;

/** A specific type of space that represents an individual "organism". (Not a type, a specific instance.) Such an
 *  object is required to have at least an instance of all machine that realise the EssentialMachine interface.
 *  As such, behaviour to ensure this is included. */
public abstract class Individual extends Space {
    private final static Logger _logger = Logger.getLogger("EvoEvo");

    /** The number of times this specific individual has replicated */
    protected int _replicationCount = 0;

    /** The set of structures that constitute the templates for machines made within this individual. */
    protected Set<Structure> _repository;

    /** The machines that exist within this individual, each of which is the result of expressing something
     *  from the machine template repository (aka repository). Some of the machines might, though, have been expressed
     *  from the "parent" individual's repository during replication and implanted directly into this object. */
    protected Set<Machine> _machines;

    /** Construct an empty new object within the supplied container. */
    public Individual(Optional<Space> container) {
        super(container);
        _machines = new HashSet<>();
        _repository = new HashSet<>();
    }

    /** Replicate this individual using the reproducer that should be available. Answer the new individual. */
    public synchronized Individual replicate() {
        _replicationCount++;
        Machine reproducer = this.locateMachine(Reproducer.class);

        _logger.fine(String.format("{%d} Replicating individual, replication count is now %d", System.currentTimeMillis(), _replicationCount));

        return (Individual)reproducer.doIt().getEnvironment();
    }

    /** Override the Object.clone() method so that the contents of the machine repositories are not copied when this object is clone()d. */
    @Override
    protected synchronized Object clone() throws CloneNotSupportedException {
        Individual result = (Individual)super.clone();
        result._machines = new HashSet<>();
        result._repository = new HashSet<>();
        result._replicationCount = 0;
        return result;
    }

    /** Require that concrete subclasses implement a runnable-like method. */
    public abstract void run();

    /** Look to see if we've got a machine of the requested type. If so, return it. If not then
     *  locate the appropriate part, if any, of the machine template repository and express the desired machine */
    public synchronized Machine locateMachine(final Class<? extends Machine> machineType) {
        // If we've already got a machine of the right sort then find it and return it. Note that this will
        // also find subclasses of the requested type.
        Optional<Machine> t = this.findMachine((Machine m) -> machineType.isAssignableFrom(m.getClass()));
        if (t.isPresent()) return t.get();

        // Find all components of the machine template repository that describe machines of the requested type and express them into
        // the local environment:
        _repository.stream()
                   .filter(s -> machineType.isAssignableFrom(s.getDomain().getMachineType()))
                   .forEach(s -> this.addMachine(this.expressMachine(s)));

        // Now find and return one of the new machines:
        return this.findMachine(m -> machineType.isAssignableFrom(m.getClass()))
                   .orElseThrow(() -> new MetaModelException("No suitable template for machine of type " + machineType.getName()));
    }

    /** Retrieve the first found machine of the receiver's contained machines
     *  that satisfies the supplied predicate */
    protected synchronized Optional<Machine> findMachine(Predicate<Machine> p) {
        return _machines.stream().filter(p).findFirst();
    }

    /** The supplied structure is part of the machine template repository and, as such, defines a specific sort of machine although
     *  perhaps with a mutated genome. Express the machine that is defined by this structure and add it to the set of machines that exists within
     *  the receiver. This is going to be a very controlled process of transcription and translation. Later on, perhaps, it should be made more
     *  independent and asynchronous. */
    public synchronized Machine expressMachine(Structure s) {
        // Find a transcriber machine. Note that this implies such a machine must always exist and the process of individual
        // replication ensures that this is so:
        Transcriber t = (Transcriber)this.locateMachine(Transcriber.class);

        // Here assume that we use the whole structure for transcription without the need for a begin and end location
        t.setSource(s);
        Structure transcriptionUnit = t.doIt();

        // Given the transcription unit, express the machine that that structure codes for using a translator machine
        // Again, the assumption at the moment is that only a single machine is coded for by the transcription unit and,
        // therefore, no begin and end locations are required.
        Translator tu = (Translator)this.locateMachine(Translator.class);
        tu.setSource(transcriptionUnit);
        return (Machine)tu.doIt();
    }

    /** The machine m has been recently expressed, add it to the collection of machines in this individual */
    public synchronized void addMachine(Machine m) {
        _machines.add(m);
        m.setEnvironment(this);
    }

    public synchronized Set<Structure> getRepository() {
        return _repository;
    }

    /** Set the collection of structures that is this individuals machine template repository, really its genome. This is used during the process
     *  of replication. */
    public synchronized void setRepository(Set<Structure> repository) {
        _repository = repository;

        // Make sure that each structure knows what its environment is:
        _repository.stream().forEach(s -> s.setEnvironment(this));
    }

    /** Add a new machine template to the receiver */
    public synchronized void addMachineTemplate(Structure template) {
        _repository.add(template);
    }

    public synchronized int getReplicationCount() {
        return _replicationCount;
    }
}
