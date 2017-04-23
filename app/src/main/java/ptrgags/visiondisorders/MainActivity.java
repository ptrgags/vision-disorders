package ptrgags.visiondisorders;

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
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;

public class MainActivity extends GvrActivity implements GvrView.StereoRenderer {

    /** floor, walls and ceiling */
    private List<Model> room = new ArrayList<>();
    private List<Model> cubes = new ArrayList<>();
    private ShaderProgram planeProgram;
    private Camera camera;

    private static final int NUM_COLOR_BLINDNESS_MODES = 3;
    private int colorBlindnessMode = 0;

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
        final float[] FLOOR_COLOR = new float[]{0.5f, 0, 1, 1};
        //Maybe make a Scene class?
        Model floor = new Plane(FLOOR_COLOR);
        floor.scale(14.0f, 1.0f, 14.0f);
        floor.translate(0.0f, -15f, 0.0f);
        room.add(floor);
        Model ceiling = new Plane(FLOOR_COLOR);
        ceiling.scale(14.0f, 1.0f, 14.0f);
        ceiling.rotate(180, 1.0f, 0.0f, 0.0f);
        ceiling.translate(0.0f, 15f, 0.0f);
        room.add(ceiling);

        final float[] WALL_COLOR = new float[]{1.0f, 0.5f, 0, 1};
        Model wall = new Plane(WALL_COLOR);
        wall.scale(14.0f, 1.0f, 14.0f);
        wall.rotate(90, 0.0f, 0.0f, 1.0f);
        wall.translate(15f, 0.0f, 0.0f);
        room.add(wall);
        wall = new Plane(WALL_COLOR);
        wall.scale(14.0f, 1.0f, 14.0f);
        wall.rotate(90, 0.0f, 0.0f, -1.0f);
        wall.translate(-15f, 0.0f, 0.0f);
        room.add(wall);
        wall = new Plane(WALL_COLOR);
        wall.scale(14.0f, 1.0f, 14.0f);
        wall.rotate(90, 1.0f, 0.0f, 0.0f);
        wall.translate(0.0f, 0.0f, -15f);
        room.add(wall);
        wall = new Plane(WALL_COLOR);
        wall.scale(14.0f, 1.0f, 14.0f);
        wall.rotate(90, -1.0f, 0.0f, 0.0f);
        wall.translate(0.0f, 0.0f, 15f);
        room.add(wall);

        buildCubes();

        camera = new Camera();
        camera.setPosition(0.0f, 0.0f, 0.1f);
        camera.setTarget(0.0f, 0.0f, 0.0f);
        camera.setUp(0.0f, 1.0f, 0.0f);

