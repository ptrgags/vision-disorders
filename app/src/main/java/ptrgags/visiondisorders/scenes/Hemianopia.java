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
import ptrgags.visiondisorders.models.Plane;

/**
 * Hemianopia simulation.
 *
 * Hemianopia is a condition where a person loses vision in part of each eye.
 * This is caused by damage to the neural pathways between the eyes to the
 * occipital lobe at the back of the brain. Depending on what exactly is
 * damaged, different halves of the field of view are no longer visible.
 *
 * To simulate this, I render two scenes:
 * 1. blocks moving in a wave (just for something cool to look at) this is
 *    rendered in perspective as usual
 * 2. 0-4 occluders are rendered in front of the camera in orthographic
 *    projection. This blocks part of the screen.
 *
 * There are 7 variations of this simulation:
 *
 * 0. Normal vision mode
 * 1. Left homonymous hemianopia (occluded left side of the vision)
 * 2. Right homonymous hemianopia (occluded right side)
 * 3. Binasal hemianopia (occluded near the nose on both sides)
 * 4. Bitemporal hemianopia (peripheral vision occluded)
 * 5. Right superior quadrantanopia (top right quadrant occluded)
 * 6. Left inferior quadrantanopia (bottom left quadrant occluded)
 *
 * Note that this simulation is not 100% accurate due to the limited field
 * of view of the Google Cardboard. However, it gets the point across.
 */
public class Hemianopia extends Scene {
    /*
     * Possible occluder patterns for a 2x2 grid.
     * the order of the boolean flags is
     *
     * 1 3
     * 0 2
     *
     * when facing the occluders
     */
    private static final boolean[] OCCLUDE_NOTHING = new boolean[] {
            false, false, false, false};
    private static final boolean[] OCCLUDE_LEFT = new boolean[] {
            true, true, false, false};
    private static final boolean[] OCCLUDE_RIGHT = new boolean[] {
            false, false, true, true};
    private static final boolean[] OCCLUDE_SUPERIOR_RIGHT = new boolean[] {
            false, false, false, true};
    private static final boolean[] OCCLUDE_INFERIOR_LEFT = new boolean[] {
            true, false, false, false};

    /** Total number of variations */
    private static final int NUM_MODES = 7;

    /** Left eye occlusion in the various modes **/
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

    /** right eye occlusion in the various modes */
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

    /** How many blocks in each direction. */
    private static final int BLOCK_RADIUS = 9;
    /** maximum allowed amplitude for the wave */
    private static final int MAX_AMPLITUDE = 5;
    /** Distance between two blocks (center to center)*/
    private static final float BLOCK_OFFSET = 2.0f;

    /** Light position in world space */
    private static final float[] LIGHT_POS = new float[] {
            2.0f, 2.0f, 0.0f, 0.0f
    };

    /** The camera with which to view the scene */
    private Camera camera;
    /** Models of the blocks in the wave */
    private List<Model> blocks = new ArrayList<>();
    /** Models for the 4 occludeer quads */
    private List<Model> occluders = new ArrayList<>();
    /**
     * Shader program for rendering. this is shared between blocks and
     * occluders
     */
    private ShaderProgram program;
    /** the current frame number.*/
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

    /**
     * Even though each eye has four quadrants, we can get away with reusing
     * 4 black quads to occlude the quadrants. I call them "occluders"
     * They are arranged in a 2x2 grid around the z axis facing the camera.
     * The point where all the occluders touch is at the origin.
     */
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

    /**
     * This is a spatial wave that pulses out from the center of the grid.
     * This gives the height f(x, y, t)
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param time the frame count works fine here
     * @return the height of the wave at position (x, y) and time time
     */
    private float wave(float x, float y, float time) {
        // Euclidean distance
        double dist = Math.sqrt(x * x + y * y);
        double wave = MAX_AMPLITUDE * Math.sin(dist - 0.1 * time);
        return (float) wave;
    }

    /**
     * Make a grid of multicolored blocks araanged in a grid.
     * The heights of each block are arranged in a wave pattern.
     */
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
                block.scale(1, Math.abs(y_scale), 1);

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
        super.onDraw(eye);

        //Get the projection matrix
        float[] projection = eye.getPerspective(0.9f, 100.0f);

        //Calculate the view matrix
        float[] cameraView = camera.getViewMatrix();
        float[] eyeView = eye.getEyeView();
        float[] view = new float[16];
        Matrix.multiplyMM(view, 0, eyeView, 0, cameraView, 0);

        program.use();
        program.setUniformMatrix("projection", projection);
        program.setUniformMatrix("view", view);

