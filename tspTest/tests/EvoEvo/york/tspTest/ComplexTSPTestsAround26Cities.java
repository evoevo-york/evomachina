package EvoEvo.york.tspTest;

import EvoEvo.york.machineMetaModel.Kloner;
import EvoEvo.york.machineMetaModel.Pearl;
import EvoEvo.york.machineMetaModel.Reproducer;
import EvoEvo.york.machineMetaModel.Simulation;
import EvoEvo.york.machineMetaModel.Structure;
import EvoEvo.york.machineMetaModel.Transcriber;
import EvoEvo.york.machineMetaModel.Translator;
import EvoEvo.york.machineMetaModel.Util;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import EvoEvo.york.machineMetaModel.Domain;
import EvoEvo.york.machineMetaModel.Machine;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.testng.Assert.*;

/** A set of tests for a (much) more complex TSP test. The underlying city distance data is an adjacency matrix that looks like this:
 *
 *   0  83  93 129 133 139 151 169 135 114 110  98  99  95  81 152 159 181 172 185 147 157 185 220 127 181
 *  83   0  40  53  62  64  91 116  93  84  95  98  89  68  67 127 156 175 152 165 160 180 223 268 179 197
 *  93  40   0  42  42  49  59  81  54  44  58  64  54  31  36  86 117 135 112 125 124 147 193 241 157 161
 *  129  53  42   0  11  11  46  72  65  70  88 100  89  66  76 102 142 156 127 139 155 180 228 278 197 190
 *  133  62  42  11   0   9  35  61  55  62  82  95  84  62  74  93 133 146 117 128 148 173 222 272 194 182
 *  139  64  49  11   9   0  39  65  63  71  90 103  92  71  82 100 141 153 124 135 156 181 230 280 202 190
 *  151  91  59  46  35  39   0  26  34  52  71  88  77  63  78  66 110 119  88  98 130 156 206 257 188 160
 *  169 116  81  72  61  65  26   0  37  59  75  92  83  76  91  54  98 103  70  78 122 148 198 250 188 148
 *  135  93  54  65  55  63  34  37   0  22  39  56  47  40  55  37  78  91  62  74  96 122 172 223 155 128
 *  114  84  44  70  62  71  52  59  22   0  20  36  26  20  34  43  74  91  68  82  86 111 160 210 136 121
 *  110  95  58  88  82  90  71  75  39  20   0  18  11  27  32  42  61  80  64  77  68  92 140 190 116 103
 *  98  98  64 100  95 103  88  92  56  36  18   0  11  34  31  56  63  85  75  87  62  83 129 178 100  99
 *  99  89  54  89  84  92  77  83  47  26  11  11   0  23  24  53  68  89  74  87  71  93 140 189 111 107
 *  95  68  31  66  62  71  63  76  40  20  27  34  23   0  15  62  87 106  87 100  93 116 163 212 132 130
 *  81  67  36  76  74  82  78  91  55  34  32  31  24  15   0  73  92 112  96 109  93 113 158 205 122 130
 *  152 127  86 102  93 100  66  54  37  43  42  56  53  62  73   0  44  54  26  39  68  94 144 196 139  95
 *  159 156 117 142 133 141 110  98  78  74  61  63  68  87  92  44   0  22  34  38  30  53 102 154 109  51
 *  181 175 135 156 146 153 119 103  91  91  80  85  89 106 112  54  22   0  33  29  46  64 107 157 125  51
 *  172 152 112 127 117 124  88  70  62  68  64  75  74  87  96  26  34  33   0  13  63  87 135 186 141  81
 *  185 165 125 139 128 135  98  78  74  82  77  87  87 100 109  39  38  29  13   0  68  90 136 186 148  79
 *  147 160 124 155 148 156 130 122  96  86  68  62  71  93  93  68  30  46  63  68   0  26  77 128  80  37
 *  157 180 147 180 173 181 156 148 122 111  92  83  93 116 113  94  53  64  87  90  26   0  50 102  65  27
 *  185 223 193 228 222 230 206 198 172 160 140 129 140 163 158 144 102 107 135 136  77  50   0  51  64  58
 *  220 268 241 278 272 280 257 250 223 210 190 178 189 212 205 196 154 157 186 186 128 102  51   0  93 107
 *  127 179 157 197 194 202 188 188 155 136 116 100 111 132 122 139 109 125 141 148  80  65  64  93   0  90
 *  181 197 161 190 182 190 160 148 128 121 103  99 107 130 130  95  51  51  81  79  37  27  58 107  90   0
 *
 *  This data comes from https://people.sc.fsu.edu/~jburkardt/datasets/tsp/tsp.html, consulted on 1601182310
 *
 *  The optimal route is:
 *  (1, 25, 24, 23, 26, 22, 21, 17, 18, 20, 19, 16, 11, 12, 13, 15, 14, 10, 9, 8, 7, 5, 6, 4, 3, 2, 1)
 *
 *  with a length of 937 */
