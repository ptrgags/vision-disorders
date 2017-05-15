package ptrgags.visiondisorders.models;

import java.nio.FloatBuffer;

/**
 * Created by Peter on 4/23/2017.
 */

public class Cube extends Model {
    private static final float[] CUBE_COORDS = new float[] {
            //Top face
            -1, 1, -1, 1,
            -1, 1, 1, 1,
            1, 1, -1, 1,
            -1, 1, 1, 1,
            1, 1, 1, 1,
            1, 1, -1, 1,

            //Left face
            -1, -1, -1, 1,
            -1, -1, 1, 1,
            -1, 1, -1, 1,
            -1, -1, 1, 1,
            -1, 1, 1, 1,
            -1, 1, -1, 1,

            //Right face
            1, 1, -1, 1,
            1, 1, 1, 1,
            1, -1, -1, 1,
            1, 1, 1, 1,
            1, -1, 1, 1,
            1, -1, -1, 1,

            //Back face
            -1, -1, -1, 1,
            -1, 1, -1, 1,
            1, -1, -1, 1,
            -1, 1, -1, 1,
            1, 1, -1, 1,
            1, -1, -1, 1,

            //Front face
            -1, 1, 1, 1,
            -1, -1, 1, 1,
            1, 1, 1, 1,
            -1, -1, 1, 1,
            1, -1, 1, 1,
            1, 1, 1, 1,

            //Bottom face
            -1, -1, 1, 1,
            -1, -1, -1, 1,
            1, -1, 1, 1,
            -1, -1, -1, 1,
            1, -1, -1, 1,
            1, -1, 1, 1
    };

    private static final float[] CUBE_NORMALS = new float[] {
            //Top face
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,

            //Left face
            -1, 0, 0,
            -1, 0, 0,
            -1, 0, 0,
            -1, 0, 0,
            -1, 0, 0,
            -1, 0, 0,

            //Right face
            1, 0, 0,
            1, 0, 0,
            1, 0, 0,
            1, 0, 0,
            1, 0, 0,
            1, 0, 0,

            //Back face
            0, 0, -1,
            0, 0, -1,
            0, 0, -1,
            0, 0, -1,
            0, 0, -1,
            0, 0, -1,

            //Front
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,

            //Bottom
            0, -1, 0,
            0, -1, 0,
            0, -1, 0,
            0, -1, 0,
            0, -1, 0,
            0, -1, 0
    };

    private static final int NUM_VERTICES = 36;
    private static final float[] DEFAULT_COLOR = new float[]{1, 1, 1, 1};

    private float[] colors;

    public Cube() {
        this(DEFAULT_COLOR);
    }

    public Cube(float[] color) {
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
        return makeVertexBuffer(CUBE_COORDS);
    }

    @Override
    public FloatBuffer getModelColors() {
        return makeVertexBuffer(colors);
    }

    @Override
    public FloatBuffer getModelNormals() {
        return makeVertexBuffer(CUBE_NORMALS);
    }

    @Override
    public FloatBuffer getUVCoords() {
        return null;
    }
}
