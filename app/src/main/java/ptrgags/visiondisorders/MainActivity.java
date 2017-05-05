package ptrgags.visiondisorders;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;

import com.google.vr.ndk.base.AndroidCompat;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;

public class MainActivity extends GvrActivity implements GvrView.StereoRenderer {
    private List<Scene> scenes = new ArrayList<>();
    private int selectedScene = 0;

    /**
     * Init the Google VR view.
     */
    private void initGvrView() {
        setContentView(R.layout.common_ui);
        GvrView view = (GvrView) findViewById(R.id.gvr_view);
        //TODO: What does this do?
        view.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

        view.setRenderer(this);
        //TODO: What does this do?
        view.setTransitionViewEnabled(true);

        if (view.setAsyncReprojectionEnabled(true)) {
            AndroidCompat.setSustainedPerformanceMode(this, true);
        }
        setGvrView(view);
    }

    /**
     * Build the geometry of the scene. Most
     * things will not move so this is nice.
     */
    private void buildScene() {
        for (Scene scene : scenes)
            scene.initScene();

        checkGLError("Ready to Draw");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initGvrView();
        createScenes();
        //buildScene();
    }

    private void createScenes() {
        Scene colorblindness = new Colorblindness();
        scenes.add(colorblindness);

        for (Scene scene : scenes)
            scene.initScene();
    }


    @Override
    public void onNewFrame(HeadTransform headTransform) {
        scenes.get(selectedScene).onFrame();
    }

    @Override
    public void onDrawEye(Eye eye) {
        scenes.get(selectedScene).onDraw(eye);
    }

    @Override
    public void onFinishFrame(Viewport viewport) {}

    @Override
    public void onSurfaceChanged(int i, int i1) {}

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        //Set a dark background
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f);

        //Also create the shaders
        Map<String, Shader> shaders = makeShaders();
        for (Scene scene : scenes)
            scene.initShaders(shaders);
    }

    private Map<String,Shader> makeShaders() {
        //Make Vertex shaders
        Map<String, Shader> shaders = new HashMap<>();

        // Create shaders
        Shader diffuse = new Shader(
                GLES20.GL_VERTEX_SHADER, R.raw.diffuse, this);
        Shader colorblindness = new Shader(
                GLES20.GL_FRAGMENT_SHADER, R.raw.color_blindness, this);

        // Store the shaders in a hash map
        shaders.put("vert_diffuse", diffuse);
        shaders.put("frag_colorblind", colorblindness);

        return shaders;
    }

    @Override
    public void onRendererShutdown() {}

    @Override
    public void onCardboardTrigger() {
        scenes.get(selectedScene).next();
    }

    public static void checkGLError(String label) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            String message = String.format("%s: glError %s", label, error);
            Log.e("VisualDisorders", message);
            throw new RuntimeException(message);
        }
    }
}
