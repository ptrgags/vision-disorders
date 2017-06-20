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
    /** Context for when fetching resources */
    private Context context;
    /** Map of shader ID -> shader */
    private Map<String, Shader> shaderMap = new HashMap<>();

    /**
     * Constructor
     * @param context context for making the shader. This is needed
     *                when fetching resources
     */
    public ShaderMapBuilder(Context context) {
        this.context = context;
    }

    /**
     * Compile a shader and assign it a name
     * @param name id for the shader. Must begin with "vert_" or "frag_"
     * @param resId the resource id for the shader code.
     */
    public void makeShader(String name, int resId) {
        int shaderType = getShaderType(name);
        Shader shader = new Shader(shaderType, resId, context);
        shaderMap.put(name, shader);
    }

    /**
     * Given a shader id starting with "frag_" or "vert_", get the type of
     * shader.
     * @param name the name of the shader
     * @return the corresponding vertex/fragment shader type constant from
     *         GLES20
     * @throws RuntimeException when the id does not start with "frag_" or
     * "vert_"
     */
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
