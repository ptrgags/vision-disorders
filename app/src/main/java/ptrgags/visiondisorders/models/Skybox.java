package ptrgags.visiondisorders.models;

import java.nio.FloatBuffer;

/**
 * This is a lot like a cube, but the front faces are on the inside of the
 * cube.
 */
public class Skybox extends Model {
    /** Total number of vertices */
    private static final int NUM_VERTICES = 36;

    /** vertex coordinates in world space */
    private static final float[] SKYBOX_COORDS = new float[]{
            // Bottom
            -1, -1, -1, 1,
            -1, -1, 1, 1,
            1, -1, -1, 1,
            -1, -1, 1, 1,
            1, -1, 1, 1,
            1, -1, -1, 1,

            // Front
            -1, 1, -1, 1,
            -1, -1, -1, 1,
            1, 1, -1, 1,
            -1, -1, -1, 1,
            1, -1, -1, 1,
            1, 1, -1, 1,

            // Right
            1, 1, -1, 1,
            1, -1, -1, 1,
            1, 1, 1, 1,
            1, -1, -1, 1,
            1, -1, 1, 1,
            1, 1, 1, 1,

            // Left
            -1, 1, 1, 1,
            -1, -1, 1, 1,
            -1, 1, -1, 1,
            -1, -1, 1, 1,
            -1, -1, -1, 1,
            -1, 1, -1, 1,

            // Back
            1, 1, 1, 1,
            1, -1, 1, 1,
            -1, 1, 1, 1,
            1, -1, 1, 1,
            -1, -1, 1, 1,
            -1, 1, 1, 1,

            // Top
            1, 1, -1, 1,
            1, 1, 1, 1,
            -1, 1, -1, 1,
            1, 1, 1, 1,
            -1, 1, 1, 1,
            -1, 1, -1, 1
    };

    /** vertex normals */
    private static final float[] SKYBOX_NORMALS = new float[] {
            //Botttom
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,

            // Front
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,

            //Right
            -1, 0, 0,
            -1, 0, 0,
            -1, 0, 0,
            -1, 0, 0,
            -1, 0, 0,
            -1, 0, 0,

            //Left
            1, 0, 0,
            1, 0, 0,
            1, 0, 0,
            1, 0, 0,
            1, 0, 0,
            1, 0, 0,

            //Back
            0, 0, -1,
            0, 0, -1,
            0, 0, -1,
            0, 0, -1,
            0, 0, -1,
            0, 0, -1,

            //Top
            0, -1, 0,
            0, -1, 0,
            0, -1, 0,
            0, -1, 0,
            0, -1, 0,
            0, -1, 0
    };

    /** vertex UV coordinates */
    private static final float[] SKYBOX_UV = new float[] {
            // Bottom
            0.25f, 0.75f,
            0.25f, 0.5f,
            0.5f, 0.75f,
            0.25f, 0.5f,
            0.5f, 0.5f,
            0.5f, 0.75f,

            // Front
            0.25f, 1.0f,
            0.25f, 0.75f,
            0.5f, 1.0f,
            0.25f, 0.75f,
            0.5f, 0.75f,
            0.5f, 1.0f,

            // Right
            0.75f, 0.75f,
            0.5f, 0.75f,
            0.75f, 0.5f,
            0.5f, 0.75f,
            0.5f, 0.5f,
            0.75f, 0.5f,

            // Left
            0.0f, 0.5f,
            0.25f, 0.5f,
            0.0f, 0.75f,
            0.25f, 0.5f,
            0.25f, 0.75f,
            0.0f, 0.75f,

            // Back
            0.5f, 0.25f,
            0.5f, 0.5f,
            0.25f, 0.25f,
            0.5f, 0.5f,
            0.25f, 0.5f,
            0.25f, 0.25f,

            // Top
            0.5f, 0.0f,
            0.5f, 0.25f,
            0.25f, 0.0f,
            0.5f, 0.25f,
            0.25f, 0.25f,
            0.25f, 0.0f,
    };

    @Override
    public FloatBuffer getModelCoords() {
        return makeVertexBuffer(SKYBOX_COORDS);
    }

    @Override
    public FloatBuffer getModelColors() {
        return null;
    }

    @Override
    public FloatBuffer getModelNormals() {
        return makeVertexBuffer(SKYBOX_NORMALS);
    }

    @Override
    public FloatBuffer getUVCoords() {
        return makeVertexBuffer(SKYBOX_UV);
    }

    @Override
    public int getNumVertices() {
        return NUM_VERTICES;
    }
}
