package EvoEvo.york.machinaClust;

import EvoEvo.york.machineMetaModel.Domain;
import EvoEvo.york.machineMetaModel.Pearl;

/** A unit of the domain used for the clustering cloner machine. This just contains the mutation rate */
class KlonerPearl extends Pearl {
    protected double _mutationRate;

    public KlonerPearl(Domain domain, double mutationRate) {
        super(domain);
        _mutationRate = mutationRate;
    }

    public double getMutationRate() {
        return _mutationRate;
    }
}

