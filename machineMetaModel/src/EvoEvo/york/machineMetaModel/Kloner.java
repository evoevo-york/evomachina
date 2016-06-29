package EvoEvo.york.machineMetaModel;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** A machine that produces an inaccurate copy of a machine template repository */
public class Kloner extends Machine {
    /** The repository that results from the Kloner's activity */
    Optional<Set<Structure>> _newRepository = Optional.empty();

    public Kloner(Space environment, List<Pearl> code, Domain domain) throws MetaModelException {
        super(environment, code, domain);
    }

    /** Enact the Kloner. In this case the machine takes the machine template repository of the environment and creates
     *  a potentially inaccurate copy of it. Each structure in the repository is (inaccurately) copied according to the
     *  rules established by that structure's domain.
     *
     *  The resulting machine template repository (a set of structures) is available from the machine once it has finished the computation.
     *
     *  @return this, although  that won't be very interesting. */
    @Override
    public Structure doIt() {
        Set<Structure> repository = ((Individual)_environment).getRepository();
        Set<Structure> newRepository = new HashSet<>(repository.size());
        repository.stream()
                  .forEach(s -> newRepository.add(s.getDomain().mutate(s, this)));

        _newRepository = Optional.of(newRepository);
        return this;
    }

    public Set<Structure> getNewRepository() {
        this.doIt();
        return _newRepository.get();
    }

    @Override
    public String toString() {
        return _domain.description(this);
    }
}
