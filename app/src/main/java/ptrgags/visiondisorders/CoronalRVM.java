package ptrgags.visiondisorders;

import android.opengl.Matrix;

import com.google.vr.sdk.base.Eye;

/**
 * Simulate RVM in the coronal plane. a 180 degree coronal
 * RVM rotates the whole scene so that the right eye is viewing
 * an upside-down left field of vision and vice-versa.
 */
public class CoronalRVM implements RVM {
    /**
     * Angle of rotation (CCW in degrees) of the view
     */
    private float angle;

    /**
     * Constructor
     * @param angle the angle to rotate
     */
    public CoronalRVM(float angle) {
        this.angle = angle;
    }

    /**
     * Rotate the view using the formula:
     *
     * view = eyeView * R(-z, angle) * cameraView
     *
     * The rotation is applied to the camera since
     *
     * @param cameraView the camera view matrix.
     * @param eye the Eye object
     * @return the final view matrix
     */
    @Override
    public float[] rotateView(float[] cameraView, Eye eye) {
        float[] view = new float[16];
        Matrix.rotateM(view, 0, cameraView, 0, angle, 0, 0, -1);
        float[] eyeView = eye.getEyeView();
        Matrix.multiplyMM(view, 0, eyeView, 0, view, 0);
        return view;
    }
}
