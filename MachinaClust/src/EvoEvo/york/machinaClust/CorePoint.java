package EvoEvo.york.machinaClust;

import EvoEvo.york.machineMetaModel.MetaModelException;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.abs;

/** A corepoint represents one of the points in a clusterer's genome with a particular position in a subspace of the
 *  clusterer's total space. It is associated with a set of Observations, each of which has a particular mismatch to the
 *  exact position of the core point. */
public class CorePoint {
    /** The integer id of this core point */
    protected int _id;

    /** The total mismatch of the associated observations */
    protected double _totalMismatch = 0.0;

    /** The number of observations attached to this core point */
    protected int _numObservations = 0;

    /** The coordinates of this core point in a (possibly) subset of the total dimension */
    protected Map<Integer, Double> _coordinates = new HashMap<>();

    public CorePoint(int id) {
        _id = id;
    }

    /** Answer the total mismatch represented by the Observations attached to the receiver. */
    public double totalMismatch() {
        return _totalMismatch;
    }

    public void addContributionToDimension(int dimension, double contribution) {
        Double value = _coordinates.get(dimension);

        if (value == null) {
            value = new Double(contribution);
            _coordinates.put(dimension, value);
        } else {
            _coordinates.put(dimension, new Double(value.doubleValue() + contribution));
        }
    }

    /** Calculate the mismatch between the coordinates of the given observation and the coordinates of the receiver. The coordinates of the
     *  observation are in a number of dimensions that may well be more than those for the receiver. That is, the receiver represents a point in a
     *  subspace of the space within which the observation was taken. */
    public double calculateMismatch(Observation o) {
        double mismatchInSubspace = this.offsetInside(o);
        double mismatchOutsideSubspace = this.offsetOutside(o);

        double mismatch = (mismatchInSubspace + mismatchOutsideSubspace) / o.numDimensions();
        return mismatch;
    }

    /** Answer the manhattan offset outside the receiver's subspace of the given observation. As the observation has been
     *  normalised this is just the coordinates of the observation. */
    private double offsetOutside(Observation o) {
        double result = 0.0;
        for (int i = 0; i < o.numDimensions(); i++) {
            if (!_coordinates.keySet().contains(i)) {
                result += abs(o.getValue(i));
            }
        }
        result *= (o.numDimensions() - this.numDimensions());
        return result;
    }

    /** Answer the manhattan offset within receiver's subspace of the given observation */
    private double offsetInside(Observation o) {
        double result = 0.0;
        for (Integer dim : _coordinates.keySet()) {
            double offset = abs(o.getValue(dim) - _coordinates.get(dim));
            result += offset/this.numDimensions();
        }
        result *= this.numDimensions();
        return result;
    }

    private int numDimensions() {
        return _coordinates.size();
    }

    /** A specific observation has been calculated to be closest to this point in space. Remember the total mismatch for the observations associated with
     *  the receiver. */
    public void add(double mismatch) {
        _totalMismatch += mismatch;
        _numObservations++;
    }

    public int getNumObservations() {
        return _numObservations;
    }

    @Override
    public String toString() {
        StringBuilder r = new StringBuilder();
        int maxDimension = _coordinates.keySet().stream().max(Integer::compareTo).orElseThrow(() -> new MetaModelException("No maximum dimension??"));
        r.append(String.format("{%d,%d}", _id,_numObservations));
        for (int dimension = 0; dimension <= maxDimension; dimension++) {
            if (_coordinates.keySet().contains(dimension)) {
                r.append(String.format("%d:%.2f", dimension, _coordinates.get(dimension)));
                if (dimension+1 <= maxDimension) r.append(", ");
            }
        }
        return r.toString();
    }
}