@Test ()
public class ComplexTSPTestsAround26Cities {
    private static final int _NUM_CITIES = 26;

    SimpleDomain _reproducerDomain;
    SimpleDomain _klonerDomain;
    SimpleDomain _transcriberDomain;
    SimpleDomain _translatorDomain;
    CityType _cityDomain;

    class SimpleDomain extends Domain {
        public SimpleDomain(String name, final Class<? extends Machine> machineType) {
            super(name, machineType);
        }
    }

    @BeforeMethod
    public void setUp() throws Exception {
        _cityDomain = new CityType("TSP cities");

        // Add cities just called "1" to "26", corresponding to the rows and columns in the matrix above
        for (int c = 1; c <= _NUM_CITIES; c++) {
            _cityDomain.addCity(String.valueOf(c));
        }

        // Add distances between cities:
        addCityDistances(1, _cityDomain.findCity(String.valueOf(1)), new int[]    {  0, 83, 93,129,133,139,151,169,135,114,110, 98, 99, 95, 81,152,159,181,172,185,147,157,185,220,127,181});
        addCityDistances(2, _cityDomain.findCity(String.valueOf(2)), new int[]   { 83,  0, 40, 53, 62, 64, 91,116, 93, 84, 95, 98, 89, 68, 67,127,156,175,152,165,160,180,223,268,179,197});
        addCityDistances(3, _cityDomain.findCity(String.valueOf(3)), new int[]   { 93, 40,  0, 42, 42, 49, 59, 81, 54, 44, 58, 64, 54, 31, 36, 86,117,135,112,125,124,147,193,241,157,161});
        addCityDistances(4, _cityDomain.findCity(String.valueOf(4)), new int[]   {129, 53, 42,  0, 11, 11, 46, 72, 65, 70, 88,100, 89, 66, 76,102,142,156,127,139,155,180,228,278,197,190});
        addCityDistances(5, _cityDomain.findCity(String.valueOf(5)), new int[]   {133, 62, 42, 11,  0,  9, 35, 61, 55, 62, 82, 95, 84, 62, 74, 93,133,146,117,128,148,173,222,272,194,182});
        addCityDistances(6, _cityDomain.findCity(String.valueOf(6)), new int[]   {139, 64, 49, 11,  9,  0, 39, 65, 63, 71, 90,103, 92, 71, 82,100,141,153,124,135,156,181,230,280,202,190});
        addCityDistances(7, _cityDomain.findCity(String.valueOf(7)), new int[]   {151, 91, 59, 46, 35, 39,  0, 26, 34, 52, 71, 88, 77, 63, 78, 66,110,119, 88, 98,130,156,206,257,188,160});
        addCityDistances(8, _cityDomain.findCity(String.valueOf(8)), new int[]   {169,116, 81, 72, 61, 65, 26,  0, 37, 59, 75, 92, 83, 76, 91, 54, 98,103, 70, 78,122,148,198,250,188,148});
        addCityDistances(9, _cityDomain.findCity(String.valueOf(9)), new int[]   {135, 93, 54, 65, 55, 63, 34, 37,  0, 22, 39, 56, 47, 40, 55, 37, 78, 91, 62, 74, 96,122,172,223,155,128});
        addCityDistances(10, _cityDomain.findCity(String.valueOf(10)), new int[] {114, 84, 44, 70, 62, 71, 52, 59, 22,  0, 20, 36, 26, 20, 34, 43, 74, 91, 68, 82, 86,111,160,210,136,121});
        addCityDistances(11, _cityDomain.findCity(String.valueOf(11)), new int[] {110, 95, 58, 88, 82, 90, 71, 75, 39, 20,  0, 18, 11, 27, 32, 42, 61, 80, 64, 77, 68, 92,140,190,116,103});
        addCityDistances(12, _cityDomain.findCity(String.valueOf(12)), new int[] { 98, 98, 64,100, 95,103, 88, 92, 56, 36, 18,  0, 11, 34, 31, 56, 63, 85, 75, 87, 62, 83,129,178,100, 99});
        addCityDistances(13, _cityDomain.findCity(String.valueOf(13)), new int[] { 99, 89, 54, 89, 84, 92, 77, 83, 47, 26, 11, 11,  0, 23, 24, 53, 68, 89, 74, 87, 71, 93,140,189,111,107});
        addCityDistances(14, _cityDomain.findCity(String.valueOf(14)), new int[] { 95, 68, 31, 66, 62, 71, 63, 76, 40, 20, 27, 34, 23,  0, 15, 62, 87,106, 87,100, 93,116,163,212,132,130});
        addCityDistances(15, _cityDomain.findCity(String.valueOf(15)), new int[] { 81, 67, 36, 76, 74, 82, 78, 91, 55, 34, 32, 31, 24, 15,  0, 73, 92,112, 96,109, 93,113,158,205,122,130});
        addCityDistances(16, _cityDomain.findCity(String.valueOf(16)), new int[] {152,127, 86,102, 93,100, 66, 54, 37, 43, 42, 56, 53, 62, 73,  0, 44, 54, 26, 39, 68, 94,144,196,139, 95});
        addCityDistances(17, _cityDomain.findCity(String.valueOf(17)), new int[] {159,156,117,142,133,141,110, 98, 78, 74, 61, 63, 68, 87, 92, 44,  0, 22, 34, 38, 30, 53,102,154,109, 51});
        addCityDistances(18, _cityDomain.findCity(String.valueOf(18)), new int[] {181,175,135,156,146,153,119,103, 91, 91, 80, 85, 89,106,112, 54, 22,  0, 33, 29, 46, 64,107,157,125, 51});
        addCityDistances(19, _cityDomain.findCity(String.valueOf(19)), new int[] {172,152,112,127,117,124, 88, 70, 62, 68, 64, 75, 74, 87, 96, 26, 34, 33,  0, 13, 63, 87,135,186,141, 81});
        addCityDistances(20, _cityDomain.findCity(String.valueOf(20)), new int[] {185,165,125,139,128,135, 98, 78, 74, 82, 77, 87, 87,100,109, 39, 38, 29, 13,  0, 68, 90,136,186,148, 79});
        addCityDistances(21, _cityDomain.findCity(String.valueOf(21)), new int[] {147,160,124,155,148,156,130,122, 96, 86, 68, 62, 71, 93, 93, 68, 30, 46, 63, 68,  0, 26, 77,128, 80, 37});
        addCityDistances(22, _cityDomain.findCity(String.valueOf(22)), new int[] {157,180,147,180,173,181,156,148,122,111, 92, 83, 93,116,113, 94, 53, 64, 87, 90, 26,  0, 50,102, 65, 27});
        addCityDistances(23, _cityDomain.findCity(String.valueOf(23)), new int[] {185,223,193,228,222,230,206,198,172,160,140,129,140,163,158,144,102,107,135,136, 77, 50,  0, 51, 64, 58});
        addCityDistances(24, _cityDomain.findCity(String.valueOf(24)), new int[] {220,268,241,278,272,280,257,250,223,210,190,178,189,212,205,196,154,157,186,186,128,102, 51,  0, 93,107});
        addCityDistances(25, _cityDomain.findCity(String.valueOf(25)), new int[] {127,179,157,197,194,202,188,188,155,136,116,100,111,132,122,139,109,125,141,148, 80, 65, 64, 93,  0, 90});
        addCityDistances(26, _cityDomain.findCity(String.valueOf(26)), new int[] {181,197,161,190,182,190,160,148,128,121,103, 99,107,130,130, 95, 51, 51, 81, 79, 37, 27, 58,107, 90,  0});


        _reproducerDomain = new SimpleDomain("Reproducer domain", Reproducer.class);
        _klonerDomain = new SimpleDomain("Kloner domain", Kloner.class);
        _transcriberDomain = new SimpleDomain("Transcriber domain", Transcriber.class);
        _translatorDomain = new SimpleDomain("Translater domain", Translator.class);

        Simulation.Initialise();
    }

