package EvoEvo.york.machineMetaModel;

/** A structure has a code that is a sequence of Pearls, each of which is drawn from a specific Domain. All of the Pearls for a specific structure
 *  are required to be drawn from the same Domain. */
public abstract class Pearl implements Cloneable {
    protected Domain _domain;

    /** True if this particular unit codes for something. That it, it should be transcribed into a part of a transcription unit. */
    protected boolean _coding;

    public Domain getDomain() {
        return _domain;
    }

    /** All Pearls must know from which Domain they're drawn */
    public Pearl(Domain domain) {
        _domain = domain;
        _coding = true; // By default all units are coding.
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Pearl result =  (Pearl)super.clone();
        result._domain = _domain;
        return result;
    }

    /** Answer true if the receiver is drawn from the supplied domain */
    public boolean isFrom(Domain domain) {
        return domain.equals(_domain);
    }

    /** A version of clone() that does not throw a checked exception */
    public Pearl klone() {
        try {
            return (Pearl)this.clone();
        } catch (CloneNotSupportedException e) {
           throw new MetaModelException("Clone Not Supported");
        }
    }

    /** Answer true if this unit actually codes for something or should be ignored in transcription.  */
    public boolean isCoding() {
        return _coding;
    }

    public Pearl setCoding(boolean b) {
        _coding = b;
        return this;
    }
}
