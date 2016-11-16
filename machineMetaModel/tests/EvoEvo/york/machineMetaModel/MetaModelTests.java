package EvoEvo.york.machineMetaModel;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/** Trying out testNG with simple class based test of the Domain class. Domain is abstract so we'll need a simple
 *  local concrete subclass. */
public class MetaModelTests {

    ConcreteDomain _testDomain;
    SimpleDomain _simpleAlphabet;
    Pearl _lastS;

    class AlphabetMachine extends Machine {
        public AlphabetMachine(Space environment, List<Pearl> code, Domain domain) throws MetaModelException {
            super(environment, code, domain);
        }

        @Override
        public Structure doIt() {
            return null;
        }
    }

    class SimpleDomain extends Domain {
        public SimpleDomain(String name) {
            super(name, AlphabetMachine.class);
        }
    }

    class ConcreteDomain extends Domain {
        private List<Pearl> _extent;

        public ConcreteDomain(String name) {
            super(name, AlphabetMachine.class);
            _extent = new ArrayList<>();
        }

        public void add(Pearl s) {
            _extent.add(s);
            _lastS = s;
        }

        public String getName() {
            return _name;
        }

        public List<Pearl> getExtent() { return _extent; }

        public List<Pearl> subsequence(int start, int length) {
            return _extent.subList(start, start+length);
        }

        /** true of the actual symbol (not a clone) appears in the known extent */
        public boolean includes(Pearl a) {
            return _extent.stream().anyMatch(s -> s == a);
        }
    }

    class ConcretePearl extends Pearl {
        private String _name;
        public ConcretePearl(String name, Domain domain) {
            super(domain);
            _name = name;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            ConcretePearl result = (ConcretePearl)super.clone();
            result._name = _name;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            return ConcretePearl.class.isAssignableFrom(obj.getClass()) && _name.equals(((ConcretePearl)obj)._name);
        }
    }

    class ConcreteSpace extends Space {
        public ConcreteSpace(Optional<Space> container) {
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

    @BeforeMethod
    public void setUp() throws Exception {
        _testDomain = new ConcreteDomain("Test alphabet");
        String names = "abcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < 26; i++) {
            makeSymbol(names.substring(i, i + 1));
        }

        _simpleAlphabet = new SimpleDomain("Domain for manipulative machines");
    }

