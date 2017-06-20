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
 * A small percentage of women have a fourth cone cell between red and green
 * which allows them to see many more colors than the rest of us.
 *
 * Since I can't simulate having extra cones, I decided to explain
 * tetrachromacy by analogy with color depth
 *
 * Go back in time to when there was only a few colors per red/green/blue
 * channel. it was possible to have pictures, but they seemed rather
 * posterized compared to the colorful images we have with 8+ bits per
 * color channel. The difference? We can see many times more shades of color.
 *
 * This simulation consists of a skybox shape but shaded with a custom
 * shader that draws a rainbow either at a low or high color depth.
 *
 * Variations:
 * 0. Reduced color-depth image as an analogy to regular trichromatic vision
 * 1. Full-color image as an analogy to tetrachromatic vision.
 */
public class Tetrachromacy extends Scene {
    /** colors for each mode */
    private static final float[] MODE_COLORS = new float[] {
        8.0f,   // Analogous to normal color
        256.0f // Tetrachromacy: Much more color differentiation
    };

    /** Camera for viewing the scene */
    private Camera camera;
    /** Tetrachromacy shader program */
    private ShaderProgram program;
    /** skybox that will be rendered with the custom shader */
    private Model skybox;
    /** some unit of time for animating the shader */
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
        program.use();
        program.setUniformMatrix("projection", projection);
        program.setUniformMatrix("view", view);

        //Enable all the attribute buffers
        program.enableAttribute("position");
        program.enableAttribute("uv");

        //Set the parameters
        FloatBuffer modelCoords = skybox.getModelCoords();
        program.setAttribute("position", modelCoords, 4);
        FloatBuffer modelUV = skybox.getUVCoords();
        program.setAttribute("uv", modelUV, 2);

        //Set the number of colors
        program.setUniform("num_colors", MODE_COLORS[mode]);
        program.setUniform("time", time);

        // load the model matrix into the GPU
        float[] model = skybox.getModelMatrix();
        program.setUniformMatrix("model", model);

        program.draw(skybox.getNumVertices());

        checkGLError("Render Skybox");

        // Disable the attribute buffers
        program.disableAttributes();
    }

    @Override
    public void initShaders(Map<String, Shader> shaders) {
        Shader vert = shaders.get("vert_uv");
        Shader frag = shaders.get("frag_tetrachromacy");
        program = new ShaderProgram(vert, frag);
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
