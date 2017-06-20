package ptrgags.visiondisorders;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Facade for compiling shaders more easily.
 */
public class Shader {
    /** shader ID in OpenGL */
    private int shaderHandle = 0;

    /**
     * Constructor
     * @param type the type of shader (GLES20.FRAGMENT_SHADER or
     *             GLES20.VERTEX_SHADER)
     * @param resId the resource ID of the shader code
     * @param context the Context for reading the text file.
     */
    public Shader(int type, int resId, Context context) {
        shaderHandle = loadShader(type, resId, context);
    }

    /**
     * Load and compile a shader
     * @param type the type of shader
     * @param resId the resource ID of the shader
     * @param context the Context for fetching resources
     * @return the ID of the compiled shader if successful
     * @throws RuntimeException on failure
     */
    private int loadShader(int type, int resId, Context context) {
        //Slurp the shader code
        String code = readTextFile(resId, context);

        //Compile the shader
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        // See if we compiled successfully
        int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(
                shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // Delete the shader on failure and raise an exception
        if (compileStatus[0] == 0) {
            String err = GLES20.glGetShaderInfoLog(shader);
            Log.e("VisionDisorders", "Shader Error: " + err);
            GLES20.glDeleteShader(shader);
            throw new RuntimeException("Shader Error! See log.");
        } else {
            return shader;
        }
    }

    /**
     * Slurp a text file as a string
     * @param resId the resource ID
     * @param context the Context
     * @return the contents of the text file.
     */
    private String readTextFile(int resId, Context context) {
        InputStream inStream = context.getResources().openRawResource(resId);
        Scanner scanner = new Scanner(inStream);
        StringBuilder builder = new StringBuilder();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            builder.append(line);
            builder.append("\n");
        }
        scanner.close();
        return builder.toString();
    }

    public int getShaderHandle() {
        return shaderHandle;
    }
}
