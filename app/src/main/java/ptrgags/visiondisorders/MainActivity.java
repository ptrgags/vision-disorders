package ptrgags.visiondisorders;

import android.opengl.GLES20;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.google.vr.ndk.base.AndroidCompat;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

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
        view.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

        view.setRenderer(this);
        view.setTransitionViewEnabled(true);

        if (view.setAsyncReprojectionEnabled(true)) {
            AndroidCompat.setSustainedPerformanceMode(this, true);
        }
        setGvrView(view);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initGvrView();
        createScenes();
    }

    private void createScenes() {
        Scene colorblindness = new Colorblindness();
        scenes.add(colorblindness);
        Scene akin = new Akinetopsia();
        scenes.add(akin);
        Scene hemi = new Hemianopia();
        scenes.add(hemi);

        for (Scene scene : scenes)
            scene.initScene();

        checkGLError("Ready to Draw");
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
        //TODO: Remove old shaders
        Shader diffuse = new Shader(
                GLES20.GL_VERTEX_SHADER, R.raw.diffuse, this);
        Shader colorblindness = new Shader(
                GLES20.GL_FRAGMENT_SHADER, R.raw.color_blindness, this);
        Shader lighting = new Shader(
                GLES20.GL_VERTEX_SHADER, R.raw.lighting, this);
        Shader lambert = new Shader(
                GLES20.GL_FRAGMENT_SHADER, R.raw.lambert, this);

        // Store the shaders in a hash map
        shaders.put("vert_diffuse", diffuse);
        shaders.put("frag_colorblind", colorblindness);
        shaders.put("vert_lighting", lighting);
        shaders.put("frag_lambert", lambert);

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

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_BUTTON_L1:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_BUTTON_X:
                prevScene();
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_BUTTON_R1:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_BUTTON_B:
                nextScene();
                break;
            case KeyEvent.KEYCODE_BUTTON_Y:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_BUTTON_R2:
                scenes.get(selectedScene).next();
                break;
            case KeyEvent.KEYCODE_BUTTON_A:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_BUTTON_L2:
                scenes.get(selectedScene).prev();
                break;
            default:
                Log.i("Vision Disorders", "User pressed: " + event.toString());
        }
        return true;
    }

    private void nextScene() {
        selectedScene++;
        selectedScene %= scenes.size();
    }

    private void prevScene() {
        selectedScene--;
        selectedScene %= scenes.size();

        // I miss Python :(
        if (selectedScene < 0)
            selectedScene += scenes.size();
    }
}
