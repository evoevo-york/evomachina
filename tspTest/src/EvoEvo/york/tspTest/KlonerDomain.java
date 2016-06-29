package EvoEvo.york.tspTest;

import EvoEvo.york.machineMetaModel.Domain;
import EvoEvo.york.machineMetaModel.Kloner;
import EvoEvo.york.machineMetaModel.Machine;
import EvoEvo.york.machineMetaModel.Pearl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.abs;

/**
 * The domain of  the code used for a Kloner machine
 */
class KlonerDomain extends Domain {

    /**
     * Construct a new instance. This initialises the superclass's mutation operator to the local
     * implementation of the  mutateCodings operation.
     */
    public KlonerDomain(String name, Class<? extends Machine> machineType) {
        super(name, machineType, (l, k) -> mutateCodings(l));
    }

    /**
     * Answer the degree of this kloner which is the degree of the first (and possibly only) unit in the supplied kloner's code
     */
    public int getDegree(Kloner kloner) {
        return ((KlonerPearl)kloner.getCode().get(0)).getDegree();
    }

    @Override
    public String description(Machine machine) {
        return String.valueOf(this.getDegree((Kloner)machine));
    }

    /** Method used to mutate the kloner itself. */
    private static List<Pearl> mutateCodings(List<Pearl> l) {
        List<Pearl> result;
        int firstCoding = ((KlonerPearl)(l.stream().filter(p -> p.isCoding()).findFirst().get())).getDegree();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        do {
            result = new ArrayList<>(l.size());
            for (int i = 0; i < l.size(); i++) {
                KlonerPearl u = (KlonerPearl)l.get(i);
                int diff = abs(u.getDegree() - firstCoding);
                KlonerPearl newUnit = (KlonerPearl)u.klone();
                double codingProbability = (diff == 0 ? 0.5 : (diff == 1 ? 0.25 : 0));
                newUnit.setCoding(random.nextDouble() < codingProbability);
                result.add(newUnit);
            }
        } while (result.stream().noneMatch(p -> p.isCoding()));
        return result;
    }


}

