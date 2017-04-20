package ptrgags.visiondisorders;

import android.opengl.Matrix;

/**
 * Created by Peter on 4/20/2017.
 */

class Shape3D {
    private float[] modelMatrix = new float[16];
    public Shape3D() {
        Matrix.setIdentityM(modelMatrix, 0);
    }

    public void translate(float x, float y, float z) {
        Matrix.translateM(modelMatrix, 0, x, y, z);
    }

    public void scale(float x, float y, float z) {
        Matrix.scaleM(modelMatrix, 0, x, y, z);
    }

    public void rotate(int angle, float x, float y, float z) {
        Matrix.rotateM(modelMatrix, 0, angle, x, y, z);
    }
}
