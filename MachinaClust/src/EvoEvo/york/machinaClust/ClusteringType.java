package EvoEvo.york.machinaClust;

import EvoEvo.york.machineMetaModel.*;
import org.apache.commons.math3.distribution.BinomialDistribution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Math.abs;

/** The type of the genome pearls that calculate the clustering of large collections of input data */
public class ClusteringType extends Domain {
    static Logger _Logger = Logger.getLogger("EvoEvo");

    private int _numCorepoints;
    private final double _minValue;
    private final double _valueRange;
    private Dataset _dataset;

    /** Local record of length change as a result of mutation. Used for testing only. */
    private int _lengthChange;

    public ClusteringType(String name, Dataset dataset, int numCorepoints, double minValue, double valueRange) {
        super(name, ClusterCalculator.class);
        _numCorepoints = numCorepoints;
        _minValue = minValue;
        _valueRange = valueRange;
        _dataset = dataset;
    }

    @Override
    /** Mutate the provided structure according to the rules for all clusterer genomes. */
    public Structure mutate(Structure initial, Kloner kloner) {
        StringBuilder logString = new StringBuilder("Mutations: ");
        _lengthChange = 0;

        // Select random set of rearrangement operators:
        List<Rearrangement> rearrangements = new ArrayList<>();
        double deletionMutationRate = ((KlonerType)kloner.getDomain()).getDeletionMutationRate(kloner);
        addANumberOfRearrangments(initial, deletionMutationRate, rearrangements, l -> this.largeDeletion(l, logString));
        double duplicationMutationRate = ((KlonerType)kloner.getDomain()).getDuplicationMutationRate(kloner);
        addANumberOfRearrangments(initial, duplicationMutationRate, rearrangements, l -> this.largeDuplication(l, logString));
        double translocationMutationRate = ((KlonerType)kloner.getDomain()).getTranslocationMutationRate(kloner);
        addANumberOfRearrangments(initial, translocationMutationRate, rearrangements, l -> this.largeTranslocation(l, logString));

        // Shuffle the set of rearrangements so thay're applied in any old order:
        Collections.shuffle(rearrangements);


        // Apply rearrangments:
        List<Pearl> mutatedCode = initial.getCode();
        for (Rearrangement r : rearrangements) {
            mutatedCode = r.rearrange(mutatedCode);
        }

        // Do point mutations:
        double pointMutationRate = ((KlonerType)kloner.getDomain()).getPointMutationRate(kloner);
        BinomialDistribution bd = new BinomialDistribution(mutatedCode.size(), pointMutationRate*2);
        int numPointMutations = bd.sample();
        for (int i = 0; i < numPointMutations; i++) {
            this.pointMutation(mutatedCode, logString);
        }

        if (_Logger.isLoggable(Level.FINER)) {
            _Logger.finer(logString.toString());
        }

        return new Structure(initial.getEnvironment(), mutatedCode, this);
    }

    /** Apply a point mutation to the supplied code, which has already been rearranged in several posible ways. */
    private void pointMutation(List<Pearl> code, StringBuilder logString) {
        // Choose a particular pearl to mutate:
        ThreadLocalRandom r = ThreadLocalRandom.current();
        ClusterPearl p = (ClusterPearl)code.get(r.nextInt(code.size()));
        if (_Logger.isLoggable(Level.FINER)) logString.append("pt,");
        p.pointMutate(r, _numCorepoints, _dataset.getNumDimensions(), _valueRange, _minValue);
    }

    private void addANumberOfRearrangments(Structure initial, double mutationRate, List<Rearrangement> rearrangements, Rearrangement operator) {
        BinomialDistribution bd = new BinomialDistribution(initial.size(), mutationRate);
        int numRearrangements = bd.sample();
        for (int i = 0; i < numRearrangements ; i++) {
            rearrangements.add(operator);
        }
    }

