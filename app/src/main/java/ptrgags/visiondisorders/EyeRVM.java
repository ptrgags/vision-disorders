package ptrgags.visiondisorders;

import android.opengl.Matrix;

import com.google.vr.sdk.base.Eye;


/**
 * Reversal of vision that rotates each field of vision independently.
 * For example, one patient had the left visual field rotated at 45 degrees
 * and the other eye had vision flipped upside-down
 */
class EyeRVM implements RVM {
    /** angle of rotation for the left eye CCW in degrees */
    private float leftAngle;
    /** angle of rotation for the right eye CCW in degrees */
    private float rightAngle;

    public EyeRVM(float leftAngle, float rightAngle) {
        this.leftAngle = leftAngle;
        this.rightAngle = rightAngle;
    }

    @Override
    public float[] rotateView(float[] cameraView, Eye eye) {
        float[] view = new float[16];
        float[] eyeView = eye.getEyeView();
        Matrix.multiplyMM(view, 0, eyeView, 0, cameraView, 0);
        float angle = (eye.getType() == Eye.Type.LEFT) ? leftAngle : rightAngle;
        Matrix.rotateM(view, 0, angle, 0, 0, -1);
        return view;
    }
}
