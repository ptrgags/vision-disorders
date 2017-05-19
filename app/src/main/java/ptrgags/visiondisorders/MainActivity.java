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
import ptrgags.visiondisorders.scenes.ReversalOfVision;
import ptrgags.visiondisorders.scenes.Scene;
import ptrgags.visiondisorders.scenes.Tetrachromacy;

public class MainActivity extends GvrActivity implements GvrView.StereoRenderer {
    private List<Scene> scenes = new ArrayList<>();
    private int selectedScene = 0;
    private ShaderProgram indicatorProgram;
    // Camera for the indicator
    private Camera camera;
    // Plane for rendering the mode indicator.
    private Model indicatorPlane;

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
        scenes.get(selectedScene).onFrame();
    }

    @Override
    public void onDrawEye(Eye eye) {
        scenes.get(selectedScene).onDraw(eye);

        //if (eye.getType() == Eye.Type.LEFT
        drawIndicators();
    }

    private void drawIndicators() {
        //Set drawing bits.
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
        GLES20.glUniformMatrix4fv(
                indicatorProgram.getUniform("projection"),
                1, false, orthoProjection, 0);
        GLES20.glUniformMatrix4fv(
                indicatorProgram.getUniform("view"), 1, false, cameraView, 0);
        GLES20.glUniformMatrix4fv(
                indicatorProgram.getUniform("model"),
                1, false, indicatorPlane.getModelMatrix(), 0);

        // Set the simuulation/variation indicators
        GLES20.glUniform1i(
                indicatorProgram.getUniform("num_simulations"), scenes.size());
        GLES20.glUniform1i(
                indicatorProgram.getUniform("selected_simulation"),
                selectedScene);

        //TODO: Set these dynamically
        GLES20.glUniform1i(
                indicatorProgram.getUniform("num_variations"), 3);
        GLES20.glUniform1i(
                indicatorProgram.getUniform("selected_variation"), 0);

        // Set the attributes
        int posParam = indicatorProgram.getAttribute("position");
        int uvParam = indicatorProgram.getAttribute("uv");
        GLES20.glVertexAttribPointer(
                posParam, 4, GLES20.GL_FLOAT, false, 0,
                indicatorPlane.getModelCoords());
        GLES20.glVertexAttribPointer(
                uvParam, 2, GLES20.GL_FLOAT, false, 0,
                indicatorPlane.getUVCoords());

        GLES20.glEnableVertexAttribArray(posParam);
        GLES20.glEnableVertexAttribArray(uvParam);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        GLES20.glDisableVertexAttribArray(posParam);
        GLES20.glDisableVertexAttribArray(uvParam);

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
        Map<String, Texture> textures = makeTextures();
        for (Scene scene : scenes) {
            scene.initShaders(shaders);
            scene.initTextures(textures);
        }

        initIndicator(shaders);
    }

    private void initIndicator(Map<String, Shader> shaders) {
        Shader vert = shaders.get("vert_uv");
        Shader frag = shaders.get("frag_indicator");
        indicatorProgram = new ShaderProgram(vert, frag);
        checkGLError("Plane program");
        indicatorProgram.addUniform("model");
        indicatorProgram.addUniform("view");
        indicatorProgram.addUniform("projection");
        indicatorProgram.addUniform("num_simulations");
        indicatorProgram.addUniform("selected_simulation");
        indicatorProgram.addUniform("num_variations");
        indicatorProgram.addUniform("selected_variation");
        indicatorProgram.addAttribute("position");
        indicatorProgram.addAttribute("uv");
        checkGLError("Program Params");

        camera = new Camera();
        camera.setPosition(0.0f, 0.0f, 0.1f);
        camera.setTarget(0.0f, 0.0f, 0.0f);
        camera.setUp(0.0f, 1.0f, 0.0f);

        //Create the indicator plane at the origin facing the z-axis
        indicatorPlane = new Plane();
        indicatorPlane.rotate(90, 1, 0, 0);
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
        shaders.put("vert_diffuse", diffuse);
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

    private Map<String, Texture> makeTextures() {
        Texture citySkybox = new Texture(this, R.drawable.skybox_city);
        Texture colorful = new Texture(this, R.drawable.colorful);

        Map<String, Texture> textures = new HashMap<>();
        textures.put("city_skybox", citySkybox);
        textures.put("colorful", colorful);
        return textures;
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
            case KeyEvent.KEYCODE_BUTTON_START:
                scenes.get(selectedScene).reset();
            default:
                Log.i("Vision Disorders", "User pressed: " + event.toString());
        }
        return true;
    }

    private void nextScene() {
        selectedScene++;
        selectedScene %= scenes.size();

        scenes.get(selectedScene).reset();
    }

    private void prevScene() {
        selectedScene--;
        selectedScene %= scenes.size();

        // I miss Python :(
        if (selectedScene < 0)
            selectedScene += scenes.size();

        scenes.get(selectedScene).reset();
    }
}
