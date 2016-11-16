package EvoEvo.york.machineMetaModel;

/** An interface representing the minimal capabilities of a set of data across which an implementation is crawling. */
public interface Dataset {
    int getNumDimensions();

    int getNumObservations();

    double valueRange();

    double minValue();
}
