package EvoEvo.york.machineMetaModel;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

/** A sort of space where the subspaces are arranged in a 2D surface which wraps in both dimensions */
public abstract class Toroidal2DSpace extends Space {
    int _xSize;
    int _ySize;

    public Toroidal2DSpace(Optional<Space> container, int xSize, int ySize) {
        super(container);
        _xSize = xSize;
        _ySize = ySize;
    }

    /** Answer the supplied coordinate mapped onto the x-axis of the toroid */
    protected int mapX(int x) {
        return (x < 0) ? x + _xSize : (x >= _xSize ? x - _xSize : x );
    }

    /** Answer the supplied coordinate mapped onto the y-axis of the toroid */
    protected int mapY(int y) {
        return (y < 0) ? y + _ySize : (y >= _ySize ? y - _ySize : y );
    }

    /** Answer the set of spaces that are adjacent to the supplied coordinates and which satisfy some predicate. */
    public Set<Space> findNeighbours(int xPos, int yPos, Predicate<Space> p) {
        Set<Space> result = new HashSet<>();
        for (int x = xPos - 1; x <= xPos + 1; x++) {
            for (int y = yPos - 1; y <= yPos + 1; y++) {
                if (x != xPos || y != yPos) {
                    Space space = this.getSubspace(this.mapX(x), this.mapY(y));
                    if (p.test(space))
                        result.add(space);
                }
            }
        }
        return result;
    }

    /** Answer the Subspace at position (x,y) */
    public Space getSubspace(int x, int y) {
        return this.getSubspace((x *_ySize) + y);
    }

    /** Find an empty space in the receiver and answer the coordiinates as an x,y pair. If the space is full, this just
     *  does not terminate */
    public int[] findEmptySpace() {
        int[] result = new int[2];
        boolean found = false;
        do {
            result[0] = ThreadLocalRandom.current().nextInt(Simulation.GetValue("xSize"));
            result[1] = ThreadLocalRandom.current().nextInt(Simulation.GetValue("ySize"));

            found = this.getSubspace(result[0], result[1]).isEmpty();
        } while (!found);
        return result;
    }
}
