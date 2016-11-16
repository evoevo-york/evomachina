package EvoEvo.york.machineMetaModel;

import java.util.List;
import java.util.Set;

/** A Reproducer is a machine that builds new individuals with a given genome.
 *  Todo: the code here is a sort of compelled reproduction with this class being in control. This is an explicit shortcut and needs to be addressed at some point. */
public class Reproducer extends Machine {

    /** Construct a Reproducer machine which has the given instruction code in the given domain. At the moment
     *  instances of this class just ignore their code... */
    public Reproducer(Space environment, List<Pearl> code, Domain domain) throws MetaModelException {
        super(environment, code, domain);
    }

    /** Execute the reproducer. In this case that means create a copy of the source structure's code using a Kloner and the
     *  encapsulated mutation operator, build a new individual space and make that the environment of a collection of new structures created using
     *  the mutated source genome.
     *
     *  @return any one of the structures in the new individual's repository. That is, the new individual is the environment of the answer. */
    @Override
    public Structure doIt() {
        // Make new individual space within the container of the reproducer's environment. That is, in the same sort
        // of container:
        Individual newIndividual = (Individual)_environment.klone();

        newIndividual.setGeneration(((Individual)_environment).getGeneration()+1);

        // Now clone, with errors, the machine template repository into the new individual;
        Kloner c = (Kloner)((Individual)_environment).locateMachine(Kloner.class);
        Set<Structure> newRepository = c.getNewRepository();
        newIndividual.setRepository(newRepository);

        // Some machines are primitive and are needed for the new individual to be viable. This will cause them to be constructed, albeit using the
        // templates mutated by the Kloner. Weirdly, we're going to coopt the machinery in the parent individual to do the expressing... However,
        // this is actually not that far from the biology where a daughter cell just gets a share of the machines in the parent cell
        newIndividual.getRepository()
                     .stream()
                     .filter(s -> s.getDomain().isEssential())
                     .forEach(s -> newIndividual.addMachine(((Individual)_environment).expressMachine(s)));

        // Todo: at some point we'll have to put things like metabolites into the new individual.

        // Return one of the structures in the new machine template repository:
        return newRepository.stream().findFirst().get();
    }
}
