package ptrgags.visiondisorders;

import android.opengl.GLES20;
import android.os.Bundle;
import android.util.Log;

import com.google.vr.ndk.base.AndroidCompat;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;

public class MainActivity extends GvrActivity implements GvrView.StereoRenderer {

    /** floor, walls and ceiling */
    private List<Shape3D> room = new ArrayList<>();

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
        Shape3D floor = new Plane();
        floor.scale(5.0f, 1.0f, 5.0f);
        floor.translate(0.0f, -1.0f, 0.0f);
        room.add(floor);
        Shape3D wall = new Plane();
        wall.scale(5.0f, 1.0f, 5.0f);
        wall.rotate(90, 0.0f, 0.0f, 1.0f);
        wall.translate(2.5f, 2.5f, 0.0f);
        room.add(wall);
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
        //Update anything that needs to move
        //Handle head rotation and update
        //the camera
    }

    @Override
    public void onDrawEye(Eye eye) {
        //Set drawing bits.
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

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
        ShaderProgram planeProgram = new ShaderProgram(vertAmbient, fragSimple);
        planeProgram.addUniform("model");
        planeProgram.addUniform("view");
        planeProgram.addUniform("projection");
        planeProgram.addAttribute("position");
        planeProgram.addAttribute("color");
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
        while (error != GLES20.GL_NO_ERROR) {
            String message = String.format("%s: glError %s", label, error);
            Log.e("VisualDisorders", message);
            throw new RuntimeException(message);
        }
    }
}
