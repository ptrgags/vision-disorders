package ptrgags.visiondisorders.scenes;

import android.opengl.GLES20;
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
import ptrgags.visiondisorders.models.Plane;

/**
 * Created by Peter on 5/6/2017.
 */

public class Hemianopia extends Scene {
    private static final boolean[] OCCLUDE_NOTHING = new boolean[] {false, false, false, false};
    private static final boolean[] OCCLUDE_LEFT = new boolean[] {true, true, false, false};
    private static final boolean[] OCCLUDE_RIGHT = new boolean[] {false, false, true, true};
    private static final boolean[] OCCLUDE_SUPERIOR_RIGHT = new boolean[] {false, false, false, true};
    private static final boolean[] OCCLUDE_INFERIOR_LEFT = new boolean[] {true, false, false, false};

    private static final int NUM_MODES = 7;

    //Left eye occlusion in the various modes
    private static final boolean[][] OCCLUSION_LEFT_EYE = new boolean[][] {
            //Normal mode
            OCCLUDE_NOTHING,
            //Left homonymous,
            OCCLUDE_LEFT,
            //Right homonymous,
            OCCLUDE_RIGHT,
            //Binasal
            OCCLUDE_RIGHT,
            //Bitemporal
            OCCLUDE_LEFT,
            //superior right
            OCCLUDE_SUPERIOR_RIGHT,
            //Inferior leftt
            OCCLUDE_INFERIOR_LEFT
    };

    //right eye occlusion in the various modes
    private static final boolean[][] OCCLUSION_RIGHT_EYE = new boolean[][] {
            //Normal mode
            OCCLUDE_NOTHING,
            //Left homonymous,
            OCCLUDE_LEFT,
            //Right homonymous,
            OCCLUDE_RIGHT,
            //Binasal
            OCCLUDE_LEFT,
            //Bitemporal
            OCCLUDE_RIGHT,
            //superior right
            OCCLUDE_SUPERIOR_RIGHT,
            //Inferior leftt
            OCCLUDE_INFERIOR_LEFT
    };

    //How many blocks in each direction.
    private static final int BLOCK_RADIUS = 9;
    // maximum allowed amplitude for the wave
    private static final int MAX_AMPLITUDE = 5;
    // Distance between two blocks (center to center)
    private static final float BLOCK_OFFSET = 2.0f;

    // Light position in world space
    private static final float[] LIGHT_POS = new float[] {
            2.0f, 2.0f, 0.0f, 0.0f
    };

    private Camera camera;
    private List<Model> blocks = new ArrayList<>();
    private List<Model> occluders = new ArrayList<>();
    private ShaderProgram blockProgram;
    private int frameCount = 0;

    @Override
    public void initScene() {
        buildBlocks();
        buildOccluders();

        camera = new Camera();
        camera.setPosition(0.0f, 0.0f, 0.1f);
        camera.setTarget(0.0f, 0.0f, 0.0f);
        camera.setUp(0.0f, 1.0f, 0.0f);
    }