    private void makeSymbol(String name) {
        Pearl s1 = new ConcretePearl(name, _testDomain);
        _testDomain.add(s1);
    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    @Test
    public void domainRemembersName() throws Exception {
        assertTrue(_testDomain.getName().equals("Test alphabet"));
    }

    @Test
    public void clonedSymbolRemembersDomainButIsDifferentObjectNotInExtentOfTestDomain() throws Exception {
        Pearl a = (Pearl)_lastS.clone();
        assertTrue(a.isFrom(_lastS.getDomain()));
        assertFalse(a==_lastS);
        assertTrue(a.equals(_lastS));
        assertFalse(_testDomain.includes(a));
    }

    @Test
    public void symbolCreatedIsAMemberOfAlphabet() throws Exception {
        assertTrue(_lastS.isFrom(_testDomain));
    }

    @Test
    public void simpleStructureRemembersAlphabetAndUsesSymbolsFromTheSameAlphabet() throws Exception {
        List<Pearl> code = new ArrayList<>();
        code.addAll(_testDomain.getExtent());
        Structure st = new Structure(new ConcreteSpace(Optional.empty()), code, _testDomain);

        assertTrue(st.getDomain().equals(_testDomain));

        List<Pearl> stCode = st.getCode();
        assertTrue(stCode.stream().allMatch(s -> s.isFrom(_testDomain)));
    }

    @Test (expectedExceptions = MetaModelException.class)
    public void structureDoesntLikeHavingSymbolsDrawnFromWrongAlphabet() throws Exception {
        List<Pearl> code = new ArrayList<>();
        code.addAll(_testDomain.getExtent());
        Pearl s = new ConcretePearl("name", new ConcreteDomain("New alphabet"));
        code.add(s);

        new Structure(new ConcreteSpace(Optional.empty()), code, _testDomain);
    }

    @Test
    public void spaceCanContainOtherSpaces() throws Exception {
        Space container = new ConcreteSpace(Optional.empty());
        Space contents = new ConcreteSpace(Optional.of(container));
        Space contents2 = new ConcreteSpace(Optional.of(container));
        Space contents3 = new ConcreteSpace(Optional.of(container));

        assertTrue(container.subSpaces().stream().anyMatch((s) -> s.equals(contents)));
        assertTrue(container.subSpaces().stream().anyMatch((s) -> s.equals(contents2)));
        assertTrue(container.subSpaces().stream().anyMatch((s) -> s.equals(contents3)));
    }

    @Test
    public void cloningASpaceReplicatesSubspacesButTheyHaveEmptyRepositories() throws Exception {
        Space container = new ConcreteSpace(Optional.empty());
        Space contents = new ConcreteSpace(Optional.of(container));
        Space contents2 = new ConcreteSpace(Optional.of(container));
        Space contents3 = new ConcreteSpace(Optional.of(container));

        Structure entireAlphabet = new Structure(contents, _testDomain._extent, _testDomain);
        contents.addMember(entireAlphabet);

        Space clonedSpace = (Space)container.clone();
        assertEquals(container.numSubspaces(), 3);
        assertEquals(clonedSpace.numSubspaces(), 3);

        assertTrue(container.getSubspaces().stream().noneMatch((s) -> clonedSpace.getSubspaces().contains(s)));
        assertTrue(container.getSubspaces().stream().anyMatch((s) -> s.numContents() == 1));
        assertEquals(container.getSubspaces().stream().filter((s) -> s.numContents() == 0).count(), 2);
        assertEquals(clonedSpace.getSubspaces().stream().filter((s) -> s.numContents() == 0).count(), 3);
    }


    @Test
    public void clonedSpaceIncludesNoCopiesOfStructures() throws Exception {
        Space superSpace = new ConcreteSpace(Optional.empty());
        Structure entireAlphabet = new Structure(superSpace, _testDomain._extent, _testDomain);
        superSpace.addMember(entireAlphabet);
        Machine m = new AlphabetMachine(superSpace, new ArrayList<>(), _testDomain);
        superSpace.addMember(m);
        Space subspace = new ConcreteSpace(Optional.of(superSpace));

        Space clonedSpace = (Space)superSpace.clone();
        assertEquals(clonedSpace.numSubspaces(), 1);

        assertEquals(clonedSpace.numContents(), 0);
    }

    class TestToroidal2DSpace extends Toroidal2DSpace {
        public TestToroidal2DSpace(Optional<Space> container, int xSize, int ySize) {
            super(container, xSize, ySize);
            for (int i = 0; i < xSize; i++) {
                for (int j = 0; j < ySize; j++) {
                    this.addSubspace(new Toroid2DSite(Optional.of(this), i, j));
                }
            }
        }
    }

    @Test
    public void emptyToroidalSpaceHasNoNonEmptyNeighbours() {
        Toroidal2DSpace world = new TestToroidal2DSpace(Optional.empty(), 10, 10);

        assertTrue(world.getSubspaces().stream().allMatch((s) -> numNeighbours(world, s) == 0), "All neighbours should be empty");
    }

    private int numNeighbours(Toroidal2DSpace world, Space s) {
        Set<Space> neighbours = world.findNeighbours(((Toroid2DSite)s).getXPosition(),
                                                     ((Toroid2DSite)s).getYPosition(),
                                                      sp -> sp.numSubspaces() != 0);
        return neighbours.size();
    }

    @Test
    public void toroidalSpaceWithSingleIndividualInTheMiddle() {
        Toroidal2DSpace world = new TestToroidal2DSpace(Optional.empty(), 9, 9);

        Space individual = new ConcreteIndividual(Optional.empty());
        world.getSubspace(4, 4).addSubspace(individual);
        assertEquals(world.getSubspaces().stream().filter((s) -> numNeighbours(world, s)==0).count(), 73, "Central node and outer nodes have no neighbours");
        assertEquals(world.getSubspaces().stream().filter((s) -> numNeighbours(world, s)==1).count(), 8, "Nodes around central node have a single neighbour");
    }

    @Test
    public void toroidalSpaceWithSingleIndividualInTheMiddleOfARectangularSpace() {
        Toroidal2DSpace world = new TestToroidal2DSpace(Optional.empty(), 15, 25);

        Space individual = new ConcreteIndividual(Optional.empty());
        world.getSubspace(4, 10).addSubspace(individual);

        assertEquals(world.getSubspaces().stream().filter((s) -> numNeighbours(world, s)==0).count(), 367, "Central node and outer nodes have no neighbours");
        assertEquals(world.getSubspaces().stream().filter((s) -> numNeighbours(world, s)==1).count(), 8, "Nodes around central node have a single neighbour");
    }
}