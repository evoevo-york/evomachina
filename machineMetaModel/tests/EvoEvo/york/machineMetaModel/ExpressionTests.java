package EvoEvo.york.machineMetaModel;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.testng.Assert.*;

/** test class for testing later version of meta model with more biological gene expression */
@Test ()
public class ExpressionTests {

    /** Domain for simple machines */
    class MachineDomain extends Domain {
        public MachineDomain(String name, final Class<? extends Machine> machineClass) {
            super(name, machineClass);
        }

        public MachineDomain(String name, Class<? extends Machine> machineType, Mutator mutator) {
            super(name, machineType, mutator);
        }
    }

    /** Container space for tests */
    class TestSpace extends Space {
        public TestSpace(Optional<Space> container) {
            super(container);
        }
    }

    class ConcreteIndividual extends Individual {
        public ConcreteIndividual(Optional<Space> container) {
            super(container);
        }

        @Override
        public int compareTo(Individual o) {
            return 0;
        }

        @Override
        public void run() {
        }
    }

    @Test
    public void createIndividualAndDoMinimalReplication() throws Exception {
        Space container = new TestSpace(Optional.empty());

        Individual i = new ConcreteIndividual(Optional.of(container));
        buildMinimalMachineStructures(i, (l, k) -> Domain.exactCopy(l));

        Individual newIndividual = i.replicate();

        // The new individual is in the same environment as the original
        assertEquals(i.getContainer(), newIndividual.getContainer(), "Containers are the same");

        // The new individual's machine template repository is the same size as the original's
        assertEquals(newIndividual.getRepository().size(), i.getRepository().size(), "Machine template repositories");
        assertTrue(i.getRepository().stream().noneMatch((s) -> newIndividual.getRepository().contains(s)), "Machine template repository contents");

        // The new individual does not, as yet, have the complete array of machines as some remain to be expressed:
        assertTrue(newIndividual._machines.size() < i._machines.size(), "Number of machines");

        // Machines in new individual support further replication:
        Individual anotherOne = newIndividual.replicate();

        // Check that the transcriber machine in all individuals has a non-zero length code:
        assertTranscriberCodeLength(i, 11);
        assertTranscriberCodeLength(newIndividual, 5); // 5 because the effect of properly transcribing the template will have been to remove the non-coding elements.
        assertTranscriberCodeLength(anotherOne, 5);
    }

    private void assertTranscriberCodeLength(Individual i, int l) {
        Transcriber t = (Transcriber)i.locateMachine(Transcriber.class);
        assertEquals(t.size(), l, "transcriber code length");
    }

    private void buildMinimalMachineStructures(Individual i, Domain.Mutator m) {
        // Add machine templates and machines necessary to just get going:
        i.addMachineTemplate(new Structure(i, new ArrayList<Pearl>(), new MachineDomain("reproducer domain", Reproducer.class)));

        Domain d = new MachineDomain("transcriber domain", Transcriber.class, m);
        List<Pearl> transcriberCode = new ArrayList<>();
        for (int p=5; p <=15; p++) {
            transcriberCode.add(new TranscriberPearl(String.valueOf(p), d));
        }
        i.addMachineTemplate(new Structure(i, transcriberCode, d));
        i.addMachine(d.constructMachine(i, transcriberCode));

        d = new MachineDomain("translator domain", Translator.class);
        i.addMachineTemplate(new Structure(i, new ArrayList<Pearl>(), d));
        i.addMachine(d.constructMachine(i, new ArrayList<>()));

        i.addMachineTemplate(new Structure(i, new ArrayList<Pearl>(), new MachineDomain("kloner domain", Kloner.class)));
    }

    /** Class for the units of the code for Transcriber machine */
    class TranscriberPearl extends Pearl {
        protected String _name;

        public TranscriberPearl(String name, Domain domain) {
            super(domain);
            _name = name;
        }

        /** For testing, this is always a non-coding unit if the name is longer than 1 character  */
        @Override
        public boolean isCoding() {
            return _name.length() <= 1;
        }
    }

    @Test
    public void createMinimalIndividualAndCheckMutationChangesLengthOfTranscriberCode() throws Exception {
        Space container = new TestSpace(Optional.empty());

        Individual i = new ConcreteIndividual(Optional.of(container));
        buildMinimalMachineStructures(i, (l, k) -> mutateUnitNames(l));

        Individual newIndividual = i.replicate();

        // Machines in new individual support further replication:
        Individual anotherOne = newIndividual.replicate();

        // Check that the transcriber machine in all individuals has a non-zero length code:
        assertTranscriberCodeLength(i, 11);
        assertTranscriberCodeLength(newIndividual, 6); // 6 because the effect of properly transcribing the template will have been to remove the non-coding elements but the mutation will have changed the number of coding units
        assertTranscriberCodeLength(anotherOne, 7);
    }

    private List<Pearl> mutateUnitNames(List<Pearl> l) {
        List<Pearl> result = new ArrayList<>();
        l.stream().forEach((u) -> result.add(mutateUnitName(u)));

        return result;
    }

    private Pearl mutateUnitName(Pearl u) {
        if (u instanceof TranscriberPearl) {
            TranscriberPearl tu = (TranscriberPearl)u;
            int name = Integer.valueOf(tu._name);
            Pearl result = new TranscriberPearl(String.valueOf(--name), tu.getDomain());
            return result;
        } else {
            return u;
        }
    }
}
