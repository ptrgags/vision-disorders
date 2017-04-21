package ptrgags.visiondisorders;

import java.nio.FloatBuffer;

/**
 * Created by Peter on 4/20/2017.
 */

public class Plane extends Model {
    private static final float[] PLANE_COORDS = new float[] {
            //First triangle
            -1, 0, -1, 1,
            -1, 0, 1, 1,
            1, 0, -1, 1,

            //Second triangle
            -1, 0, 1, 1,
            1, 0, 1, 1,
            1, 0, -1, 1,
    };

    private static final float[] PLANE_COLORS = new float[] {
            //Color it purple to stand out
            0.5f, 0, 1, 1,
            0.5f, 0, 1, 1,
            0.5f, 0, 1, 1,
            0.5f, 0, 1, 1,
            0.5f, 0, 1, 1,
            0.5f, 0, 1, 1,
    };

    @Override
    public FloatBuffer getModelCoords() {
        return makeVertexBuffer(PLANE_COORDS);
    }

    @Override
    public FloatBuffer getModelColors() {
        return makeVertexBuffer(PLANE_COLORS);
    }
}
