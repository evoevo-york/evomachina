package EvoEvo.york.tspTest;

import EvoEvo.york.machineMetaModel.Space;
import EvoEvo.york.machineMetaModel.Toroid2DSite;

import java.util.Optional;
import java.util.logging.Logger;

/** A special space that will most likely contain subspaces and exists at an x,y coordinate */
public class Journey2DSite extends Toroid2DSite {

    private static Logger _Logger = Logger.getLogger("EvoEvo");

    public Journey2DSite(Optional<Space> container, int x, int y) {
        super(container, x, y);
        _runCount = 0;
    }

    @Override
    public String toString() {
        return String.format("Journey2DSite: <%d> %s", _runCount, super.toString());
    }
}

