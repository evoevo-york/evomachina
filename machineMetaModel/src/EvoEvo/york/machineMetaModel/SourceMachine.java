package EvoEvo.york.machineMetaModel;

import java.util.List;
import java.util.Optional;

/** Abstract class that expresses the notion of a machine that has a single structure that is its source for the behaviour that it represents. */
public abstract class SourceMachine extends Machine {
    /** The source structure for the machine's actions */
    Optional<Structure> _source;

    public SourceMachine(Space environment, List<Pearl> code, Domain domain) throws MetaModelException {
        super(environment, code, domain);
        _source = Optional.empty();
    }

    public void setSource(Structure source) {
        this._source = Optional.of(source);
    }

    protected void ensureSource() {
        if (!_source.isPresent()) throw new MetaModelException("No source for machine of type: " + this.getClass().getName());
    }
}
