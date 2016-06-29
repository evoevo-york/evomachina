package EvoEvo.york.tspTest;

import EvoEvo.york.machineMetaModel.Domain;
import EvoEvo.york.machineMetaModel.Machine;
import EvoEvo.york.machineMetaModel.MetaModelException;
import EvoEvo.york.machineMetaModel.Space;
import EvoEvo.york.machineMetaModel.Structure;
import EvoEvo.york.machineMetaModel.Pearl;

import java.util.List;
import java.util.Optional;

/** A machine that knows how to calculate travelling salesman distances. The code for the machine (it's genome, that is) is actually a route
 *  through a collection of cities in a domain that represents all the cities in the model. */
public class TSPCalculator extends Machine {
    protected Optional<Double> _journeyTime;

    public TSPCalculator(Space environment, List<Pearl> code, Domain domain) throws MetaModelException {
        super(environment, code, domain);
        assert(CityType.class.isAssignableFrom(domain.getClass()));
        _journeyTime = Optional.empty();
    }

    /** In the context of this particular machine, doIt is overridden to calculate the journey time around the defined list of cities. */
    @Override
    public Structure doIt() {
        if (_code.size() == 0 || _code.size() == 1) throw new RuntimeException("Degenerate route in journey");
        double result = 0;
        City start = (City)_code.get(0);
        CityType type = (CityType)start.getDomain();
        City last = start;
        for (int i = 1; i < _code.size(); i++) {
            City next = (City)_code.get(i);
            result += type.journeyTime(last, next);
            last = next;
        }
        result += type.journeyTime(last, start);
        _journeyTime = Optional.of(result);
        return this;
    }

    public Double getJourneyTime() {
        if (!_journeyTime.isPresent()) this.doIt();
        return _journeyTime.get();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(_code.size());
        Pearl first = _code.stream().findFirst().get();
        _code.stream().forEach((u) -> b.append(u.toString() + ","));
        b.append(first);
        return b.toString();
    }
}
