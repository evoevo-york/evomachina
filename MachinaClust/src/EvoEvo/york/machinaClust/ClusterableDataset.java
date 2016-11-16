package EvoEvo.york.machinaClust;

import EvoEvo.york.machineMetaModel.Dataset;
import EvoEvo.york.machineMetaModel.MetaModelException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/** A class representing a set of data to be clustered. This is a set of observations in an n-dimensional space which are to be clustered around a set of points, each of which
 *  exists in a subspace of that space. */
public class ClusterableDataset implements Dataset, Iterable<Observation> {
    /** The actual data is all stored in a list */
    protected List<Observation> _observations = new ArrayList<>();

    protected Optional<Double> _maxValue = Optional.empty();
    protected Optional<Double> _minValue = Optional.empty();

    /** The dimensionality of the space containing these observations */
    protected Optional<Integer> _numDimensions = Optional.empty();

    public ClusterableDataset() {
    }

    public ClusterableDataset(int numDimensions) {
        _numDimensions = Optional.of(numDimensions);
    }

    /** Add a single observation to the receiver */
    public void add(Observation o) {
        _observations.add(o);
    }

    @Override
    public Iterator<Observation> iterator() {
        return _observations.iterator();
    }

    public int getNumObservations() {
        return _observations.size();
    }

    @Override
    public int getNumDimensions() {
        return _numDimensions.get();
    }

    @Override
    public double valueRange() {
        if (!_minValue.isPresent()) this.calcValues();
        return _maxValue.get() - _minValue.get();
    }

    /** The receiver is empty, in fact make sure it is. Then load it by creating a single observation from each line in the supplied file and
     *  adding that to the receiver */
    void loadFrom(String dataFileName) throws IOException {
        _observations.clear();
        try (Stream<String> stream = Files.lines(Paths.get(dataFileName))) {
            stream.forEach(s -> this.createObservation(s));
        }
    }

    /** Take a single line from the input and create an observation by parsing that line. Add that observation to the receiver */
    private void createObservation(String s) {
        String[] parts = s.split(",");
        if (parts.length != this.getNumDimensions()) throw new MetaModelException("incorrect number of dimensions in data");
        Double[] values = new Double[parts.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = Double.valueOf(parts[i]);
        }
        Observation o = new Observation(values, this);
        this.add(o);
    }

    /** Calculate the minimum and range of value2, along al dimensions, of all observations */
    private void calcValues() {
        _observations.forEach(o -> this.summariseObservation(o));
    }

    private void summariseObservation(Observation o) {
        o.stream().forEach(d -> this.summariseValue(d));
    }

    private void summariseValue(Double v) {
        if (!_minValue.isPresent() || _minValue.get() > v) _minValue = Optional.of(v);
        if (!_maxValue.isPresent() || _maxValue.get() < v) _maxValue = Optional.of(v);
    }

    @Override
    public double minValue() {
        if (!_minValue.isPresent()) this.calcValues();
        return _minValue.get();
    }
}
