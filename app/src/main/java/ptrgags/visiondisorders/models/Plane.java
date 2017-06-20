package ptrgags.visiondisorders.models;

import java.nio.FloatBuffer;

/**
 * a simple square plane shape. 2 Triangles. Can't get much
 * simpler than that.
 */
public class Plane extends Model {
    /** vertex coordinates in model space */
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

    /** vertex normals */
    private static final float[] PLANE_NORMALS = new float[] {
            //All normals point upwards!
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0
    };

    /** vertex UV coordinates */
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

    /** number of vertices in total */
    private static final int NUM_VERTICES = 6;
    /** Default color is white */
    private static final float[] DEFAULT_COLOR = new float[]{1, 1, 1, 1};

    /** vertex colors */
    private float[] colors;

    /**
     * Make a white plane
     */
    public Plane() {
        this(DEFAULT_COLOR);
    }

    /**
     * Create a plane with a given color
     * @param color the color [r, g, b, a]
     */
    public Plane(float[] color) {
        //Copy the color for each vertex
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

    @Override
    public int getNumVertices() {
        return NUM_VERTICES;
    }
}
