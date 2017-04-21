package ptrgags.visiondisorders;

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
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;

public class MainActivity extends GvrActivity implements GvrView.StereoRenderer {

    /** floor, walls and ceiling */
    private List<Model> room = new ArrayList<>();
    private ShaderProgram planeProgram;
    private Camera camera;

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
        //Maybe make a Scene class?
        Model floor = new Plane();
        floor.scale(5.0f, 1.0f, 5.0f);
        floor.translate(0.0f, -5.2f, 0.0f);
        room.add(floor);
        Model ceiling = new Plane();
        ceiling.scale(5.0f, 1.0f, 5.0f);
        ceiling.rotate(180, 1.0f, 0.0f, 0.0f);
        ceiling.translate(0.0f, 5.2f, 0.0f);
        room.add(ceiling);

        Model wall = new Plane();
        wall.scale(5.0f, 1.0f, 5.0f);
        wall.rotate(90, 0.0f, 0.0f, 1.0f);
        wall.translate(5.2f, 0.0f, 0.0f);
        room.add(wall);
        wall = new Plane();
        wall.scale(5.0f, 1.0f, 5.0f);
        wall.rotate(90, 0.0f, 0.0f, -1.0f);
        wall.translate(-5.2f, 0.0f, 0.0f);
        room.add(wall);
        wall = new Plane();
        wall.scale(5.0f, 1.0f, 5.0f);
        wall.rotate(90, 1.0f, 0.0f, 0.0f);
        wall.translate(0.0f, 0.0f, -5.2f);
        room.add(wall);
        wall = new Plane();
        wall.scale(5.0f, 1.0f, 5.0f);
        wall.rotate(90, -1.0f, 0.0f, 0.0f);
        wall.translate(0.0f, 0.0f, 5.2f);
        room.add(wall);

        camera = new Camera();
        camera.setPosition(0.0f, 0.0f, 0.01f);
        camera.setTarget(0.0f, 0.0f, 0.0f);
        camera.setUp(0.0f, 1.0f, 0.0f);

        checkGLError("Ready to Draw");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initGvrView();
        buildScene();
    }


    @Override
    public void onNewFrame(HeadTransform headTransform) {
    }

    @Override
    public void onDrawEye(Eye eye) {
        //Set drawing bits.
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        //GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        checkGLError("Color settings");

        //Get the projection matrix
        float[] projection = eye.getPerspective(0.1f, 100.0f);

        //Calculate the view matrix
        float[] eyeView = eye.getEyeView();
        float[] cameraView = camera.getViewMatrix();
        float[] view = new float[16];
        Matrix.multiplyMM(view, 0, eyeView, 0, cameraView, 0);

        //Draw planes.
        planeProgram.use();
        GLES20.glUniformMatrix4fv(
                planeProgram.getUniform("projection"), 1, false, projection, 0);
        GLES20.glUniformMatrix4fv(
                planeProgram.getUniform("view"), 1, false, view, 0);

        int modelParam = planeProgram.getUniform("model");
        int posParam = planeProgram.getAttribute("position");
        int colorParam = planeProgram.getAttribute("color");

        for (Model m : room) {
            float[] model = m.getModelMatrix();
            GLES20.glUniformMatrix4fv(modelParam, 1, false, model, 0);

            FloatBuffer modelCoords = m.getModelCoords();
            GLES20.glVertexAttribPointer(
                    posParam, 4, GLES20.GL_FLOAT, false, 0, modelCoords);

            FloatBuffer modelColors = m.getModelColors();
            GLES20.glVertexAttribPointer(
                    colorParam, 4, GLES20.GL_FLOAT, false, 0, modelColors);

            GLES20.glEnableVertexAttribArray(posParam);
            GLES20.glEnableVertexAttribArray(colorParam);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

            GLES20.glDisableVertexAttribArray(posParam);
            GLES20.glDisableVertexAttribArray(colorParam);
        }

        // Do the drawing. Make sure to check
        // eye.getType() to do eye-specific
        // behavior (e.g. Hemianopia, RVM)
    }

    @Override
    public void onFinishFrame(Viewport viewport) {}

    @Override
    public void onSurfaceChanged(int i, int i1) {}

    private void createBuffers() {
    }

    private void createShaders() {
        Shader vertAmbient = new Shader(
                GLES20.GL_VERTEX_SHADER, R.raw.ambient, this);
        Shader fragSimple = new Shader(
                GLES20.GL_FRAGMENT_SHADER, R.raw.simple_color, this);

        //TODO: maybe move some of this to a subclass?
        planeProgram = new ShaderProgram(vertAmbient, fragSimple);
        checkGLError("Plane program");
        planeProgram.addUniform("model");
        planeProgram.addUniform("view");
        planeProgram.addUniform("projection");
        planeProgram.addAttribute("position");
        planeProgram.addAttribute("color");
        checkGLError("Plane Params");
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        //Set a dark background
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f);

        //Create the buffers for all the objects we need
        createBuffers();
        //Also create the shaders
        createShaders();
    }

    @Override
    public void onRendererShutdown() {}

    public static void checkGLError(String label) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            String message = String.format("%s: glError %s", label, error);
            Log.e("VisualDisorders", message);
            throw new RuntimeException(message);
        }
    }
}