        checkGLError("Ready to Draw");
    }

    private void buildCubes() {
        final int CUBE_RADIUS = 3;
        final float OFFSET = 4.0f;
        for (int i = -CUBE_RADIUS; i <= CUBE_RADIUS; i++) {
            for (int j = -CUBE_RADIUS; j <= CUBE_RADIUS; j++) {
                for (int k = -CUBE_RADIUS; k <= CUBE_RADIUS; k++) {
                    //We only care about coordinates on the outside of the cube
                    int maxComponent = Math.max(
                            Math.max(Math.abs(i), Math.abs(j)), Math.abs(k));
                    if (maxComponent < CUBE_RADIUS)
                        continue;

                    float[] color = new float[] {
                            (i + 2.0f) / 4.0f,
                            (j + 2.0f) / 4.0f,
                            (k + 2.0f) / 4.0f,
                            1
                    };
                    Model cube = new Cube(color);
                    cube.translate(OFFSET * i, OFFSET * j, OFFSET * k);
                    cubes.add(cube);
                }
            }
        }
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
        GLES20.glEnable(GLES20.GL_CULL_FACE);
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
        GLES20.glUniform1i(
                planeProgram.getUniform("colorblind_mode"), colorBlindnessMode);

        int modelParam = planeProgram.getUniform("model");
        int posParam = planeProgram.getAttribute("position");
        int colorParam = planeProgram.getAttribute("color");
        int normalParam = planeProgram.getAttribute("normal");

        for (Model m : room) {
            float[] model = m.getModelMatrix();
            GLES20.glUniformMatrix4fv(modelParam, 1, false, model, 0);

            FloatBuffer modelCoords = m.getModelCoords();
            GLES20.glVertexAttribPointer(
                    posParam, 4, GLES20.GL_FLOAT, false, 0, modelCoords);

            FloatBuffer modelColors = m.getModelColors();
            GLES20.glVertexAttribPointer(
                    colorParam, 4, GLES20.GL_FLOAT, false, 0, modelColors);

            FloatBuffer modelNormals = m.getModelNormals();
            GLES20.glVertexAttribPointer(
                    normalParam, 3, GLES20.GL_FLOAT, false, 0, modelNormals);

            GLES20.glEnableVertexAttribArray(posParam);
            GLES20.glEnableVertexAttribArray(colorParam);
            GLES20.glEnableVertexAttribArray(normalParam);

            //TODO: Do we want the walls?
            //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

            GLES20.glDisableVertexAttribArray(posParam);
            GLES20.glDisableVertexAttribArray(colorParam);
            GLES20.glDisableVertexAttribArray(normalParam);

            checkGLError("Render Plane");
        }

        //TODO: wrap this up in a function, this is sooo ugly!!!
        for (Model m : cubes) {
            float[] model = m.getModelMatrix();
            GLES20.glUniformMatrix4fv(modelParam, 1, false, model, 0);

            FloatBuffer modelCoords = m.getModelCoords();
            GLES20.glVertexAttribPointer(
                    posParam, 4, GLES20.GL_FLOAT, false, 0, modelCoords);

            FloatBuffer modelColors = m.getModelColors();
            GLES20.glVertexAttribPointer(
                    colorParam, 4, GLES20.GL_FLOAT, false, 0, modelColors);

            FloatBuffer modelNormals = m.getModelNormals();
            GLES20.glVertexAttribPointer(
                    normalParam, 3, GLES20.GL_FLOAT, false, 0, modelNormals);

            GLES20.glEnableVertexAttribArray(posParam);
            GLES20.glEnableVertexAttribArray(colorParam);
            GLES20.glEnableVertexAttribArray(normalParam);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);

            GLES20.glDisableVertexAttribArray(posParam);
            GLES20.glDisableVertexAttribArray(colorParam);
            GLES20.glDisableVertexAttribArray(normalParam);

            checkGLError("Render Cube");
        }

        // Do the drawing. Make sure to check
        // eye.getType() to do eye-specific
        // behavior (e.g. Hemianopia, RVM)
    }

    @Override
    public void onFinishFrame(Viewport viewport) {}

    @Override
    public void onSurfaceChanged(int i, int i1) {}

    private void createShaders() {
        Shader vertAmbient = new Shader(
                GLES20.GL_VERTEX_SHADER, R.raw.diffuse, this);
        Shader fragSimple = new Shader(
                GLES20.GL_FRAGMENT_SHADER, R.raw.color_blindness, this);

        //TODO: maybe move some of this to a subclass?
        planeProgram = new ShaderProgram(vertAmbient, fragSimple);
        checkGLError("Plane program");
        planeProgram.addUniform("model");
        planeProgram.addUniform("view");
        planeProgram.addUniform("projection");
        planeProgram.addUniform("colorblind_mode");
        planeProgram.addAttribute("position");
        planeProgram.addAttribute("color");
        planeProgram.addAttribute("normal");
        checkGLError("Plane Params");
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        //Set a dark background
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f);

        //Also create the shaders
        createShaders();
    }

    @Override
    public void onRendererShutdown() {}

    @Override
    public void onCardboardTrigger() {
        colorBlindnessMode += 1;
        colorBlindnessMode %= NUM_COLOR_BLINDNESS_MODES;
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