        //Get the light position in view space
        float[] light_pos = new float[4];
        Matrix.multiplyMV(light_pos, 0, view, 0, LIGHT_POS, 0);
        program.setUniformVector("light_pos", light_pos);

        //Enable all the attribute buffers
        program.enableAttribute("position");
        program.enableAttribute("color");
        program.enableAttribute("normal");

        drawBlocks();

        // we no longer need the eyeView transformation
        program.setUniformMatrix("view", cameraView);

        drawOccluders(eye);

        // Disable the attribute buffers
        program.disableAttributes();

    }

    /**
     * Draw the block wave on the screen.
     *
     * Preconditions:
     * - the vertex buffers have been enabled
     */
    private void drawBlocks() {
        // Since all the cubes have the same vertices and normals, only load
        // them into the shader once
        Model firstBlock = blocks.get(0);
        FloatBuffer modelCoords = firstBlock.getModelCoords();
        program.setAttribute("position", modelCoords, 4);
        FloatBuffer modelNormals = firstBlock.getModelNormals();
        program.setAttribute("normal", modelNormals, 3);

        // Render each cube. Only the color and position needs to change.
        for (Model block : blocks) {
            // Set the model matrix
            float[] model = block.getModelMatrix();
            program.setUniformMatrix("model", model);

            // Set the vertex colors
            FloatBuffer modelColors = block.getModelColors();
            program.setAttribute("color", modelColors, 4);

            program.draw(block.getNumVertices());

            checkGLError("Render Cube");
        }
    }

    /**
     * For the hemianopia simulation, we need to occlude quadrants of the
     * screen, and it can differ from eye to eye. This is done by rendering
     * black planes in orthographic projection.
     *
     * Preconditions:
     * - drawblocks() has been called
     * - program.disableAttributes() has NOT been called
     * - the view matrix has been reset to just the camera view matrix.
     */
    private void drawOccluders(Eye eye) {
        // The occluders are rendered in orthographic projection
        float[] orthoProjection = new float[16];
        Matrix.orthoM(orthoProjection, 0, -1, 1, -1, 1, 0.1f, -3);

        //Update the matrices for ortho
        program.setUniformMatrix("projection", orthoProjection);

        // Since all the cubes have the same vertices and normals, only load
        // them into the shader once
        Model firstOcc = occluders.get(0);
        FloatBuffer occCoords = firstOcc.getModelCoords();
        program.setAttribute("position", occCoords, 4);
        FloatBuffer occNormals = firstOcc.getModelNormals();
        program.setAttribute("normal", occNormals, 3);

        // Determine which occluders to create.
        boolean[][] occluderFlags;
        if (eye.getType() == Eye.Type.LEFT)
            occluderFlags = OCCLUSION_LEFT_EYE;
        else
            occluderFlags = OCCLUSION_RIGHT_EYE;
        boolean[] modeFlags = occluderFlags[mode];

        // Render each Occluder. Only the color and position needs to change.
        for (int i = 0; i < occluders.size(); i++) {
            // Check if we should render this occluder
            if (!modeFlags[i])
                continue;

            Model occ = occluders.get(i);
            float[] model = occ.getModelMatrix();
            program.setUniformMatrix("model", model);

            FloatBuffer modelColors = occ.getModelColors();
            program.setAttribute("color", modelColors, 4);

            program.draw(occ.getNumVertices());

            checkGLError("Render Occluder");
        }
    }

    @Override
    public void initShaders(Map<String, Shader> shaders) {
        Shader vert = shaders.get("vert_lighting");
        Shader frag = shaders.get("frag_lambert");
        program = new ShaderProgram(vert, frag);
        checkGLError("Plane program");
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

    /**
     * Make the blocks move in a wave outwards from the center of the grid.
     * This has nothing to do with Hemianopia, it just looks cool.
     *
     * Each block is scaled and moved up and down.
     */
    private void moveBlocks() {
        final int BLOCKS_PER_ROW = 2 * BLOCK_RADIUS + 1;
        for (int i = -BLOCK_RADIUS; i <= BLOCK_RADIUS; i++) {
            for (int j = -BLOCK_RADIUS; j <= BLOCK_RADIUS; j++) {
                int row = i + BLOCK_RADIUS;
                int col = j + BLOCK_RADIUS;
                Model block = blocks.get(row * BLOCKS_PER_ROW + col);

                // Update the scale
                float y_scale = wave(i, j, frameCount) / 2.0f;
                block.scaleTo(1, Math.abs(y_scale), 1);

                // Update the position.
                float y_pos = y_scale / 2.0f - MAX_AMPLITUDE - 1.0f;
                block.translateTo(i * BLOCK_OFFSET, y_pos, j * BLOCK_OFFSET);
            }
        }
    }
}
