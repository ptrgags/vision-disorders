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

//TODO: Document me!
public class Colorblindness extends Scene {
    private static final int NUM_COLOR_BLINDNESS_MODES = 5;
    private static final float[] LIGHT_POS = new float[] {
            10.0f, 1.0f, 0.0f, 0.0f
    };

    private List<Model> cubes = new ArrayList<>();
    private ShaderProgram cubeProgram;
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
        cubeProgram.use();
        cubeProgram.setUniformMatrix("projection", projection);
        cubeProgram.setUniformMatrix("view", view);
        cubeProgram.setUniform("colorblind_mode", mode);

        //Get the light position in view space
        float[] light_pos = new float[4];
        Matrix.multiplyMV(light_pos, 0, view, 0, LIGHT_POS, 0);
        cubeProgram.setUniformVector("light_pos", light_pos);

        //Enable attribute buffers
        cubeProgram.enableAttribute("position");
        cubeProgram.enableAttribute("color");
        cubeProgram.enableAttribute("normal");

        // Since all the cubes have the same vertices and normals, only load
        // them into the shader once
        Model firstCube = cubes.get(0);
        FloatBuffer modelCoords = firstCube.getModelCoords();
        cubeProgram.setAttribute("position", modelCoords, 4);
        FloatBuffer modelNormals = firstCube.getModelNormals();
        cubeProgram.setAttribute("normal", modelNormals, 3);

        // Render each cube. Only the color and position needs to change.
        for (Model m : cubes) {
            // Set the model matrix
            float[] model = m.getModelMatrix();
            cubeProgram.setUniformMatrix("model", model);

            // Set the color attribute
            FloatBuffer modelColors = m.getModelColors();
            cubeProgram.setAttribute("color", modelColors, 4);

            cubeProgram.draw(m.getNumVertices());

            checkGLError("Render Cube");
        }

        // Disable the attribute buffers
        cubeProgram.disableAttributes();
    }

    @Override
    public void initShaders(Map<String, Shader> shaders) {
        Shader vert = shaders.get("vert_lighting");
        Shader colorblind = shaders.get("frag_colorblindness");
        cubeProgram = new ShaderProgram(vert, colorblind);
        checkGLError("Plane program");
    }

    private void initCubes() {
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
    public int getNumModes() {
        return NUM_COLOR_BLINDNESS_MODES;
    }
}
