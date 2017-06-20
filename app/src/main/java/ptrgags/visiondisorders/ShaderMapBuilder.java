package ptrgags.visiondisorders;

import android.content.Context;
import android.opengl.GLES20;

import java.util.HashMap;
import java.util.Map;

/**
 * A ShaderMapBuilder builds a map of string identifiers to
 * compiled vertex/fragment shaders.
 */
public class ShaderMapBuilder {
    private Context context;
    private Map<String, Shader> shaderMap = new HashMap<>();

    public ShaderMapBuilder(Context context) {
        this.context = context;
    }

    public void makeShader(String name, int resId) {
        int shaderType = getShaderType(name);
        Shader shader = new Shader(shaderType, resId, context);
        shaderMap.put(name, shader);
    }

    private int getShaderType(String name) {
        if (name.startsWith("vert_"))
            return GLES20.GL_VERTEX_SHADER;
        else if (name.startsWith("frag_"))
            return GLES20.GL_FRAGMENT_SHADER;
        else
            throw new RuntimeException(
                    "Shaders must begin with 'vert_' or 'frag_'");
    }

    public Map<String, Shader> getShaderMap() {
        return shaderMap;
    }
}
