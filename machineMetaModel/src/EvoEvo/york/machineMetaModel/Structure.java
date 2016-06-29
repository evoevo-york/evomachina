package EvoEvo.york.machineMetaModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Metamodel class representing the class of all Structures which therefore means all machines as well. */
public class Structure implements Cloneable {
    /** The domain from which this structure's code is drawn */
    protected Domain _domain;

    /** The code of the structure, a sequence of pearls from the domain */
    protected List<Pearl> _code;

    /** The space within which the structure exists. Note that a structure cannot be "environment-less". */
    protected Space _environment;

    /** Override the standard clone() method to clone the receiving structure. However, it's always possible that cloning
     *  a structure will be done inaccurately and the decision about whether is left to the machine that is invoking this method.
     *  @throws CloneNotSupportedException */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Structure result = (Structure)super.clone();
        result._domain = _domain;
        result._code = new ArrayList<>();
        _code.stream().forEach(p -> result._code.add(p.klone()));
        result._environment = _environment;
        return result;
    }

    /** A version of clone() that does not throw a checked exception */
    public Structure klone() {
        try {
            return (Structure)this.clone();
        } catch (CloneNotSupportedException e) {
            throw new MetaModelException("Clone Not Supported");
        }
    }

    /** Construct a new Structure with the provided symbols each of which must be drawn from the supplied domain.
     *  The members of the code list are expected to be clones of the base symbols as they're new instances, not
     *  just references to existing instances. */
    public Structure(Space environment, List<Pearl> code, Domain domain) {
        _domain = domain;
        _environment = environment;
        _code = code;
        assert code != null;
        assert environment != null;
        assert domain != null;
        domain.checkTypeOfAll(code);
    }

    public Domain getDomain() {
        return _domain;
    }

    public List<Pearl> getCode() {
        return _code;
    }

    /** Answer the length of the receiver's code */
    public int size() {
        return _code.size();
    }

    /** Two structures are declared to be equal if they are from the same domain and have code symbols that are equals() in the same
     *  order */
    @Override
    public boolean equals(Object obj) {
        if (!this.getClass().isAssignableFrom(obj.getClass())) return false;

        List<Pearl> objCode = ((Structure)obj).getCode();
        if (_code.size() != objCode.size()) return false;
        for (int i = 0; i < _code.size(); i++) {
            if (!(_code.get(i).equals(objCode.get(i)))) return false;
        }
        return true;
    }

    public Space getEnvironment() {
        return _environment;
    }

    public void setEnvironment(Space environment) {
        _environment = environment;
    }

    public void setCode(List<Pearl> code) {
        _code = code;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(_code.size());
        Optional<Pearl> first = _code.stream().findFirst();
        if (!first.isPresent()) return "";

        _code.stream().forEach((u) -> b.append(u + ","));
        b.append(first.get());
        return b.toString();
    }
}
