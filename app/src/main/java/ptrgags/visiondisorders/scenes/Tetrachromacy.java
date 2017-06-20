package ptrgags.visiondisorders.scenes;

import android.opengl.Matrix;

import com.google.vr.sdk.base.Eye;

import java.nio.FloatBuffer;
import java.util.Map;

import ptrgags.visiondisorders.Camera;
import ptrgags.visiondisorders.Shader;
import ptrgags.visiondisorders.ShaderProgram;
import ptrgags.visiondisorders.models.Model;
import ptrgags.visiondisorders.models.Skybox;

/**
 * TODO: Document me!
 */

public class Tetrachromacy extends Scene {
    private static final float[] MODE_COLORS = new float[] {
        8.0f,   // Normal color
        256.0f // Tetrachromacy: Much more color differentiation
    };

    private Camera camera;
    private ShaderProgram skyboxProgram;
    private Model skybox;
    private float time = 0;

    @Override
    public void initScene() {
        skybox = new Skybox();
        skybox.scale(50.0f, 50.0f, 50.0f);

        camera = new Camera();
        camera.setPosition(0.0f, 0.0f, 0.1f);
        camera.setTarget(0.0f, 0.0f, 0.0f);
        camera.setUp(0.0f, 1.0f, 0.0f);
    }

    @Override
    public void onDraw(Eye eye) {
        super.onDraw(eye);

        //Get the projection matrix
        float[] projection = eye.getPerspective(25.0f, 100.0f);

        //Calculate the view matrix
        float[] cameraView = camera.getViewMatrix();
        float[] eyeView = eye.getEyeView();
        float[] view = new float[16];
        Matrix.multiplyMM(view, 0, eyeView, 0, cameraView, 0);

        // Set uniform matrices
        skyboxProgram.use();
        skyboxProgram.setUniformMatrix("projection", projection);
        skyboxProgram.setUniformMatrix("view", view);

        //Enable all the attribute buffers
        skyboxProgram.enableAttribute("position");
        skyboxProgram.enableAttribute("uv");

        //Set the parameters
        FloatBuffer modelCoords = skybox.getModelCoords();
        skyboxProgram.setAttribute("position", modelCoords, 4);
        FloatBuffer modelUV = skybox.getUVCoords();
        skyboxProgram.setAttribute("uv", modelUV, 2);

        //Set the number of colors
        skyboxProgram.setUniform("num_colors", MODE_COLORS[mode]);
        skyboxProgram.setUniform("time", time);

        // load the model matrix into the GPU
        float[] model = skybox.getModelMatrix();
        skyboxProgram.setUniformMatrix("model", model);

        skyboxProgram.draw(skybox.getNumVertices());

        checkGLError("Render Skybox");

        // Disable the attribute buffers
        skyboxProgram.disableAttributes();
    }

    @Override
    public void initShaders(Map<String, Shader> shaders) {
        Shader vert = shaders.get("vert_uv");
        Shader frag = shaders.get("frag_tetrachrome");
        skyboxProgram = new ShaderProgram(vert, frag);
        checkGLError("Plane program");
    }

    @Override
    public int getNumModes() {
        return MODE_COLORS.length;
    }

    @Override
    public void onFrame() {
        time += 0.02;
    }
}
