package ptrgags.visiondisorders.scenes;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.google.vr.sdk.base.Eye;

import java.nio.FloatBuffer;
import java.util.Map;

import ptrgags.visiondisorders.Camera;
import ptrgags.visiondisorders.Shader;
import ptrgags.visiondisorders.ShaderProgram;
import ptrgags.visiondisorders.Texture;
import ptrgags.visiondisorders.models.Model;
import ptrgags.visiondisorders.models.Skybox;

/**
 * Created by Peter on 5/15/2017.
 */

public class Tetrachromacy extends Scene {
    private static final float[] MODE_COLORS = new float[] {
        8.0f,   // Normal color
        256.0f // Tetrachromacy: Much more color differentiation
    };

    private Camera camera;
    private ShaderProgram skyboxProgram;
    private Model skybox;
    private Texture skyboxTex;

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
        //Set drawing bits.
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        checkGLError("Color settings");

        //Get the projection matrix
        float[] projection = eye.getPerspective(25.0f, 100.0f);

        //Calculate the view matrix
        float[] cameraView = camera.getViewMatrix();
        float[] eyeView = eye.getEyeView();
        float[] view = new float[16];
        Matrix.multiplyMM(view, 0, eyeView, 0, cameraView, 0);

        skyboxProgram.use();
        GLES20.glUniformMatrix4fv(
                skyboxProgram.getUniform("projection"),
                1, false, projection, 0);
        GLES20.glUniformMatrix4fv(
                skyboxProgram.getUniform("view"), 1, false, view, 0);

        int modelParam = skyboxProgram.getUniform("model");
        int posParam = skyboxProgram.getAttribute("position");
        int uvParam = skyboxProgram.getAttribute("uv");

        //Enable all the attribute buffers
        GLES20.glEnableVertexAttribArray(posParam);
        GLES20.glEnableVertexAttribArray(uvParam);

        //Set the parameters
        FloatBuffer modelCoords = skybox.getModelCoords();
        GLES20.glVertexAttribPointer(
                posParam, 4, GLES20.GL_FLOAT, false, 0, modelCoords);
        FloatBuffer modelUV = skybox.getUVCoords();
        GLES20.glVertexAttribPointer(
                uvParam, 2, GLES20.GL_FLOAT, false, 0, modelUV);

        //Set the number of colors
        GLES20.glUniform1f(
                skyboxProgram.getUniform("num_colors"), MODE_COLORS[mode]);

        // load the model matrix into the GPU
        float[] model = skybox.getModelMatrix();
        GLES20.glUniformMatrix4fv(modelParam, 1, false, model, 0);

        //Load the texture
        int texParam = skyboxProgram.getUniform("texture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, skyboxTex.getTexHandle());
        GLES20.glUniform1i(texParam, 0);


        //TODO: models should have a way to get the number of vertices
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);

        checkGLError("Render Cube");

        // Disable the attribute buffers
        GLES20.glDisableVertexAttribArray(posParam);
        GLES20.glDisableVertexAttribArray(uvParam);
    }

    @Override
    public void initShaders(Map<String, Shader> shaders) {
        Shader vert = shaders.get("vert_skybox");
        Shader frag = shaders.get("frag_tetrachrome");
        skyboxProgram = new ShaderProgram(vert, frag);
        checkGLError("Plane program");
        skyboxProgram.addUniform("model");
        skyboxProgram.addUniform("view");
        skyboxProgram.addUniform("projection");
        skyboxProgram.addUniform("texture");
        skyboxProgram.addUniform("num_colors");
        skyboxProgram.addAttribute("position");
        skyboxProgram.addAttribute("uv");
        checkGLError("Program Params");
    }

    @Override
    public void initTextures(Map<String, Texture> textures) {
        skyboxTex = textures.get("colorful");
    }

    @Override
    public int getNumModes() {
        return MODE_COLORS.length;
    }
}
