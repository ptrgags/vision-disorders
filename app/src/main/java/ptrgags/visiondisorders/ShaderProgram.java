package ptrgags.visiondisorders;

import android.opengl.GLES20;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Peter on 4/20/2017.
 */

public class ShaderProgram {
    private int programHandle = -1;
    // Map of shader variable name -> param id
    private Map<String, Integer> uniforms = new HashMap<>();
    private Map<String, Integer> attributes = new HashMap<>();

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
     * Specify an attribute variable present in the vertex shader
     * and add it to the internal map. this way we only have to do the
     * lookup once. Later lookups are done with getAttribute()
     * @param name the name of the variable
     */
    public void addAttribute(String name) {
        int param = GLES20.glGetAttribLocation(programHandle, name);
        attributes.put(name, param);
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

    public int getAttribute(String name) {
        return attributes.get(name);
    }

    public int getProgramHandle() {
        return programHandle;
    }

    public void use() {
        GLES20.glUseProgram(programHandle);
    }
}
