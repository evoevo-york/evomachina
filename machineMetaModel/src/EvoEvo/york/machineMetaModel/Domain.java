package EvoEvo.york.machineMetaModel;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/** A single domain used as the code for a specific Structure.
 *  All of the Pearls used by a particular structure are required to be drawn from the same Domain */
public class Domain {
    private final static Logger _logger = Logger.getLogger("EvoEvo");

    /** Domains have a name */
    protected String _name;

    public void setMutator(Mutator m) {
        _mutator = m;
    }

    /** Interface to which a mutator mechanism must conform. A FunctionalInterface so that lambda expressions
     *  may be used to realise the interface. */
    @FunctionalInterface
    public interface Mutator {
        /** Mutate the  supplied Pearl sequence and answer the mutation. The Kloner that is requesting the  mutation is provided
         *  to allow for callbacks for any additional information required. */
        List<Pearl> mutate(List<Pearl> original, Kloner klonerMachine);
    }

    /** A domain is essentially the type of a structure and, as such, the mutations that are applicable to
     *  structures of that type are implemented by this mutator. */
    protected Mutator _mutator;

    /** The type of machine that this domain relates to and which it encodes for. The domain uses this class, by reflection, to create new
     *  instances of the machine type in question. */
    protected Class<? extends Machine> _machineType;

    /** As part of the translation process, construct a new machine of the type described by this domain and initialise it
     *  with the supplied list of pearls as its code.  */
    public Machine constructMachine(Space environment, List<Pearl> code) {
        try {
            Constructor<? extends Machine> cons = _machineType.getConstructor(Space.class, List.class, Domain.class);
            return cons.newInstance(environment, code, this);
        } catch (Exception e) {
            throw new MetaModelException("Cannot construct machine of type " + _machineType.getName());
        }
    }

    /** True if the machine type represented by the receiver is deemed to be essential. Essential machines are those types that must exist in a new individual.
     *  Essentiality is encoded by the machine type in question implementing the marker interface EssentialMachine. */
    public boolean isEssential() {
        return EssentialMachine.class.isAssignableFrom(_machineType);
    }

    /** Answer a description of the provided machine in the receiving domain */
    public String description(Machine machine) {
        return "-";
    }

    /** Principal constructor which defines the name, class of generated machines and the applicable mutator implementation. */
    public Domain(String name, Class<? extends Machine> machineType, Mutator mutator) {
        _name = name;
        _machineType = machineType;
        _mutator = mutator;
    }

    /** Construct a new domain defining its name and using by default a do-nothing mutator */
    public Domain(String name, Class<? extends Machine> machineType) {
        this(name, machineType, (l, k) -> exactCopy(l));
    }

    /** A local realisation of the Mutator interface that answers an exact copy of the supplied code. That is, it does no mutation at all. */
    public static List<Pearl> exactCopy(List<Pearl> l) {
        return l.stream().map(s -> s.klone()).collect(Collectors.toList());
    }

    /** The provided structure is drawn from this domain. Answer a copy of the structure, potentially mutated.
     *  The invoking Kloner machine is also provided. */
    public Structure mutate(Structure initial, Kloner kloner) {
        if (!initial.getDomain().equals(this)) throw new MetaModelException("Wrong Domain");

        List<Pearl> newSequence = _mutator.mutate(initial.getCode(), kloner);
        return new Structure(initial.getEnvironment(), newSequence, this);
    }

    /** If the receiver is not the type of all of the pearls in the supplied list then complain with an exception */
    public void checkTypeOfAll(List<Pearl> list) {
        if (!list.stream().allMatch((s) -> s.isFrom(this))) {
            throw new MetaModelException("Wrong Alphabet");
        }
    }

    public Class getMachineType() {
        return _machineType;
    }
}
