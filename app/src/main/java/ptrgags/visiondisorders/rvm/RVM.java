package ptrgags.visiondisorders.rvm;

import com.google.vr.sdk.base.Eye;

/**
 * This class handlles Reversal of Vision Metamorphopsia
 * by adding a rotation to the normal view calculation. This
 * can be done one of a couple different ways depending on the desired
 * effect. So this is a strategy pattern to handle the view mattrix calculation
 */
public interface RVM {
    /**
     * Given the camera view and the Eye object, calculate the
     * view matrix.
     * @param cameraView the camera view matrix.
     * @param eye the Eye object
     * @return the calculated  view matrix.
     */
    float[] rotateView(float[] cameraView, Eye eye);
}
