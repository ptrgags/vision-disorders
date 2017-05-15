package ptrgags.visiondisorders;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.google.vr.sdk.base.Eye;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ptrgags.visiondisorders.models.Cube;
import ptrgags.visiondisorders.models.Model;

public class Colorblindness extends Scene {
    private static final int NUM_COLOR_BLINDNESS_MODES = 9;
    private static final float[] LIGHT_POS = new float[] {
            10.0f, 1.0f, 0.0f, 0.0f
    };

    private List<Model> cubes = new ArrayList<>();
    private ShaderProgram cubeProgram;
    private int colorBlindnessMode = 0;
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
        //Set drawing bits.
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        checkGLError("Color settings");

        //Get the projection matrix
        float[] projection = eye.getPerspective(0.1f, 100.0f);

        //Calculate the view matrix
        float[] cameraView = camera.getViewMatrix();
        float[] eyeView = eye.getEyeView();
        float[] view = new float[16];
        Matrix.multiplyMM(view, 0, eyeView, 0, cameraView, 0);

        cubeProgram.use();
        GLES20.glUniformMatrix4fv(
                cubeProgram.getUniform("projection"), 1, false, projection, 0);
        GLES20.glUniformMatrix4fv(
                cubeProgram.getUniform("view"), 1, false, view, 0);
        GLES20.glUniform1i(
                cubeProgram.getUniform("colorblind_mode"), colorBlindnessMode);

        //Get the light position in view space
        float[] light_pos = new float[4];
        Matrix.multiplyMV(light_pos, 0, view, 0, LIGHT_POS, 0);

        GLES20.glUniform3fv(
                cubeProgram.getUniform("light_pos"), 1, light_pos, 0);

        int modelParam = cubeProgram.getUniform("model");
        int posParam = cubeProgram.getAttribute("position");
        int colorParam = cubeProgram.getAttribute("color");
        int normalParam = cubeProgram.getAttribute("normal");

        //Enable all the attribute buffers
        GLES20.glEnableVertexAttribArray(posParam);
        GLES20.glEnableVertexAttribArray(colorParam);
        GLES20.glEnableVertexAttribArray(normalParam);

        // Since all the cubes have the same vertices and normals, only load
        // them into the shader once
        Model firstCube = cubes.get(0);
        FloatBuffer modelCoords = firstCube.getModelCoords();
        GLES20.glVertexAttribPointer(
                posParam, 4, GLES20.GL_FLOAT, false, 0, modelCoords);
        FloatBuffer modelNormals = firstCube.getModelNormals();
        GLES20.glVertexAttribPointer(
                normalParam, 3, GLES20.GL_FLOAT, false, 0, modelNormals);

        // Render each cube. Only the color and position needs to change.
        for (Model m : cubes) {
            float[] model = m.getModelMatrix();
            GLES20.glUniformMatrix4fv(modelParam, 1, false, model, 0);

            FloatBuffer modelColors = m.getModelColors();
            GLES20.glVertexAttribPointer(
                    colorParam, 4, GLES20.GL_FLOAT, false, 0, modelColors);

            //TODO: models should have a way to get the number of vertices
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);

            checkGLError("Render Cube");
        }

        // Disable the attribute buffers
        GLES20.glDisableVertexAttribArray(posParam);
        GLES20.glDisableVertexAttribArray(colorParam);
        GLES20.glDisableVertexAttribArray(normalParam);
    }

    @Override
    public void initShaders(Map<String, Shader> shaders) {
        Shader vert = shaders.get("vert_lighting");
        Shader colorblind = shaders.get("frag_colorblind");
        cubeProgram = new ShaderProgram(vert, colorblind);
        checkGLError("Plane program");
        cubeProgram.addUniform("model");
        cubeProgram.addUniform("view");
        cubeProgram.addUniform("projection");
        cubeProgram.addUniform("colorblind_mode");
        cubeProgram.addUniform("light_pos");
        cubeProgram.addAttribute("position");
        cubeProgram.addAttribute("color");
        cubeProgram.addAttribute("normal");
        checkGLError("Plane Params");
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
    public void next() {
        colorBlindnessMode++;
        colorBlindnessMode %= NUM_COLOR_BLINDNESS_MODES;
    }

    public void prev() {
        colorBlindnessMode--;
        colorBlindnessMode %= NUM_COLOR_BLINDNESS_MODES;

        // Because Java != Python :(
        if (colorBlindnessMode < 0)
            colorBlindnessMode += NUM_COLOR_BLINDNESS_MODES;
    }
}
