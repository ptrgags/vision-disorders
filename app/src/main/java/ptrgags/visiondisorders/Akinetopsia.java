package ptrgags.visiondisorders;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.google.vr.sdk.base.Eye;

import java.nio.FloatBuffer;
import java.util.Map;

/**
 * Created by Peter on 5/5/2017.
 */

public class Akinetopsia extends Scene {
    /** Normal vision, don't skip a frame */
    private static final int RATE_NORMAL = 1;
    /** Akinetopsia. Only refresh when frameCount is divisible by this number */
    private static final int RATE_AKINETOPSIA = 60;

    private Camera camera;
    private ShaderProgram cubeProgram;
    private long frameCount = 0;
    private int akinetopsiaRate = RATE_NORMAL;

    //TODO: This will change to a list of cube
    private Model cube;

    @Override
    public void initScene() {
        float[] cubeColor = new float[] {0.0f, 0.5f, 1.0f, 1.0f};
        cube = new Cube(cubeColor);
        cube.scale(2.0f, 1.0f, 1.0f);

        camera = new Camera();
        camera.setPosition(0.0f, 0.0f, 0.1f);
        camera.setTarget(0.0f, 0.0f, 0.0f);
        camera.setUp(0.0f, 1.0f, 0.0f);
    }

    @Override
    public void onDraw(Eye eye) {
        // This skips rendering frames when in akinetopsia mode
        if (frameCount % akinetopsiaRate != 0)
            return;

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
        //TODO: Remove me
        GLES20.glUniform1i(
                cubeProgram.getUniform("colorblind_mode"), 0);

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
        FloatBuffer modelCoords = cube.getModelCoords();
        GLES20.glVertexAttribPointer(
                posParam, 4, GLES20.GL_FLOAT, false, 0, modelCoords);
        FloatBuffer modelNormals = cube.getModelNormals();
        GLES20.glVertexAttribPointer(
                normalParam, 3, GLES20.GL_FLOAT, false, 0, modelNormals);

        // Render each cube. Only the color and position needs to change.
        float[] model = cube.getModelMatrix();
        GLES20.glUniformMatrix4fv(modelParam, 1, false, model, 0);

        FloatBuffer modelColors = cube.getModelColors();
        GLES20.glVertexAttribPointer(
                colorParam, 4, GLES20.GL_FLOAT, false, 0, modelColors);

        //TODO: models should have a way to get the number of vertices
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);

        checkGLError("Render Cube");
    }

    @Override
    public void initShaders(Map<String, Shader> shaders) {
        Shader diffuse = shaders.get("vert_diffuse");
        Shader colorblind = shaders.get("frag_colorblind");
        cubeProgram = new ShaderProgram(diffuse, colorblind);
        checkGLError("Plane program");
        cubeProgram.addUniform("model");
        cubeProgram.addUniform("view");
        cubeProgram.addUniform("projection");
        cubeProgram.addUniform("colorblind_mode");
        cubeProgram.addAttribute("position");
        cubeProgram.addAttribute("color");
        cubeProgram.addAttribute("normal");
        checkGLError("Plane Params");
    }

    @Override
    public void onFrame() {
        frameCount++;

        //Oscillate the cube back and forth in the x direction
        double xPos = 15.0 * Math.sin(0.02 * frameCount);
        cube.translateTo((float)xPos, -1.0f, -10.0f);
    }

    @Override
    public void next() {
        if (akinetopsiaRate == RATE_NORMAL)
            akinetopsiaRate = RATE_AKINETOPSIA;
        else
            akinetopsiaRate = RATE_NORMAL;
    }
}
