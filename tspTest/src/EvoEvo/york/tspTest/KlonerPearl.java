package EvoEvo.york.tspTest;

import EvoEvo.york.machineMetaModel.Domain;
import EvoEvo.york.machineMetaModel.Pearl;

/** A unit of the domain used for the Kloner machine  */
class KlonerPearl extends Pearl {
    protected int _degree;

    public KlonerPearl(Domain domain, int degree) {
        super(domain);
        _degree = degree;
    }

    public KlonerPearl(Domain klonerDomain, int degree, boolean coding) {
        this(klonerDomain, degree);
        _coding = coding;
    }

    public int getDegree() {
        return _degree;
    }
}

