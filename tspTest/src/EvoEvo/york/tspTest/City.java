package EvoEvo.york.tspTest;

import EvoEvo.york.machineMetaModel.Domain;
import EvoEvo.york.machineMetaModel.Pearl;

/** An example Pearl. Each member of the class City is one of the places around which a TSP route will be taken. A city just has a name as it's the domain
 *  that, correctly, knows how far apart the cities are. */
public class City extends Pearl {
    /** The name of the city */
    private String _name;

    /** Construct a new city defining its name and the domain it shares with other cities */
    public City(String name, Domain domain) {
        super(domain);
        _name = name;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        City result = (City)super.clone();
        result._name = _name;
        return result;
    }

    @Override
    /** Two cities are the same if their names are the same, nothing else matters. */
    public boolean equals(Object obj) {
        return this.getClass().isAssignableFrom(obj.getClass()) && _name.equals(((City)obj)._name);
    }

    public String getName() {
        return _name;
    }

    @Override
    public String toString() {
        return _name;
    }
}
