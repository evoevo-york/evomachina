package EvoEvo.york.tspTest;

import EvoEvo.york.machineMetaModel.Domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** A special sort of Domain that has symbols which are cities around which a TSP is going to be travelling. */
public class CityType extends Domain {
    /** The range of this map is the set of Cities that are members of this domain. The keys of the map are the names of the cities. */
    private Map<String, City> _cities = new HashMap<>();

    /** Local class that remembers a pair of cities so that the distance between them can be retained */
    class CityPair {
        City _a;
        City _b;

        CityPair(City a, City b) {
            assert !a.equals(b);
            _a = a;
            _b = b;
        }

        /** Answer true if the receiving pair connects to the supplied city */
        private boolean connectsTo(City target) {
            return target.getName().equals(_a.getName()) || target.getName().equals(_b.getName());
        }

        /** One city pair equals another if it connects two cities with the same name.
         *  The cities might actually be different objects, albeit with the same name. */
        @Override
        public boolean equals(Object obj) {
            return CityPair.class.isAssignableFrom(obj.getClass()) &&
                           ((CityPair)obj).connectsTo(_a) && ((CityPair)obj).connectsTo(_b);
        }
    }

    /** The distances between pairs of cities */
    private Map<CityPair, Double> _distances;

    /** Construct the domain instance and set up the list of allowed cities. This domain just knows the type of machine that it codes for:
     *  the TSPCalculator machine. */
    public CityType(String name) {
        super(name, TSPCalculator.class);

        _distances = new HashMap<>();
    }

    /** Add a city to this domain */
    public City addCity(String name) {
        City result = new City(name, this);
        _cities.put(name, result);
        return result;
    }

    /** Add an inter-city distance */
    public void addCityDistance(City from, City to, double distance) {
        CityPair pair = new CityPair(from, to);
        Optional<CityPair> found = _distances.keySet()
                                             .stream()
                                             .filter(cp -> cp.equals(pair))
                                             .findFirst();
        if (!found.isPresent())
            _distances.put(pair, distance);
    }

    /** Answer the time taken to transition between the two given cities */
    public double journeyTime(City from, City to) {
        CityPair p = new CityPair(from, to);
        return _distances
                       .entrySet()
                       .stream()
                       .filter(e -> e.getKey().equals(p))
                       .findFirst()
                       .map(e -> e.getValue())
                       .get();
    }

    /** Find a specific named city, used for testing
     *  todo: work out how to elide this for a non-testing build */
    public City findCity(String name) {
        return _cities.get(name);
    }

}