    private void addCityDistances(int cityNum, City from, int[] distances) {
        for (int c = 1; c <=_NUM_CITIES; c++) {
            if (c != cityNum) {
                City to = _cityDomain.findCity(String.valueOf(c));
                _cityDomain.addCityDistance(from, to, distances[c-1]);
            }
        }
    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    @Test (priority = 1)
    public void testSimpleDistances1() throws Exception {
        City c1 = _cityDomain.findCity("1");
        City c15 = _cityDomain.findCity("15");

        assertEquals(_cityDomain.journeyTime(c1, c15), 81.0);
    }

    @Test (priority = 1)
    public void testSimpleDistances2() throws Exception {
        City c22 = _cityDomain.findCity("22");
        City c25 = _cityDomain.findCity("25");


        assertEquals(_cityDomain.journeyTime(c22, c25), 65.0);
    }


    @Test (priority = 2)
    public void ensureTripRoundAllCitiesIsTheSameDistanceRegardlessOfTheDirectionTaken() throws Exception {
        _cityDomain.setMutator((l, k) -> TestUtil.Reverse(l));

        // Create a new individual representing the route 1/2/3/...25/26/1
        List<Pearl> route = new ArrayList<>();
        for (int i = 1; i<=_NUM_CITIES; i++) {
            route.add(_cityDomain.findCity(String.valueOf(i)));
        }

        // Create a space that will contain the new individual and any constructed copes:
        TSPConcreteSpace world = new TSPConcreteSpace();

        // Create a journey as an individual space within the world space:
        Journey j = new Journey(Optional.of(world));
        Util.AddTTMachines(j, _transcriberDomain, _translatorDomain, _reproducerDomain, _klonerDomain, new ArrayList<>());

        // Create a structure that describes this route:
        Structure s = new Structure(j, route, _cityDomain);
        j.addMachineTemplate(s);

        // Check time of this journey:
        System.out.println(j);
        assertEquals(j.journeyTime(), 1140.0);

        // Replicate all the journeys in the world. meaning there should be two:
        world.replicate();
        assertEquals(world.numSubspaces(), 2);

        // Check that the new journey is the Reverse of the original:
        Journey copiedJourney = world.getAnotherSpace(j);
        assertFalse(j == copiedJourney);

        List<Pearl> newRoute = copiedJourney.locateMachine(TSPCalculator.class).getCode();
        assertEquals(route, TestUtil.Reverse(newRoute));

        // But it should still be the same time as the journey has just been reversed:
        assertEquals(copiedJourney.journeyTime(), j.journeyTime());
    }

    @Test (priority = 10)
    public void simpleIterationOfComplexRoute() throws Exception {
        _cityDomain.setMutator((l, k) -> TestUtil.OptN(l, 3));

        // Create a new individual representing the route 1/2/3/...25/26/1
        List<Pearl> route = new ArrayList<>();
        for (int i = 1; i<=_NUM_CITIES; i++) {
            route.add(_cityDomain.findCity(String.valueOf(i)));
        }

        // Create a space that will contain the new individual and any constructed copies:
        TSPConcreteSpace world = new TSPConcreteSpace();

        // Make 10 identical journeys:
        for (int i = 0; i<10; i++) {
            makeJourney(route, world, _cityDomain);
        }

        assertEquals(world.numSubspaces(), 10);

        Journey best = generateOneNewJourney(world);

        // Do max 100000 iterations:
        int iteration = 0;
        do {
            best = generateOneNewJourney(world);
            iteration++;
        } while (iteration != 100000 && best.journeyTime() != 937.0);
        System.out.println("Iterations: " + iteration);

        assertEquals(best.journeyTime(), 937.0);
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

    @Test (priority = 4)
    public void paralleliseGenerations() throws Exception {
        _cityDomain.setMutator((l, k) -> TestUtil.OptN(l, 3));

        // Create a single new journey representing the initial route around all cities in the space. Start with
        // a random list
        List<Pearl> route = new ArrayList<>();
        for (int i=1; i<=_NUM_CITIES; i++) {
            route.add(_cityDomain.findCity(String.valueOf(i)));
        }

        // Create the world (a top level world) that will enact the individuals in a parallel manner:
        TSPConcreteSpace world = new TSPConcreteSpace();

        // Add 100 instances of a journey over the already calculated route to the world:
        for (int i =0; i<100; i++) {
            this.makeJourney(route, world, _cityDomain);
            route = TestUtil.OptN(route, 10);
        }

        // Iterate many cycles of calculating all the journey times, chucking away the weakest half and
        // allowing the strongest half to reproduce:
        int iteration = 0;
        Journey j;
        do {
            j = (Journey)world.search().get();
            if (iteration % 10 == 0)
                System.out.println(iteration + ": " + j);
            iteration++;
        } while (iteration != 1000000 && j.journeyTime() != 937.0);
        System.out.println(iteration + ": " + j);
    }
}