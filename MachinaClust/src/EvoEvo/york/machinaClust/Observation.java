package EvoEvo.york.machinaClust;

import EvoEvo.york.machineMetaModel.Dataset;

import java.util.Arrays;
import java.util.stream.Stream;

/** A single observation of data in an n-dimensional space of observations to be clustered. */
public class Observation {
    /** In order of dimensions, the values of this particular observation */
    protected Double[] _values;

    /** Construct the instance using an array of values, ordered by dimension */
    public Observation(Double[] values, Dataset container) {
        assert(container.getNumDimensions() == values.length);
        _values = new Double[values.length];
        System.arraycopy(values, 0, _values, 0, values.length);
    }

    public Stream<Double> stream() {
        return Arrays.stream(_values);
    }

    public double getValue(int dimension) {
        return _values[dimension];
    }

    public int numDimensions() {
        return _values.length;
    }
}
