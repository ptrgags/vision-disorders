package ptrgags.visiondisorders;

import android.opengl.GLES10;
import android.opengl.GLES20;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Peter on 4/20/2017.
 */

class ShaderProgram {
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

        //Use this program so we can check for errors in the main app.
        //use();

        //Return the program handle
        return program;
    }

    /**
     * Specify a uniform variable present in the vertex/fragment shader
     * and add it to the internal map. this way we only have to do the
     * lookup once. Later lookups are done with getUniform()
     * @param name the name of the variable
     */
    public void addUniform(String name) {
        int param = GLES20.glGetUniformLocation(programHandle, name);
        uniforms.put(name, param);
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

    public int getUniform(String name) {
        return uniforms.get(name);
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
