package ptrgags.visiondisorders.scenes;

import android.opengl.GLES20;
import android.util.Log;

import com.google.vr.sdk.base.Eye;

import java.util.Map;

import ptrgags.visiondisorders.Shader;
import ptrgags.visiondisorders.Texture;

/**
 * A scene manages the models and
 */
public abstract class Scene {
    // Mode number;
    protected int mode;

    /**
     * Create Models and Camera(s)
     */
    public abstract void initScene();

    /**
     * Draw event. This should be overriden in subclasses with a call
     * to super.onDraw() somewhere at the beginning.
     * @param eye the eye to assist with computation.
     */
    public void onDraw(Eye eye) {
        //Set drawing bits.
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        checkGLError("Color settings");
    };

    /**
     * Initialize ShaderPrograms. The Shader objects are preallocated in
     * the main app since it needs access to the renderer.
     * @param shaders a map of shader names -> Shader objects available.
     */
    public abstract void initShaders(Map<String, Shader> shaders);

    public void initTextures(Map<String, Texture> textures){}

    public static void checkGLError(String label) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            String message = String.format("%s: glError %s", label, error);
            Log.e("VisualDisorders", message);
            throw new RuntimeException(message);
        }
    }

    /**
     * Show the next version of the current simulation
     */
    public void next() {
        mode++;
        mode %= getNumModes();
    }

    /**
     * Show the previous version of the current simulation
     */
    public void prev() {
        mode--;
        mode %= getNumModes();
        if (mode < 0)
            mode += getNumModes();
    }

    /**
     * Reset scene.
     */
    public void reset() {
        mode = 0;
    }

    public void onFrame() {}

    /**
     * Get the number of variations of this simulation
     * @return the number of simulation modes.
     */
    public abstract int getNumModes();

    public int getMode() {
        return mode;
    }
}