    private void buildOccluders() {
        final float[] OFFSETS = new float[] {-0.5f, 0.5f};
        final float[] BLACK = new float[] {0, 0, 0, 1};
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                Model occluder = new Plane(BLACK);

                //Scale the occluder
                occluder.scale(0.5f, 1, 0.5f);

                //Position it
                float x = OFFSETS[i];
                float y = OFFSETS[j];
                occluder.translate(x, y, 0);

                //Rotate it so it's facing the camera
                occluder.rotate(90, 1, 0, 0);

                //Add it to the list
                occluders.add(occluder);
            }
        }
    }

    private float wave(float x, float y, float time) {
        // Euclidean distance
        double dist = Math.sqrt(x * x + y * y);
        double wave = MAX_AMPLITUDE * Math.sin(dist - 0.1 * time);
        return (float) wave;
    }

    private void buildBlocks() {
        final float[][] COLORS = new float[][] {
                {1, 0, 0, 1}, // red
                {0.5f, 0, 1, 1}, // purple
                {1, 0.5f, 0, 1}, // orange
                {0, 0, 1, 1}, // blue
                {0, 1, 0, 1} // green
        };

        for (int i = -BLOCK_RADIUS; i <= BLOCK_RADIUS; i++) {
            for (int j = -BLOCK_RADIUS; j <= BLOCK_RADIUS; j++) {
                // Pick a color
                int manhattan = Math.abs(i) + Math.abs(j);
                float[] color = COLORS[manhattan % COLORS.length];
                Model block = new Cube(color);

                // Scale the blocks vertically only. Note that we scale
                // half the height since the cube goes from -1 to 1 instead
                // of 0 to 1 in each direction
                float y_scale = wave(i, j, 0) / 2.0f;
                block.scale(1, (float) Math.abs(y_scale), 1);

                //Translate the block to the place in the grid. adjust the
                //height so the bottom of the blocks line up at the bottom.
                float y_pos = y_scale / 2.0f - MAX_AMPLITUDE - 1.0f;
                block.translate(i * BLOCK_OFFSET, y_pos, j * BLOCK_OFFSET);

                //Save the block for later
                blocks.add(block);
            }
        }
    }

    @Override
    public void onDraw(Eye eye) {
        //Set drawing bits.
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        checkGLError("Color settings");

        //Get the projection matrix
        float[] projection = eye.getPerspective(0.9f, 100.0f);

        //Calculate the view matrix
        float[] cameraView = camera.getViewMatrix();
        float[] eyeView = eye.getEyeView();
        float[] view = new float[16];
        Matrix.multiplyMM(view, 0, eyeView, 0, cameraView, 0);

        blockProgram.use();
        GLES20.glUniformMatrix4fv(
                blockProgram.getUniform("projection"), 1, false, projection, 0);
        GLES20.glUniformMatrix4fv(
                blockProgram.getUniform("view"), 1, false, view, 0);

        //Get the light position in view space
        float[] light_pos = new float[4];
        Matrix.multiplyMV(light_pos, 0, view, 0, LIGHT_POS, 0);

        GLES20.glUniform3fv(
                blockProgram.getUniform("light_pos"), 1, light_pos, 0);

        int modelParam = blockProgram.getUniform("model");
        int posParam = blockProgram.getAttribute("position");
        int colorParam = blockProgram.getAttribute("color");
        int normalParam = blockProgram.getAttribute("normal");

        //Enable all the attribute buffers
        GLES20.glEnableVertexAttribArray(posParam);
        GLES20.glEnableVertexAttribArray(colorParam);
        GLES20.glEnableVertexAttribArray(normalParam);

        // Since all the cubes have the same vertices and normals, only load
        // them into the shader once
        Model firstBlock = blocks.get(0);
        FloatBuffer modelCoords = firstBlock.getModelCoords();
        GLES20.glVertexAttribPointer(
                posParam, 4, GLES20.GL_FLOAT, false, 0, modelCoords);
        FloatBuffer modelNormals = firstBlock.getModelNormals();
        GLES20.glVertexAttribPointer(
                normalParam, 3, GLES20.GL_FLOAT, false, 0, modelNormals);

        // Render each cube. Only the color and position needs to change.
        for (Model block : blocks) {
            float[] model = block.getModelMatrix();
            GLES20.glUniformMatrix4fv(modelParam, 1, false, model, 0);

            FloatBuffer modelColors = block.getModelColors();
            GLES20.glVertexAttribPointer(
                    colorParam, 4, GLES20.GL_FLOAT, false, 0, modelColors);

            //TODO: models should have a way to get the number of vertices
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);

            checkGLError("Render Cube");
        }

        float[] orthoProjection = new float[16];
        Matrix.orthoM(orthoProjection, 0, -1, 1, -1, 1, 0.1f, -3);

        //Update the matrices for ortho
        GLES20.glUniformMatrix4fv(
                blockProgram.getUniform("projection"), 1, false, orthoProjection, 0);
        GLES20.glUniformMatrix4fv(
                blockProgram.getUniform("view"), 1, false, cameraView, 0);

        // Since all the cubes have the same vertices and normals, only load
        // them into the shader once
        Model firstOccluder = occluders.get(0);
        FloatBuffer occCoords = firstBlock.getModelCoords();
        GLES20.glVertexAttribPointer(
                posParam, 4, GLES20.GL_FLOAT, false, 0, occCoords);
        FloatBuffer occNormals = firstBlock.getModelNormals();
        GLES20.glVertexAttribPointer(
                normalParam, 3, GLES20.GL_FLOAT, false, 0, occNormals);

        boolean[][] occluderFlags = (eye.getType() == Eye.Type.LEFT) ? OCCLUSION_LEFT_EYE : OCCLUSION_RIGHT_EYE;
        boolean[] modeFlags = occluderFlags[mode];

        // Render each Occluder. Only the color and position needs to change.
        for (int i = 0; i < occluders.size(); i++) {
            if (!modeFlags[i])
                continue;

            Model occ = occluders.get(i);
            float[] model = occ.getModelMatrix();
            GLES20.glUniformMatrix4fv(modelParam, 1, false, model, 0);

            FloatBuffer modelColors = occ.getModelColors();
            GLES20.glVertexAttribPointer(
                    colorParam, 4, GLES20.GL_FLOAT, false, 0, modelColors);

            //TODO: models should have a way to get the number of vertices
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

            checkGLError("Render Occluder");
        }

        // Disable the attribute buffers
        GLES20.glDisableVertexAttribArray(posParam);
        GLES20.glDisableVertexAttribArray(colorParam);
        GLES20.glDisableVertexAttribArray(normalParam);

    }

    @Override
    public void initShaders(Map<String, Shader> shaders) {
        Shader vert = shaders.get("vert_lighting");
        Shader frag = shaders.get("frag_lambert");
        blockProgram = new ShaderProgram(vert, frag);
        checkGLError("Plane program");
        blockProgram.addUniform("model");
        blockProgram.addUniform("view");
        blockProgram.addUniform("projection");
        blockProgram.addUniform("light_pos");
        blockProgram.addAttribute("position");
        blockProgram.addAttribute("color");
        blockProgram.addAttribute("normal");
        checkGLError("Program Params");
    }

    @Override
    public void onFrame() {
        frameCount++;
        moveBlocks();
    }

    @Override
    public int getNumModes() {
        return NUM_MODES;
    }

    private void moveBlocks() {
        final int BLOCKS_PER_ROW = 2 * BLOCK_RADIUS + 1;
        for (int i = -BLOCK_RADIUS; i <= BLOCK_RADIUS; i++) {
            for (int j = -BLOCK_RADIUS; j <= BLOCK_RADIUS; j++) {
                int row = i + BLOCK_RADIUS;
                int col = j + BLOCK_RADIUS;
                Model block = blocks.get(row * BLOCKS_PER_ROW + col);

                // Update the scale
                float y_scale = wave(i, j, frameCount) / 2.0f;
                block.scaleTo(1, (float) Math.abs(y_scale), 1);

                // Update the position.
                float y_pos = y_scale / 2.0f - MAX_AMPLITUDE - 1.0f;
                block.translateTo(i * BLOCK_OFFSET, y_pos, j * BLOCK_OFFSET);
            }
        }
    }
}
