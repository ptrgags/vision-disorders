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

/**
 * Created by Peter on 5/5/2017.
 */

public class Akinetopsia extends Scene {
    /** Normal vision, don't skip a frame */
    private static final int RATE_NORMAL = 1;
    /** Akinetopsia. Only refresh when frameCount is divisible by this number */
    private static final int RATE_AKINETOPSIA = 120;
    //Lookup table for modes
    private static final int[] RATES = new int[] {
            RATE_NORMAL,
            RATE_AKINETOPSIA
    };
    /** Number of layers of blocks. there are four blocks per layer */
    private static final int NUM_LAYERS = 10;
    // y value of the bottom layer
    private static final float MIN_Y = -5.0f;
    // distance between layers
    private static final float Y_OFFSET = 2.5f;
    // Half the thickness of a cube
    private static final float HALF_THICKNESS = 1.0f;
    // Distance from the origin to the face of each block in the xz-plane.
    // This is the same as the width of the biggest block in the z direction
    // when at the 0 position.
    private static final float DISTANCE = 10.0f;
    // distance from thee origin to the center of each block in the xz-plane
    private static final float RADIUS = DISTANCE + HALF_THICKNESS;
    /** Position of the light in world space */
    private static final float[] LIGHT_POS = new float[] {
            // close to the right wall and a little bit above the origin
            DISTANCE - 3.0f, 3.0f, 0, 0
    };

    private Camera camera;
    private ShaderProgram cubeProgram;
    private long frameCount = 0;
    private List<Model> blocks = new ArrayList<>();

    @Override
    public void initScene() {
        makeBlocks();

        camera = new Camera();
        camera.setPosition(0.0f, 0.0f, 0.1f);
        camera.setTarget(0.0f, 0.0f, 0.0f);
        camera.setUp(0.0f, 1.0f, 0.0f);
    }

    private void makeBlocks() {
        final float[] TURQUOISE = new float[] {0.0f, 0.5f, 1.0f, 1.0f};

        for (int layer = 0; layer < NUM_LAYERS; layer++) {
            // we'll be making four blocks, each rotating 90 degrees further
            // out of phase
            for (int theta = 0; theta < 360; theta += 90) {
                Model block = new Cube(TURQUOISE);

                // Stretch the cube in the z direction only, inversely
                // proportional to the layer
                float zScale = DISTANCE - layer;
                block.scale(1.0f, 1.0f, zScale);

                //Calculate the position keeping in mind the phase shift
                double thetaRadians = Math.toRadians(theta);
                float x = (float)(RADIUS * Math.cos(thetaRadians));
                float y = layer * Y_OFFSET + MIN_Y;
                float z = (float)(RADIUS * Math.sin(thetaRadians));
                block.translate(x, y, z);

                //Rotate the cube around the y axis
                block.rotate(theta, 0, 1, 0);

                //Store it for later
                blocks.add(block);
            }
        }

    }

    @Override
    public void onDraw(Eye eye) {
        // This skips rendering frames when in akinetopsia mode
        if (frameCount % RATES[mode] != 0)
            return;

        // The super call comes after the frame skip to make sure the screen
        // is not cleared.
        super.onDraw(eye);

        //Get the projection matrix
        float[] projection = eye.getPerspective(1.0f, 50.0f);

        //Calculate the view matrix
        float[] cameraView = camera.getViewMatrix();
        float[] eyeView = eye.getEyeView();
        float[] view = new float[16];
        Matrix.multiplyMM(view, 0, eyeView, 0, cameraView, 0);

        // Set the projection and view matricies
        cubeProgram.use();
        cubeProgram.setUniformMatrix("projection", projection);
        cubeProgram.setUniformMatrix("view", view);

        //Get the light position in view space
        float[] light_pos = new float[4];
        Matrix.multiplyMV(light_pos, 0, view, 0, LIGHT_POS, 0);
        cubeProgram.setUniformVector("light_pos", light_pos);

        //TODO: Remove me
        int posParam = cubeProgram.getAttribute("position");
        int colorParam = cubeProgram.getAttribute("color");
        int normalParam = cubeProgram.getAttribute("normal");

        //Enable all the attribute buffers
        //TODO: Simplify this
        GLES20.glEnableVertexAttribArray(posParam);
        GLES20.glEnableVertexAttribArray(colorParam);
        GLES20.glEnableVertexAttribArray(normalParam);

        // Since all the cubes have the same vertices and normals, only load
        // them into the shader once
        Model firstBlock = blocks.get(0);
        FloatBuffer modelCoords = firstBlock.getModelCoords();
        //TODO: simplify attributes
        GLES20.glVertexAttribPointer(
                posParam, 4, GLES20.GL_FLOAT, false, 0, modelCoords);
        FloatBuffer modelNormals = firstBlock.getModelNormals();
        GLES20.glVertexAttribPointer(
                normalParam, 3, GLES20.GL_FLOAT, false, 0, modelNormals);

        // Render each cube. Only the color and position needs to change.
        //TODO: simplify attributes
        for (Model block : blocks) {
            float[] model = block.getModelMatrix();
            cubeProgram.setUniformMatrix("model", model);

            FloatBuffer modelColors = block.getModelColors();
            GLES20.glVertexAttribPointer(
                    colorParam, 4, GLES20.GL_FLOAT, false, 0, modelColors);

            //TODO: models should have a way to get the number of vertices
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);

            checkGLError("Render Cube");
        }
        // Disable the attribute buffers
        //TODO: Simplify this
        GLES20.glDisableVertexAttribArray(posParam);
        GLES20.glDisableVertexAttribArray(colorParam);
        GLES20.glDisableVertexAttribArray(normalParam);
    }

    @Override
    public void initShaders(Map<String, Shader> shaders) {
        Shader lighting = shaders.get("vert_lighting");
        Shader lambert = shaders.get("frag_lambert");
        cubeProgram = new ShaderProgram(lighting, lambert);
        checkGLError("Plane program");
        cubeProgram.addAttribute("position");
        cubeProgram.addAttribute("color");
        cubeProgram.addAttribute("normal");
        checkGLError("Plane Params");
    }

    @Override
    public void onFrame() {
        frameCount++;
        rotateBlocks();
    }

    @Override
    public int getNumModes() {
        return RATES.length;
    }

    private void rotateBlocks() {
        final int BLOCKS_PER_LAYER = 4;
        // How many degrees/frame a layer rotates. Must be a factor of 90
        // so things will line up as best as possible.
        final int[] ROTATION_RATES = new int[] {
                1, 2, 3, 5, 6, 9, 10, 15, 18, 30, 45
        };
        final int QUARTER_TAU_DEGREES = 90;

        for (int i = 0; i < NUM_LAYERS; i++) {
            for (int j = 0; j < BLOCKS_PER_LAYER; j++) {

                Model block = blocks.get(i * BLOCKS_PER_LAYER + j);

                //Rotate the block a little more.
                int phaseShift = j * QUARTER_TAU_DEGREES;
                float angularVelocity = ROTATION_RATES[i] / 4.0f;
                float angle = frameCount * angularVelocity + phaseShift;
                block.rotate(-angularVelocity, 0, 1, 0);

                // Update the position based on the new angle
                double angleRadians = Math.toRadians(angle);
                float x = (float) (RADIUS * Math.cos(angleRadians));
                float y = i * Y_OFFSET + MIN_Y;
                float z = (float) (RADIUS * Math.sin(angleRadians));
                block.translateTo(x, y, z);
            }
        }
    }
}
