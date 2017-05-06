package ptrgags.visiondisorders;

import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Peter on 4/20/2017.
 */

abstract class Model {
    private float[] rotateMatrix = new float[16];
    private float[] translateMatrix = new float[16];
    private float[] scaleMatrix = new float[16];

    public Model() {
        Matrix.setIdentityM(rotateMatrix, 0);
        Matrix.setIdentityM(translateMatrix, 0);
        Matrix.setIdentityM(scaleMatrix, 0);
    }

    /**
     * Like Translate, but it resets the translation to the identitty
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

    public void scale(float x, float y, float z) {
        Matrix.scaleM(scaleMatrix, 0, x, y, z);
    }

    public void rotate(float angle, float x, float y, float z) {
        Matrix.rotateM(rotateMatrix, 0, angle, x, y, z);
    }

    public float[] getModelMatrix() {
        float[] modelMatrix = new float[16];
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.multiplyMM(modelMatrix, 0, scaleMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(modelMatrix, 0, rotateMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(modelMatrix, 0, translateMatrix, 0, modelMatrix, 0);
        return modelMatrix;
    }

    public abstract FloatBuffer getModelCoords();

    public abstract FloatBuffer getModelColors();

    public abstract FloatBuffer getModelNormals();

    public static FloatBuffer makeVertexBuffer(float[] vertices) {
        ByteBuffer bytebuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        bytebuffer.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = bytebuffer.asFloatBuffer();
        floatBuffer.put(vertices);
        floatBuffer.position(0);
        return floatBuffer;
    }
}
