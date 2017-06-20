package ptrgags.visiondisorders;

import android.opengl.GLES20;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Document me!
 * Created by Peter on 4/20/2017.
 */

public class ShaderProgram {
    private int programHandle = -1;
    // Map of shader variable name -> param id
    private Map<String, Integer> uniforms = new HashMap<>();
    private Map<String, Integer> attributes = new HashMap<>();
    private List<Integer> enabledAttributes = new ArrayList<>();

    public ShaderProgram(Shader vert, Shader frag) {
        programHandle = makeProgram(vert, frag);
    }

    private int makeProgram(Shader vert, Shader frag) {
        //Create the program
        int program = GLES20.glCreateProgram();

        //Attach and link shaders
        GLES20.glAttachShader(program, vert.getShaderHandle());
        GLES20.glAttachShader(program, frag.getShaderHandle());
        GLES20.glLinkProgram(program);

        //Return the program handle
        return program;
    }

    /**
     * Attach a 4x4 float matrix to the shader program. This is the only type
     * of matrix used in the program.
     * @param name the variable name in the shader
     * @param matrix the matrix values
     */
    public void setUniformMatrix(String name, float[] matrix) {
        int varId = getUniform(name);
        GLES20.glUniformMatrix4fv(varId, 1, false, matrix, 0);
    }

    /**
     * Attach a 3-component float vector to the shader program.
     * @param name the variablee name
     * @param vector the components
     */
    public void setUniformVector(String name, float[] vector) {
        int varId = getUniform(name);
        GLES20.glUniform3fv(varId, 1, vector, 0);
    }

    /**
     * Attach a float to the shader program.
     * @param name The variable name in the shader
     * @param val the value of the variable
     */
    public void setUniform(String name, float val) {
        int varId = getUniform(name);
        GLES20.glUniform1f(varId, val);
    }

    /**
     * Attach an integer to the shader program
     * @param name the variable name
     * @param val the value of the variable
     */
    public void setUniform(String name, int val) {
        int varId = getUniform(name);
        GLES20.glUniform1i(varId, val);
    }

    /**
     * Get the location of the uniform variable in the shader program.
     * This is cached in a hashmap so glGetUniformLocation() is only
     * called the first time this function is callled.
     * @param name the name of the uniform variable in the shader
     * @return the location of the uniform variable.
     */
    public int getUniform(String name) {
        if (uniforms.containsKey(name))
            // Return the cached location
            return uniforms.get(name);
        else {
            // Fetch the location, cache it and return it.
            int param = GLES20.glGetUniformLocation(programHandle, name);
            uniforms.put(name, param);
            return param;
        }
    }

    /**
     * Get the location of the attribute variable in the shader program.
     * This is cached in a hashmap so glGetAttribLocation() is only
     * called the first time this function is called.
     * @param name the name of the attribute variable in the shader
     * @return the location of the attribute variable.
     */
    public int getAttribute(String name) {
        if (attributes.containsKey(name))
            // Return the cached position
            return attributes.get(name);
        else {
            //Fetch the attribute location, cache it and return it
            int param = GLES20.glGetAttribLocation(programHandle, name);
            attributes.put(name, param);
            return param;
        }
    }

    /**
     * Enable an attribute buffer for use and store its location in a
     * list that will be used for simple disabling all at once
     * @param name the name of the attribute
     */
    public void enableAttribute(String name) {
        int varId = getAttribute(name);
        enabledAttributes.add(varId);
        GLES20.glEnableVertexAttribArray(varId);
    }

    /**
     * Since we cached which attributes were enabled, we can disable them
     * all at once. This also clears the list of enabled attributes.
     */
    public void disableAttributes() {
        for (int id : enabledAttributes)
            GLES20.glDisableVertexAttribArray(id);
        enabledAttributes.clear();
    }

    /**
     * Load an attribute buffer
     * @param name the name of the attribute
     * @param vals a FloatBuffer with all the values
     * @param stride components per vector in the buffer
     */
    public void setAttribute(String name, FloatBuffer vals, int stride) {
        int varId = getAttribute(name);
        GLES20.glVertexAttribPointer(
                varId, stride, GLES20.GL_FLOAT, false, 0, vals);
    }

    /**
     * Use the shader program
     */
    public void use() {
        GLES20.glUseProgram(programHandle);
    }

    /**
     * Draw the faces specified in memory
     * @param numVertices the number of vertices to draw
     */
    public void draw(int numVertices) {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, numVertices);
    }
}