    /** Carry out a large deletion restructuring of the supplied clusterer genome. The code here assumes that as the
     *  genome is circular it doesn't matter if the relative position is changed around the loop as well as the deletion
     *  mutation.
     *
     *  Note that there's a practical lower limit to the size of a genome that can be subjected to a large deletion and this code
     *  requires that the provided list is at least 20 units long. Any shorter than that and it is returned unmodified. */
    protected List<Pearl> largeDeletion(List<Pearl> l, StringBuilder logString) {
        List<Pearl> result = new ArrayList<>();

        if (l.size() < 20) {
            result = new ArrayList<>(l);
            return result;
        }

        // Calculate where in the list we're going to split things. Note that i and j here have
        // the meaning that they do in Sergio's paper:
        ThreadLocalRandom r = ThreadLocalRandom.current();
        int i = r.nextInt(l.size());
        int j, cutSize;
        do {
            j = r.nextInt(l.size());
            cutSize = this.cutSize(i,j,l);
        } while (3 >=  cutSize ||
                 cutSize >= (l.size() - 3) ||
                 (l.size() - cutSize) < 10);

        _lengthChange -= cutSize;

//        System.out.println(String.format("Deleting %d to %d from %d", i, j, l.size()));

        deleteChunk(l, result, i, j, cutSize);
        if (_Logger.isLoggable(Level.FINER)) logString.append("del,");

        if (result.size() == 0) {
            System.out.println("No genome");
        }

        return result;
    }

    private void deleteChunk(List<Pearl> source, List<Pearl> result, int deleteFrom, int deleteTo, int cutSize) {
        // Pick point around the "other" side of  the genome from which to copy the retained parts of the genome:
        int offset = ((source.size() - cutSize) / 2);
        int start;
        if (deleteFrom < deleteTo) {
            start = deleteTo + offset;
        } else {
            start = deleteFrom - offset;
        }

        start = this.normalise(start, source.size());

        // Copy from the start around to the first cut point:
        this.copyChunk(source, result, start, deleteFrom);

        // Add the merge of the pearls at points i and j:
        int splitPoint = ThreadLocalRandom.current().nextInt(4);
        result.add(ClusterPearl.MergePearls((ClusterPearl)source.get(deleteFrom), (ClusterPearl)source.get(deleteTo), splitPoint));

        // Copy the chunk from the second cut point around back to the start:
        this.copyChunk(source, result, this.normalise(deleteTo + 1, source.size()), start);
    }

    /** Calculate the amount of l that will be snipped by cutting from i to j */
    private int cutSize(int start, int finish, List<Pearl> l) {
        // todo there must be a better way of doing this:
        int result = 0;
        int pos = start;
        do {
            result++;
            pos = this.normalise(++pos, l.size());
        } while (pos != finish);
        return result;
    }
     /** Answer the supplied position, normalised to the size of genome, as supplued */
    private int normalise(int position, int size) {
        return (position < 0 ? position + size : position) % size;
    }

    /** Carry out a large duplication mutation of the supplied clusterer genome */
    protected List<Pearl> largeDuplication(List<Pearl> l, StringBuilder logString) {
        List<Pearl> result = new ArrayList<>();

        // Calculate where in the list we're going to copy a chunk from:
        int[] positions = this.calculateDuplicationPositions(l);
        int i = positions[0];
        int j = positions[1];
        int cutSize = positions[2];
        int insertPos = positions[3];

        _lengthChange += cutSize;

//        System.out.println(String.format("Duplicating %d to %d at %d", i, j, insertPos));

        // Now duplicate the chunk into result and return that:
        duplicateChunk(l, result, i, j, insertPos);

        if (result.size() == 0) {
            System.out.println("No genome");
        }

        if (_Logger.isLoggable(Level.FINER)) logString.append("dup,");

        return result;
    }

