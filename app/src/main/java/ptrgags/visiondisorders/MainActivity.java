package ptrgags.visiondisorders;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

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

import ptrgags.visiondisorders.models.Model;
import ptrgags.visiondisorders.models.Plane;
import ptrgags.visiondisorders.scenes.Akinetopsia;
import ptrgags.visiondisorders.scenes.Colorblindness;
import ptrgags.visiondisorders.scenes.Hemianopia;
import ptrgags.visiondisorders.scenes.Scene;
import ptrgags.visiondisorders.scenes.Tetrachromacy;

//TODO: Document me!
public class MainActivity extends GvrActivity implements GvrView.StereoRenderer {
    private List<Scene> scenes = new ArrayList<>();
    private int selectedSceneIndex = 0;
    private ShaderProgram indicatorProgram;
    // Camera for the indicator
    private Camera camera;
    // Plane for rendering the mode indicator.
    private Model indicatorPlane;

    // Set this to true to show the indicators.
    private boolean indicatorsVisible = false;

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
        Scene tetra = new Tetrachromacy();
        scenes.add(tetra);

        for (Scene scene : scenes)
            scene.initScene();

        checkGLError("Ready to Draw");
    }


    @Override
    public void onNewFrame(HeadTransform headTransform) {
        scenes.get(selectedSceneIndex).onFrame();
    }

    @Override
    public void onDrawEye(Eye eye) {
        scenes.get(selectedSceneIndex).onDraw(eye);

        if (indicatorsVisible)
            drawIndicators();
    }

    private void drawIndicators() {
        //Set drawing bits.
        //TODO: Simplify this?
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        checkGLError("Color settings");

        // Get the camera view matrix
        float[] cameraView = camera.getViewMatrix();

        // Get an orthographic projection matrix
        float[] orthoProjection = new float[16];
        Matrix.orthoM(orthoProjection, 0, -1, 1, -1, 1, 0.1f, -3);

        indicatorProgram.use();

        //Set the matrices
        indicatorProgram.setUniformMatrix("projection", orthoProjection);
        indicatorProgram.setUniformMatrix("view", cameraView);
        float[] model = indicatorPlane.getModelMatrix();
        indicatorProgram.setUniformMatrix("model", model);

        // Set the simulation indicators
        indicatorProgram.setUniform("num_simulations", scenes.size());
        indicatorProgram.setUniform("selected_simulation", selectedSceneIndex);

        // Set the scene variation indicators
        Scene selectedScene = scenes.get(selectedSceneIndex);
        indicatorProgram.setUniform(
                "num_variations", selectedScene.getNumModes());
        indicatorProgram.setUniform(
                "selected_variation", selectedScene.getMode());

        // Enable attribute buffers
        indicatorProgram.enableAttribute("position");
        indicatorProgram.enableAttribute("uv");

        // Set the attributes
        FloatBuffer modelCoords = indicatorPlane.getModelCoords();
        indicatorProgram.setAttribute("position", modelCoords, 4);
        FloatBuffer uvCoords = indicatorPlane.getUVCoords();
        indicatorProgram.setAttribute("uv", uvCoords, 2);

        indicatorProgram.draw(indicatorPlane.getNumVertices());

        // Disable the attribute buffers
        indicatorProgram.disableAttributes();

        // Turn off blending.
        GLES20.glDisable(GLES20.GL_BLEND);
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
        for (Scene scene : scenes) {
            scene.initShaders(shaders);
        }

        initIndicator(shaders);
    }

    private void initIndicator(Map<String, Shader> shaders) {
        Shader vert = shaders.get("vert_uv");
        Shader frag = shaders.get("frag_indicator");
        indicatorProgram = new ShaderProgram(vert, frag);
        checkGLError("Plane program");

        camera = new Camera();
        camera.setPosition(0.0f, 0.0f, 0.1f);
        camera.setTarget(0.0f, 0.0f, 0.0f);
        camera.setUp(0.0f, 1.0f, 0.0f);

        //Create the indicator plane at the origin facing the z-axis
        indicatorPlane = new Plane();
        indicatorPlane.rotate(90, 1, 0, 0);
    }

    //TODO: make a class that handles this?
    private Map<String,Shader> makeShaders() {
        //Make Vertex shaders
        Map<String, Shader> shaders = new HashMap<>();

        // Create shaders
        Shader colorblindness = new Shader(
                GLES20.GL_FRAGMENT_SHADER, R.raw.color_blindness, this);
        Shader lighting = new Shader(
                GLES20.GL_VERTEX_SHADER, R.raw.lighting, this);
        Shader lambert = new Shader(
                GLES20.GL_FRAGMENT_SHADER, R.raw.lambert, this);
        Shader skyboxVert = new Shader(
                GLES20.GL_VERTEX_SHADER, R.raw.skybox_vert, this);
        Shader skyboxFrag = new Shader(
                GLES20.GL_FRAGMENT_SHADER, R.raw.skybox_frag, this);
        Shader tetraFrag = new Shader(
                GLES20.GL_FRAGMENT_SHADER, R.raw.tetrachrome_frag, this);
        Shader simpleUV = new Shader(
                GLES20.GL_VERTEX_SHADER, R.raw.simple_uv, this);
        Shader indicatorFrag = new Shader(
                GLES20.GL_FRAGMENT_SHADER, R.raw.mode_indicator, this);

        // Store the shaders in a hash map
        shaders.put("frag_colorblind", colorblindness);
        shaders.put("vert_lighting", lighting);
        shaders.put("frag_lambert", lambert);
        shaders.put("vert_skybox", skyboxVert);
        shaders.put("frag_skybox", skyboxFrag);
        shaders.put("frag_tetrachrome", tetraFrag);
        shaders.put("vert_uv", simpleUV);
        shaders.put("frag_indicator", indicatorFrag);

        return shaders;
    }

    @Override
    public void onRendererShutdown() {}

    @Override
    public void onCardboardTrigger() {
        scenes.get(selectedSceneIndex).next();
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
                scenes.get(selectedSceneIndex).next();
                break;
            case KeyEvent.KEYCODE_BUTTON_A:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_BUTTON_L2:
                scenes.get(selectedSceneIndex).prev();
                break;
            case KeyEvent.KEYCODE_BUTTON_START:
                scenes.get(selectedSceneIndex).reset();
            case KeyEvent.KEYCODE_BUTTON_SELECT:
                indicatorsVisible = !indicatorsVisible;
            default:
                Log.i("Vision Disorders", "User pressed: " + event.toString());
        }
        return true;
    }

    private void nextScene() {
        selectedSceneIndex++;
        selectedSceneIndex %= scenes.size();

        scenes.get(selectedSceneIndex).reset();
    }

    private void prevScene() {
        selectedSceneIndex--;
        selectedSceneIndex %= scenes.size();

        // I miss Python :(
        if (selectedSceneIndex < 0)
            selectedSceneIndex += scenes.size();

        scenes.get(selectedSceneIndex).reset();
    }
}
