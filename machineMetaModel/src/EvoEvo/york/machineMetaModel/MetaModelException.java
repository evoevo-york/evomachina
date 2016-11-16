package EvoEvo.york.machineMetaModel;

/** Super class, and perhaps the only class, of meta model exceptions  */
public class MetaModelException extends RuntimeException {
    public MetaModelException(String message) {
        super(message);
    }

    public MetaModelException(String message, Throwable cause) {
        super(message, cause);
    }
}
