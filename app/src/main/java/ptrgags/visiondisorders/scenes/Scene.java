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
    /**
     * Create Models and Camera(s)
     */
    public abstract void initScene();

    /**
     * Draw event
     * @param eye the eye to assist with computation.
     */
    public abstract void onDraw(Eye eye);

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
    public void next() {}

    /**
     * Show the previous version of the current simulation
     */
    public void prev() {}

    public void onFrame() {}
}