    private int[] calculateDuplicationPositions(List<Pearl> source) {
        // Calculate where in the list we're going to copy a chunk from:
        ThreadLocalRandom r = ThreadLocalRandom.current();
        int i = r.nextInt(source.size());
        int j, cutSize, insertPos;

        do {
            j = r.nextInt(source.size());
            cutSize = this.cutSize(i,j,source);
//            System.out.println(String.format("i: %d; j: %d; cutsize: %d", i, j, cutSize));
        } while (3 >=  cutSize || cutSize >= (source.size() - 3));

        // Generate a position in which we're going to insert the copied chunk:
        do {
            insertPos = r.nextInt(source.size());
//            System.out.println(String.format("i: %d; j: %d; cutsize: %d, insertPos: %d", i, j, cutSize, insertPos));

        } while ((2 >=  insertPos || insertPos >= (source.size() - 2)));

//        System.out.println(String.format("i: %d; j: %d; cutSize: %d; insert: %d", i, j, cutSize, insertPos));
        return new int[] { i, j, cutSize, insertPos };
    }

    private void duplicateChunk(List<Pearl> source, List<Pearl> result, int chunkStart, int chunkEnd, int insertPos) {
        // Copy from the start of the genome up to, but not including, the insert position to the output:
        this.copyChunk(source, result, 0, insertPos);

        // Add the merge of the insert position's pearl with the first of the copied part:
        int splitPoint = ThreadLocalRandom.current().nextInt(4);
        result.add(ClusterPearl.MergePearls((ClusterPearl)source.get(insertPos), (ClusterPearl)source.get(chunkStart), splitPoint));

        // Insert the rest of the duplicated chunk:
        this.copyChunk(source, result, this.normalise(chunkStart + 1, source.size()), this.normalise(chunkEnd, source.size()));

        // Add the merge of the last item in the duplicated chunkj with the item at the insert position:
        result.add(ClusterPearl.MergePearls((ClusterPearl)source.get(this.normalise(chunkEnd, source.size())), (ClusterPearl)source.get(insertPos), splitPoint));

        // Add the rest of the genome:
        this.copyChunk(source, result, this.normalise(insertPos + 1, source.size()), 0);
    }

    private void copyChunk(List<Pearl> source, List<Pearl> destination, int start, int finish) {
        int position = start;
        do {
            destination.add(source.get(position).klone());
            position = this.normalise(++position, source.size());;
        } while ( position != finish );
    }

    /** Carry out a large translocation restructuring of the supplied clusterer genome */
    protected List<Pearl> largeTranslocation(List<Pearl> l, StringBuilder logString) {
        List<Pearl> intermediateResult = new ArrayList<>();
        List<Pearl> result = new ArrayList<>();

        // Calculate where in the list we're going to copy a chunk from:
        int[] positions;
        int i, j, cutSize, insertPos;
        do {
            positions = this.calculateDuplicationPositions(l);
            i = positions[0];
            j = positions[1];
            cutSize = positions[2];
            insertPos = positions[3];
        } while (this.duplicationOverlaps(l.size(), i, j, insertPos));

//        System.out.println(String.format("Translocating %d to %d at %d", i, j, insertPos));

        // Now duplicate the chunk into the intermediate result:
        duplicateChunk(l, intermediateResult, i, j, insertPos);

        // Delete the original part:
        this.deleteChunk(intermediateResult, result, (i<j ? i : i+cutSize), j, cutSize);

        if (result.size() == 0) {
            System.out.println("No genome");
        }

        if (_Logger.isLoggable(Level.FINER)) logString.append("trans,");

        return result;
    }

    /** Answer true if a duplication of the segment between start and end at position insert will overlap
     *  the original duplicated segment. */
    private boolean duplicationOverlaps(int size, int start, int end, int insertPos) {
        if (start < end) {
            // Simple case where no overlap of join:
            return insertPos <= end;
        } else if (start > end) {
            // the duplicated portion wraps around the end of the genome:
            return (start <= insertPos || insertPos < end);
        }
        return true;
    }

    public int getLengthChange() {
        return _lengthChange;
    }

    public int getNumCorepoints() {
        return _numCorepoints;
    }

    public void setNumCorepoints(int numCorepoints) {
        _numCorepoints = numCorepoints;
    }
}

@FunctionalInterface
interface Rearrangement {
    List<Pearl> rearrange(List<Pearl> original);
}
