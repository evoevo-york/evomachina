package EvoEvo.york.machineMetaModel;

import java.util.List;

/** A translator machine is responsible for taking a transcription unit and building the machine that is coded for
 *  by that transcription unit.
 *  todo: resolve issues around the absence of start and end locations and the fact that only a single machine is coded for by the transcription unit. */
public class Translator extends SourceMachine  implements EssentialMachine {
    public Translator(Space environment, List<Pearl> code, Domain domain) throws MetaModelException {
        super(environment, code, domain);
    }

    /** A translator machine must take its source structure, which is the transcription unit, and
     *  generate the machine that is encoded by that structure */
    @Override
    public Structure doIt() {
        this.ensureSource();

        // The domain of the source structure defines the sort of machine that the supplied transcription unit codes for.
        // Ask the domain to make a new machine of the appropriate sort and use the source as the code for that machine, even though
        // in many cases the code will be of zero length
        return _source.get()
                      .getDomain()
                      .constructMachine(_environment, _source.get().getCode());
    }
}
