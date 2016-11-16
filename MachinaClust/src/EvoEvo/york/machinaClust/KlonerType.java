package EvoEvo.york.machinaClust;

import EvoEvo.york.machineMetaModel.Domain;
import EvoEvo.york.machineMetaModel.Individual;
import EvoEvo.york.machineMetaModel.Kloner;
import EvoEvo.york.machineMetaModel.Machine;
import EvoEvo.york.machineMetaModel.MetaModelException;
import EvoEvo.york.machineMetaModel.Pearl;
import EvoEvo.york.machineMetaModel.Simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import static java.lang.Math.max;
import static java.lang.Math.min;

/** The domain of the Kloner used to klone MachinaClust repositories. */
public class KlonerType extends Domain {
    public KlonerType(String name, Class<? extends Machine> machineType) {
        super(name, machineType, (l, k) -> mutateKlonerCoding(l, k));
    }

    /**  Mutate the mutation rates encoded in the list of Kloner pearls. These are used in order for the four different mutation rates. */
    private static List<Pearl> mutateKlonerCoding(List<Pearl> l, Kloner k) {
        List<Pearl> result = new ArrayList<>();
        Random r = ThreadLocalRandom.current();
        double mutationModifier = Simulation.GetValue("mutationAmount", 0.01);
        for (Pearl p : l) {
            KlonerPearl kp = (KlonerPearl)p;
            double currentMutationRate = kp.getMutationRate();
            if (r.nextDouble() > currentMutationRate &&
                r.nextDouble() > 0.25) {
                // No mutation occurs if the mutation rate is not exceeded and a constant 1/4 is not exceeded
                result.add(p);
            } else {
                double newRate = max(0,currentMutationRate  + (r.nextGaussian() * mutationModifier));
                newRate = min(1, newRate);
                KlonerPearl kpm = new KlonerPearl(kp.getDomain(), newRate); //currentMutationRate  + (r.nextGaussian() * mutationModifier)); // * ((r.nextDouble() * mutationModifier) + (1 - mutationModifier / 2)));
                result.add(kpm);
            }
        }
        return result;
    }

    /** Answer the deletion mutation rate for this machina clust domain. This is the mutation rate in the first member of the provided Kloner machine's genome that
     *  is coding */
    public double getDeletionMutationRate(Kloner kloner) {
        return this.readRate(kloner, 0, () -> new MetaModelException("No deletion rate coding pearls in kloner code"));
    }

    private double readRate(Kloner kloner, int skipCount, Supplier<MetaModelException> metaModelExceptionSupplier) {
        List<Pearl> code = kloner.getCode();
        KlonerPearl deletionPearl = (KlonerPearl)code.stream()
                                                     .filter(p -> p.isCoding())
                                                     .skip(skipCount)
                                                     .findFirst()
                                                     .orElseThrow(metaModelExceptionSupplier);
        return deletionPearl.getMutationRate();
    }

    /** Answer the duplication mutation rate for this machina clust domain. This is the mutation rate in the second member of the provided Kloner machine's genome that
     *  is coding */
    public double getDuplicationMutationRate(Kloner kloner) {
        return this.readRate(kloner, 1, () -> new MetaModelException("No duplication rate coding pearls in kloner code"));
    }

    /** Answer the translocation mutation rate for this machina clust domain. This is the mutation rate in the fourth member of the provided Kloner machine's genome that
     *  is coding */
    public double getTranslocationMutationRate(Kloner kloner) {
        return this.readRate(kloner, 2, () -> new MetaModelException("No translocation rate coding pearls in kloner code"));
    }

    public double getPointMutationRate(Kloner kloner) {
        return this.readRate(kloner, 3, () -> new MetaModelException("No point rate coding pearls in kloner code"));
    }
}
