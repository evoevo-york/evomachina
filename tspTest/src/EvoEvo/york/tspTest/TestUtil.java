package EvoEvo.york.tspTest;

import EvoEvo.york.machineMetaModel.Domain;
import EvoEvo.york.machineMetaModel.Individual;
import EvoEvo.york.machineMetaModel.Kloner;
import EvoEvo.york.machineMetaModel.Pearl;
import EvoEvo.york.machineMetaModel.Simulation;
import EvoEvo.york.machineMetaModel.Space;
import EvoEvo.york.machineMetaModel.Structure;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/** Collection of useful testing utilities */
public class TestUtil {
    /** return the Reverse of the provided list */
    static List<Pearl> Reverse(List<Pearl> l) {
        List<Pearl> result = new ArrayList<>(l);
        Collections.reverse(result);
        return result;
    }

    /** Apply an n-opt modification of the input list into the output list where n is the number of places where
     *  the input list is cut. The resulting list is then formed from the n-1 fragments plus the two end regions. Any
     *  of these fragments can be of length 0.
     *
     *  It is possible for this method to leave the route unchanged. This is not checked for. */
    static List<Pearl> OptN(List<Pearl> l, int n) {
        ThreadLocalRandom r = ThreadLocalRandom.current();

        // Generate snipping points in the range 0..l.size() where one point may be the same as the previous, but no lower
        int[] positions = new int[n];
        for (int i=0; i<n; i++)
            positions[i] = r.nextInt(i==0?1:positions[i-1],l.size()+1);

        List<Pearl> result = new ArrayList<>(l.size());

        // Copy initial fragment, note that the first element, the start city, is always the same:
        result.add(l.get(0));
        CopyFragmentForwards(result, l, 1, positions[0]-1);

        // Copy snipped fragments:
        Set<Integer> copiedFragments = new HashSet<>();
        while(copiedFragments.size() < n-1) {
            // Choose a fragment that is as yet uncopied:
            int fragment;
            do {
                fragment = r.nextInt(n-1);
            } while (copiedFragments.contains(fragment));
            copiedFragments.add(fragment);

            // Copy fragment to output:
            CopyFragment(result, l, positions[fragment], positions[fragment+1]-1, r);
        }

        // Copy terminal section, if any:
        CopyFragmentForwards(result, l, positions[n-1], l.size()-1);

        return result;
    }

    private static void CopyFragment(List<Pearl> output, List<Pearl> input, int start, int end, ThreadLocalRandom r) {
        // Randomly choose direction of copy:
        if (r.nextBoolean()) {
            // Forwards
            CopyFragmentForwards(output, input, start, end);
        } else {
            // Backwards
            CopyFragmentBackwards(output, input, start, end);
        }
    }

    private static void CopyFragmentBackwards(List<Pearl> output, List<Pearl> input, int start, int end) {
        for (int i = end; i >= start; i--) output.add(input.get(i));
    }

    private static void CopyFragmentForwards(List<Pearl> output, List<Pearl> input, int start, int end) {
        for (int i=start; i <= end; i++) output.add(input.get(i));
    }

    public static void AddTTMachines(Individual container, Domain transcriberDomain, Domain translatorDomain, Domain reproducerDomain, Domain klonerDomain, List<Pearl> klonerSequence) {
        Structure transcriberTemplate = new Structure(container, new ArrayList<>(), transcriberDomain);
        container.addMachineTemplate(transcriberTemplate);
        container.addMachine(transcriberDomain.constructMachine(container, new ArrayList<>()));

        Structure translatorTemplate = new Structure(container, new ArrayList<>(), translatorDomain);
        container.addMachineTemplate(translatorTemplate);
        container.addMachine(translatorDomain.constructMachine(container, new ArrayList<>()));

        Structure reproducerTemplate = new Structure(container, new ArrayList<>(), reproducerDomain);
        container.addMachineTemplate(reproducerTemplate);

        Structure klonerTemplate = new Structure(container, klonerSequence, klonerDomain);
        container.addMachineTemplate(klonerTemplate);
    }


    /** Create a single route that visits all cities in the domain by constructing a sequential route and using a 10-opt algorithm to
     *  shuffle it about. */
    static List<Pearl> CreateInitialRandomRoute(CityType cityDomain, int numCities) {
        List<Pearl> result = new ArrayList<>();
        for (int i=2; i<=numCities; i++) {
            result.add(cityDomain.findCity(String.valueOf(i)));
        }
        Collections.shuffle(result);
        result.add(0, cityDomain.findCity("1"));
        return result;
    }

    /** Create a new journey at a defined point in the supplied toroidal space */
    static void MakeJourneyInToroidalSpace(ToroidalTSP2DSpace world, int xPos, int yPos, int numCities,
                                           CityType cityDomain, Domain transcriberDomain, Domain translatorDomain, Domain reproducerDomain) {
        KlonerDomain kDomain = new KlonerDomain("Kloner domain", Kloner.class);
        List<Pearl> route = CreateInitialRandomRoute(cityDomain, numCities);
        Space js = world.getSubspace(xPos, yPos);
        Journey j = MakeJourneyWithMutatingCopier(route, js, cityDomain, kDomain, transcriberDomain, translatorDomain, reproducerDomain);
    }

    static Journey MakeJourneyWithMutatingCopier(List<Pearl> route, Space world,
                                                 Domain tspDomain, Domain klonerDomain, Domain transcriberDomain, Domain translatorDomain, Domain reproducerDomain) {
        Journey j = new Journey(Optional.of(world));
        List<Pearl> klonerCode = new ArrayList<>();

        int mink = Simulation.GetValue("minKOpt", 2);
        int maxk = Simulation.GetValue("maxKOpt", 10);
        int initialk = Simulation.GetValue("initialKOpt", 10);
        if (mink >= maxk
            || initialk < mink
            || initialk > maxk) throw new TSPTestException("Invalid values of k");

        for (int i = mink; i <= maxk; i++) {
            klonerCode.add(new KlonerPearl(klonerDomain, i, i == initialk));
        }
        AddTTMachines(j, transcriberDomain, translatorDomain, reproducerDomain, klonerDomain, klonerCode);

        j.addMachineTemplate(new Structure(j, route, tspDomain));

        return j;
    }

    static void LoadProperties(String propertiesFileName) {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(propertiesFileName));
            Simulation.SetProperties(properties);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }
    }
}
