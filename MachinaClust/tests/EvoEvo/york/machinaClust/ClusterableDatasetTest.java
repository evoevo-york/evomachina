package EvoEvo.york.machinaClust;

import org.testng.annotations.Test;

import java.util.Iterator;

import static org.testng.Assert.*;

/** Simple teste for the clusterable dataset classes*/
public class ClusterableDatasetTest {

    @Test
    public void createDatasetAndCheckObservationDimensionalityCorrect() throws Exception {
        ClusterableDataset data = new ClusterableDataset(5);

        data.add(new Observation(new Double[] { 0.0, 1.0, 2.0, 3.0, 4.0}, data));
    }

    @Test (expectedExceptions = java.lang.AssertionError.class)
    public void createDatasetAndCheckObservationDimensionalityInCorrect() throws Exception {
        ClusterableDataset data = new ClusterableDataset(5);

        data.add(new Observation(new Double[] { 0.0, 1.0, 3.0, 4.0}, data));
    }

    @Test
    public void createDatasetAndCheckForeachIterationCorrect() throws Exception {
        ClusterableDataset data = new ClusterableDataset(5);

        double x = 0.0;
        for (int i = 0; i < 10; i++) {
            data.add(new Observation(new Double[]{x++, x++, x++, x++, x++}, data));
        }

        assertEquals(data.getNumObservations(), 10, "Number of observations");
        int count = 0;
        for (Observation observation : data) {
            count++;
            assertEquals(observation.numDimensions(), 5, "Number of dimensions in observation");
        }

        assertEquals(count, 10, "Number of observations found");
    }

    @Test
    public void createDatasetAndCheckOldstyleIterationCorrect() throws Exception {
        ClusterableDataset data = new ClusterableDataset(5);

        double x = 0.0;
        for (int i = 0; i < 10; i++) {
            data.add(new Observation(new Double[]{x++, x++, x++, x++, x++}, data));
        }

        int count = 0;
        for (Iterator<Observation> it = data.iterator(); it.hasNext();) {
            Observation observation = it.next();
            count++;
            assertEquals(observation.numDimensions(), 5, "Number of dimensions in observation");
        }

        assertEquals(count, 10, "Number of observations found");
    }


}