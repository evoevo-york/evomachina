package EvoEvo.york.machineMetaModel;

import java.util.List;
import java.util.stream.Collectors;

/** A transcriber machine reads a portion of a genome (that is, the machine template repository) and transcribes it into
 *  a structure that takes the role of a transcription unit. Rather than being realeased into the environment, the transcription
 *  unit is returned from the doIt method. */
public class Transcriber extends SourceMachine implements EssentialMachine {

    public Transcriber(Space environment, List<Pearl> code, Domain domain) throws MetaModelException {
        super(environment, code, domain);
    }

    /** Generate, and return, the transcription unit from the supplied source structure. Presently this is dim, although
     *  its one bit of cleverness is that the resulting transcription unit includes no non-coding units. */
    @Override
    public Structure doIt() {
        this.ensureSource();
        Structure result = _source.get().klone();

        result.setCode(result.getCode().stream().filter(p -> p.isCoding()).collect(Collectors.toList()));
        return result;
    }
}
