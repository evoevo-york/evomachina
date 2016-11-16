package EvoEvo.york.tspTest;

import EvoEvo.york.machineMetaModel.Domain;
import EvoEvo.york.machineMetaModel.Kloner;
import EvoEvo.york.machineMetaModel.Machine;
import EvoEvo.york.machineMetaModel.Pearl;
import EvoEvo.york.machineMetaModel.Reproducer;
import EvoEvo.york.machineMetaModel.Structure;
import EvoEvo.york.machineMetaModel.Transcriber;
import EvoEvo.york.machineMetaModel.Translator;
import EvoEvo.york.machineMetaModel.Util;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/** TestNG tests for simple aspects of structures where the code represents a sequence of cities to be visited by a
 *  travelling salesman. */
public class SimpleTSPStructureTests {

    SimpleDomain _reproducerDomain;
    SimpleDomain _klonerDomain;
    SimpleDomain _transcriberDomain;
    SimpleDomain _translatorDomain;
    CityType _cityDomain;

    class SimpleDomain extends Domain {
        public SimpleDomain(String name, final Class<? extends Machine> machineClass) {
            super(name, machineClass);
        }
    }


    @BeforeMethod
    public void setUp() throws Exception {
        _cityDomain = new CityType("UK cities");

        City birmingham = _cityDomain.addCity("Birmingham");
        City london = _cityDomain.addCity("London");
        City liverpool = _cityDomain.addCity("Liverpool");
        City york = _cityDomain.addCity("York");

        _cityDomain.addCityDistance(birmingham, london, 2.87);
        _cityDomain.addCityDistance(birmingham, liverpool, 2.18);
        _cityDomain.addCityDistance(birmingham, york, 2.47);
        _cityDomain.addCityDistance(london, york, 4.02);
        _cityDomain.addCityDistance(liverpool, york, 2.43);
        _cityDomain.addCityDistance(liverpool, london, 4.43);

        _reproducerDomain = new SimpleDomain("Reproducer domain", Reproducer.class);
        _klonerDomain = new SimpleDomain("Kloner domain", Kloner.class);
        _transcriberDomain = new SimpleDomain("Transcriber domain", Transcriber.class);
        _translatorDomain = new SimpleDomain("Translater domain", Translator.class);
    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    @Test
    public void checkSimpleDistances() throws Exception {
        City york = _cityDomain.findCity("York");
        City liverpool = _cityDomain.findCity("Liverpool");


        assertEquals(_cityDomain.journeyTime(york, liverpool), 2.43);
    }

    @Test
    public void checkRouteLengthOfAShortJourney() throws Exception {
        // Create a new individual representing the route London/York/Birmingham/Liverpool/London
        List<Pearl> route = new ArrayList<>(4);
        route.add(_cityDomain.findCity("London"));
        route.add(_cityDomain.findCity("York"));

        // The top level space that contains the new individual:
        Journey container = new Journey(Optional.empty());
        Util.AddTTMachines(container, _transcriberDomain, _translatorDomain, _reproducerDomain, _klonerDomain, new ArrayList<>());

        // Create a machine template that describes this route:
        Structure s = new Structure(container, route, _cityDomain);
        container.addMachineTemplate(s);


        assertEquals(container.journeyTime(), 8.04);
    }

    @Test
    public void checkRouteLengthOfAnIndividualJourney() throws Exception {
        // Create a new individual representing the route London/York/Birmingham/Liverpool/London
        List<Pearl> route = constructSimpleRoute();

        // The top level space that contains the new individual:
        Journey container = new Journey(Optional.empty());
        Util.AddTTMachines(container, _transcriberDomain, _translatorDomain, _reproducerDomain, _klonerDomain, new ArrayList<>());

        // Create a machine template that describes this route:
        Structure s = new Structure(container, route, _cityDomain);
        container.addMachineTemplate(s);

        assertEquals(container.journeyTime(), 13.1);
    }

    private List<Pearl> constructSimpleRoute() {
        List<Pearl> route = new ArrayList<>(4);
        route.add(_cityDomain.findCity("London"));
        route.add(_cityDomain.findCity("York"));
        route.add(_cityDomain.findCity("Birmingham"));
        route.add(_cityDomain.findCity("Liverpool"));
        return route;
    }

    @Test
    public void checkRouteLengthOfAJourneyWhichHasBeenReversedByAReversingCopier() throws Exception {
        _cityDomain.setMutator((l, k) -> TestUtil.Reverse(l));

        // Create a new individual representing the route London/York/Birmingham/Liverpool/London
        List<Pearl> route = constructSimpleRoute();

        // Create a space that will container the new individual and any constructed copes:
        TSPConcreteSpace world = new TSPConcreteSpace();

        // The space that is the first new individual:
        Journey initialJourney = new Journey(Optional.of(world));
        Util.AddTTMachines(initialJourney, _transcriberDomain, _translatorDomain, _reproducerDomain, _klonerDomain, new ArrayList<>());

        initialJourney.addMachineTemplate(new Structure(initialJourney, route, _cityDomain));

        // replicate all the individuals which should mean we have two essentially identical members of the world:
        world.replicate();
        assertEquals(world.numSubspaces(), 2);

        // Check that the new journey is the Reverse of the original:
        Journey copiedJourney = world.getAnotherSpace(initialJourney);
        assertFalse(initialJourney == copiedJourney);

        TSPCalculator newCalculator = (TSPCalculator)copiedJourney.locateMachine(TSPCalculator.class);
        List<Pearl> newRoute = newCalculator.getCode();
        assertEquals(route, TestUtil.Reverse(newRoute));

        // But it should still be the same time as the journey has just been reversed:
        assertEquals(copiedJourney.journeyTime(), initialJourney.journeyTime());
    }


    /** Reverse the provided path so that the resulting list, other than the initial symbol, is reversed. */
    private List<Pearl> reversePartOfPath(List<Pearl> path) {
        List<Pearl> result = new ArrayList<>();
        result.add(path.get(0));
        for (int i=path.size()-2; i>0; i--)
            result.add(path.get(i));
        result.add(path.get(path.size()-1));
        return result;
    }

    @Test
    public void checkRouteLengthOfAJourneyWhichHasBeenModifiedByASpecific2OptCopier() throws Exception {
        // Install the mutator required:
        _cityDomain.setMutator((l, k) -> this.reversePartOfPath(l));

        // Create a new individual representing the route London/York/Birmingham/Liverpool/London
        List<Pearl> route = constructSimpleRoute();

        // Create a space that will contain the new individual and any constructed copies:
        TSPConcreteSpace world = new TSPConcreteSpace();

        // The space that is the first new individual:
        Journey initialJourney = new Journey(Optional.of(world));
        Util.AddTTMachines(initialJourney, _transcriberDomain, _translatorDomain, _reproducerDomain, _klonerDomain, new ArrayList<>());

        initialJourney.addMachineTemplate(new Structure(initialJourney, route, _cityDomain));

        // replicate all the individuals which should mean we have two essentially identical members of the world:
        world.replicate();
        assertEquals(world.numSubspaces(), 2);

        // Check that the new journey is a manipulation of the original:
        Journey copiedJourney = world.getAnotherSpace(initialJourney);
        assertFalse(initialJourney == copiedJourney);

        TSPCalculator calculator = (TSPCalculator)copiedJourney.locateMachine(TSPCalculator.class);
        List<Pearl> newRoute = calculator.getCode();

        assertEquals(route.size(), newRoute.size());
        assertTrue(newRoute.stream().allMatch((c) -> route.contains(c)));
        assertTrue(newRoute.stream().noneMatch((c) -> !route.contains(c)));

        // In this case, the route time is now different:
        assertFalse(copiedJourney.journeyTime() == initialJourney.journeyTime());
        assertEquals(copiedJourney.journeyTime(), 12.2);
    }

    @Test
    public void iterateTenJourneysAndLookAtBestOne() throws Exception {
        _cityDomain.setMutator((l, k) -> TestUtil.OptN(l, 2));

        // Create a new individual representing the route London/York/Birmingham/Liverpool/London
        List<Pearl> route = constructSimpleRoute();

        // Create a space that will container the new individual and any constructed copes:
        TSPConcreteSpace world = new TSPConcreteSpace();

        // Make 10 identical journeys:
        for (int i = 0; i<10; i++) {
            makeJourney(route, world, _cityDomain);
        }

        assertEquals(world.numSubspaces(), 10);

        Journey best = generateOneNewJourney(world);

        // Do 100 iterations:
        for (int i = 0; i<100; i++)
            best = generateOneNewJourney(world);

        assertEquals(best.journeyTime(), 11.5);
    }

    private Journey generateOneNewJourney(TSPConcreteSpace world) {
        Journey best = world.generate();
        System.out.println(best);
        return best;
    }

    private void makeJourney(List<Pearl> route, TSPConcreteSpace world, Domain domain) {
        Journey j = new Journey(Optional.of(world));
        Util.AddTTMachines(j, _transcriberDomain, _translatorDomain, _reproducerDomain, _klonerDomain, new ArrayList<>());

        j.addMachineTemplate(new Structure(j, route, domain));
    }
}