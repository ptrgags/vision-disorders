package ptrgags.visiondisorders.models;

import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Geometry for some form of 3D model.
 */
public abstract class Model {
    /** Rotation of the model in world space */
    private float[] rotateMatrix = new float[16];
    /** Translation of the model in world space */
    private float[] translateMatrix = new float[16];
    /** Scale of the model in world space */
    private float[] scaleMatrix = new float[16];

    /**
     * Constructor. Initialize each matrix to the identity matrix
     */
    public Model() {
        Matrix.setIdentityM(rotateMatrix, 0);
        Matrix.setIdentityM(translateMatrix, 0);
        Matrix.setIdentityM(scaleMatrix, 0);
    }

    /**
     * Like Translate, but it resets the translation to the identity
     * @param x x-coordinate
     * @param y y-coordinate
     * @param z z-coordinate
     */
    public void translateTo(float x, float y, float z) {
        Matrix.setIdentityM(translateMatrix, 0);
        Matrix.translateM(translateMatrix, 0, x, y, z);
    }

    /**
     * Translate the model from the current position.
     * @param dx the change in x position
     * @param dy the change in y position
     * @param dz the change in z position
     */
    public void translate(float dx, float dy, float dz) {
        Matrix.translateM(translateMatrix, 0, dx, dy, dz);
    }

    /**
     * Like Translate, but it resets the translation to the identity
     * @param x x-coordinate
     * @param y y-coordinate
     * @param z z-coordinate
     */
    public void scaleTo(float x, float y, float z) {
        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.scaleM(scaleMatrix, 0, x, y, z);
    }

    /**
     * Scale the scale matrix by this amount
     * @param x x-scale
     * @param y y-scale
     * @param z z-scale
     */
    public void scale(float x, float y, float z) {
        Matrix.scaleM(scaleMatrix, 0, x, y, z);
    }

    /**
     * rotate the rotation matrix by a given amount specied as an axis
     * and an angle
     * @param angle the angle in degrees to rotate
     * @param x the axis's x component
     * @param y the axis's y component
     * @param z the axis's z component
     */
    public void rotate(float angle, float x, float y, float z) {
        Matrix.rotateM(rotateMatrix, 0, angle, x, y, z);
    }

    /**
     * Calculate the current model matrix for the model.
     * This performs the calculation in this order
     * model = translate * rotate * scale
     * @return the model matrix
     */
    public float[] getModelMatrix() {
        float[] modelMatrix = new float[16];
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.multiplyMM(modelMatrix, 0, scaleMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(modelMatrix, 0, rotateMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(modelMatrix, 0, translateMatrix, 0, modelMatrix, 0);
        return modelMatrix;
    }

    /**
     * Get a buffer of the model's model-space coordinates
     * @return a buffer suitable for passing to OpenGL
     */
    public abstract FloatBuffer getModelCoords();

    /**
     * Get a buffer of the vertex colors
     * @return a buffer suitable for passing to OpenGL
     */
    public abstract FloatBuffer getModelColors();

    /**
     * Get a buffer of the vertex normals
     * @return a buffer suitable for passing to OpenGL
     */
    public abstract FloatBuffer getModelNormals();

    /**
     * Get a buffer of vertex UV coordinates
     * @return a buffer suitable for passing to OpenGL
     */
    public abstract FloatBuffer getUVCoords();

    /**
     * Get the number of vertices the model needs to render
     * @return the number of vertices in the model
     */
    public abstract int getNumVertices();

    /**
     * Convert a float array to a FloatBuffer
     * @param vertices the floas to convert
     * @return a FloatBuffer for passing to GLES20
     */
    public static FloatBuffer makeVertexBuffer(float[] vertices) {
        ByteBuffer bytebuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        bytebuffer.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = bytebuffer.asFloatBuffer();
        floatBuffer.put(vertices);
        floatBuffer.position(0);
        return floatBuffer;
    }
}
