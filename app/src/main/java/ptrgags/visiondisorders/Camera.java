package ptrgags.visiondisorders;

import android.opengl.Matrix;

/**
 * TODO: Document me!
 * Created by Peter on 4/20/2017.
 */

public class Camera {
    private float[] position = new float[3];
    private float[] target = new float[3];
    private float[] up = new float[3];

    public void setPosition(float x, float y, float z) {
        position[0] = x;
        position[1] = y;
        position[2] = z;
    }

    public void setTarget(float x, float y, float z) {
        target[0] = x;
        target[1] = y;
        target[2] = z;
    }

    public void setUp(float x, float y, float z) {
        up[0] = x;
        up[1] = y;
        up[2] = z;
    }

    public float[] getViewMatrix() {
        float[] view = new float[16];
        Matrix.setLookAtM(
                view, 0,
                position[0], position[1], position[2],
                target[0], target[1], target[2],
                up[0], up[1], up[2]);
        return view;
    }
}
