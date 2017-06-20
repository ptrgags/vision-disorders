precision mediump float;

// Position in object space
attribute vec4 position;
// UV coordinates for the vertex
attribute vec2 uv;

//Object -> World space
uniform mat4 model;
// World -> View space
uniform mat4 view;
// View -> Clip space
uniform mat4 projection;

// Pass through the UV coordinates so it gets interpolated
varying vec2 v_uv;

void main() {
    // Pass through UV coordinates
    v_uv = uv;

    // Flip the texture coords in the y direction. I had forgotten that
    // OpenGL reads textures upside down.
    v_uv.y = 1.0 - v_uv.y;

    // Caclulate the position
    gl_Position = projection * view * model * position;
}
