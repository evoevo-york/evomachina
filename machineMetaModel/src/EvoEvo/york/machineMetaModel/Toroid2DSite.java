package EvoEvo.york.machineMetaModel;

import java.util.Optional;
import java.util.Set;

/** A special sort of Site representing a site in a 2D toroidal space */
public class Toroid2DSite extends Site {
    /** X coordinate of this site within the toroid */
    protected int _xpos;

    /** Y coordinate of this site within the toroid */
    protected int _ypos;

    public Toroid2DSite(Optional<Space> container, int x, int y) {
        super(container);
        _xpos = x;
        _ypos = y;
    }

    public int getXPosition() {
        return _xpos;
    }

    public int getYPosition() {
        return _ypos;
    }

    /** Answer the set of neighbouring sites that have no contained individual subspaces. */
    @Override
    public Set<Space> emptyNeighbours() {
        Toroidal2DSpace container = (Toroidal2DSpace)_container.get();
        return container.findNeighbours(_xpos, _ypos, sp -> sp.isEmpty());
    }

    /** Answer the set of neighbouring sites that have contained individual subspaces */
    @Override
    public Set<Space> nonEmptyNeighbours() {
        Toroidal2DSpace container = (Toroidal2DSpace)_container.get();
        return container.findNeighbours(_xpos, _ypos, sp -> !sp.isEmpty());
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer(String.format(" [%d:%d] ", this.getXPosition(), this.getYPosition()));
        if (this.numSubspaces() != 0) {
            Space s = this.getASubspace();
            result.append(String.format("[%s]", s));
        }

        return result.toString();
    }

}
