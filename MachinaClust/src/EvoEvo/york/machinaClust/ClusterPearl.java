package EvoEvo.york.machinaClust;

import EvoEvo.york.machineMetaModel.Domain;
import EvoEvo.york.machineMetaModel.Pearl;
import EvoEvo.york.machineMetaModel.Simulation;

import java.util.Random;

/** A Pearl for the MachinaClust implementation. It provides, in addition to the coding/non-coding notion inherited from the super class notions of which
 *  cluster and along which dimension a contribution is made. */
public class ClusterPearl extends Pearl {
    /** An integer denoting the core point to which this Pearl contributes.
     *  todo: think about whether Cluster should actually be a class? */
    protected int _corePoint;

    /** The number of the dimension to which this pearl contributes
     *  todo: think about whether Dimension should actually be a class */
    protected int _dimension;

    /** The normalised value of the contribution to the coordinate of the core point of this pearl*/
    protected double _value;

    /** The maximum number of dimensions: */
    protected static int _MaxDimension = Simulation.GetValue("maxDimension", 3);

    public ClusterPearl(Domain domain) {
        super(domain);
        _corePoint = 0;
        _dimension = 0;
        _value = 0.0;
    }

    /** Constructor used for testing */
    protected ClusterPearl(Domain domain, int value) {
        this(domain);
        this._coding = (value % 2) == 0;
        this._corePoint = value;
        this._dimension = value;
        this._value = value;
    }

    /** Answer a new Pearl formed from the merging of the two supplied parameters, according to the rules in Sergio's paper. */
    protected static ClusterPearl MergePearls(ClusterPearl p1, ClusterPearl p2, int splitPoint) {
        ClusterPearl result = (ClusterPearl)p1.klone();
        switch (splitPoint) {
            case 0: result._value = p2._value;
            case 1: result._dimension = p2._dimension;
            case 2: result._corePoint = p2._corePoint;
        }

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClusterPearl)) return false;

        ClusterPearl that = (ClusterPearl)o;

        if (_coding != that._coding) return false;
        if (_corePoint != that._corePoint) return false;
        if (_dimension != that._dimension) return false;
        return Double.compare(that._value, _value) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = _corePoint;
        result = 31 * result + _dimension;
        temp = Double.doubleToLongBits(_value);
        result = 31 * result + (int)(temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return String.format("(%d,%d,%1.1f)", _corePoint, _dimension, _value);
    }

    /** Point mutate the receiver. This just varies the four basic components (coding, core point, dimension and value)
     *  within their possible sets */
    public void pointMutate(Random r, int numCorepoints, int numDimensions, double valueRange, double minValue) {
        switch (r.nextInt(3)) {
            case 0: // Mutate the coding:
                    _coding = r.nextBoolean();
                    break;
            case 1: // Mutate the core point:
                _corePoint = r.nextInt(numCorepoints);
                    break;
            case 2: // Mutate the dimension:
                    _dimension = r.nextInt(numDimensions);
                    break;
            case 3: // Mutate the value:
                _value = (r.nextDouble() * valueRange) + minValue;
                    break;
        }
    }

    /** Construct a random instance of the receiver with 5 dimensions, 5 corepoints and values in the range -1 .. 1 */
    public static Pearl MakeRandom(Domain clustererDomain, Random r, int maxCorepoint, int maxDimension, double valRange, double minValue) {
        ClusterPearl result = new ClusterPearl(clustererDomain);

        result._coding = r.nextInt(2) == 0;
        result._dimension = r.nextInt(maxDimension);
        result._corePoint = r.nextInt(maxCorepoint);
        result._value = (r.nextDouble() * valRange) + minValue;

        return result;
    }

    public int getCorePoint() {
        return _corePoint;
    }

    public int getDimension() {
        return _dimension;
    }

    public double getValue() {
        return _value;
    }
}
