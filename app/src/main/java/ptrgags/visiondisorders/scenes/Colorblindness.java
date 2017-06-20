package ptrgags.visiondisorders.scenes;

import android.opengl.Matrix;

import com.google.vr.sdk.base.Eye;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ptrgags.visiondisorders.Camera;
import ptrgags.visiondisorders.Shader;
import ptrgags.visiondisorders.ShaderProgram;
import ptrgags.visiondisorders.models.Cube;
import ptrgags.visiondisorders.models.Model;

/**
 * Color blindness is the deficiency of color vision caused by one or more
 * types of malfunctioning cone cells.
 *
 * This simulation places the viewer in the center of an RGB cube made of
 * smaller cubes. Each variation changes the color filter on each cube to
 * simulate the lack of red, green or blue vision. The filters are applied at
 * the shader level of each cube.
 *
 * Variations:
 *
 * 0. Normal vision
 * 1. Protanopia (loss of red vision)
 * 2. Deuteranopia (loss of green vision)
 * 3. Tritanopia (loss of blue vision)
 */
public class Colorblindness extends Scene {
    /** Number of variations */
    private static final int NUM_COLOR_BLINDNESS_MODES = 5;
    /** position of the light in world space */
    private static final float[] LIGHT_POS = new float[] {
            10.0f, 1.0f, 0.0f, 0.0f
    };

    /** all the cubes to draw */
    private List<Model> cubes = new ArrayList<>();
    /** shader for rendering the cubes */
    private ShaderProgram program;
    /** camera for viewing the scene */
    private Camera camera;

    @Override
    public void initScene() {
        initCubes();

        camera = new Camera();
        camera.setPosition(0.0f, 0.0f, 0.1f);
        camera.setTarget(0.0f, 0.0f, 0.0f);
        camera.setUp(0.0f, 1.0f, 0.0f);
    }

    @Override
    public void onDraw(Eye eye) {
        super.onDraw(eye);

        //Get the projection matrix
        float[] projection = eye.getPerspective(0.1f, 100.0f);

        //Calculate the view matrix
        float[] cameraView = camera.getViewMatrix();
        float[] eyeView = eye.getEyeView();
        float[] view = new float[16];
        Matrix.multiplyMM(view, 0, eyeView, 0, cameraView, 0);

        // Set matrices and uniforms
        program.use();
        program.setUniformMatrix("projection", projection);
        program.setUniformMatrix("view", view);
        program.setUniform("colorblind_mode", mode);

        //Get the light position in view space
        float[] light_pos = new float[4];
        Matrix.multiplyMV(light_pos, 0, view, 0, LIGHT_POS, 0);
        program.setUniformVector("light_pos", light_pos);

        //Enable attribute buffers
        program.enableAttribute("position");
        program.enableAttribute("color");
        program.enableAttribute("normal");

        // Since all the cubes have the same vertices and normals, only load
        // them into the shader once
        Model firstCube = cubes.get(0);
        FloatBuffer modelCoords = firstCube.getModelCoords();
        program.setAttribute("position", modelCoords, 4);
        FloatBuffer modelNormals = firstCube.getModelNormals();
        program.setAttribute("normal", modelNormals, 3);

        // Render each cube. Only the color and position needs to change.
        for (Model m : cubes) {
            // Set the model matrix
            float[] model = m.getModelMatrix();
            program.setUniformMatrix("model", model);

            // Set the color attribute
            FloatBuffer modelColors = m.getModelColors();
            program.setAttribute("color", modelColors, 4);

            program.draw(m.getNumVertices());

            checkGLError("Render Cube");
        }

        // Disable the attribute buffers
        program.disableAttributes();
    }

    @Override
    public void initShaders(Map<String, Shader> shaders) {
        Shader vert = shaders.get("vert_lighting");
        Shader colorblind = shaders.get("frag_colorblindness");
        program = new ShaderProgram(vert, colorblind);
        checkGLError("Plane program");
    }

    /**
     * Build the RGB cube made out of RGB cubes. More specifically, we only
     * want the outermost part of the cube since the viewer is standing in
     * the middle of the cube.
     */
    private void initCubes() {
        //Radius in L-\infty space to be technicall
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

                    // Select the color. One corner is black, then the x
                    // direction is red, y blue, z red.
                    float[] color = new float[] {
                            (i + 2.0f) / 4.0f,
                            (j + 2.0f) / 4.0f,
                            (k + 2.0f) / 4.0f,
                            1
                    };

                    //Make the cube and translate it into place
                    Model cube = new Cube(color);
                    cube.translate(OFFSET * i, OFFSET * j, OFFSET * k);
                    cubes.add(cube);
                }
            }
        }
    }

    @Override
    public int getNumModes() {
        return NUM_COLOR_BLINDNESS_MODES;
    }
}
