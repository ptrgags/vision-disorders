package ptrgags.visiondisorders;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.InputStream;
import java.util.Scanner;

/**
 * TODO: Document me!
 * Created by Peter on 4/20/2017.
 */

public class Shader {
    private int shaderHandle = 0;

    public Shader(int type, int resId, Context context) {
        shaderHandle = loadShader(type, resId, context);
    }

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
            shader = 0;
            throw new RuntimeException("Shader Error! See log.");
        } else {
            return shader;
        }
    }

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
