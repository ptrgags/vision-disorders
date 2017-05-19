package ptrgags.visiondisorders.models;

import java.nio.FloatBuffer;

/**
 * a simple square plane shape. 2 Triangles. Can't get much
 * simpler than that.
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

    private static final float[] PLANE_NORMALS = new float[] {
            //All normals point upwards!
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0
    };

    private static final float[] PLANE_UV = new float[] {
            //First Triangle
            0, 1,
            0, 0,
            1, 1,

            // Second triangle
            0, 0,
            1, 0,
            1, 1,
    };

    private static final int NUM_VERTICES = 6;
    private static final float[] DEFAULT_COLOR = new float[]{1, 1, 1, 1};

    private float[] colors;

    public Plane() {
        this(DEFAULT_COLOR);
    }

    public Plane(float[] color) {
        final int COLOR_COMPONENTS = 4;
        colors = new float[NUM_VERTICES * COLOR_COMPONENTS];
        for (int i = 0; i < NUM_VERTICES; i++) {
            for (int j = 0; j < COLOR_COMPONENTS; j++) {
                colors[i * COLOR_COMPONENTS + j] = color[j];
            }
        }
    }

    @Override
    public FloatBuffer getModelCoords() {
        return makeVertexBuffer(PLANE_COORDS);
    }

    @Override
    public FloatBuffer getModelColors() {
        return makeVertexBuffer(colors);
    }

    @Override
    public FloatBuffer getModelNormals() {
        return makeVertexBuffer(PLANE_NORMALS);
    }

    @Override
    public FloatBuffer getUVCoords() {
        return makeVertexBuffer(PLANE_UV);
    }
}
