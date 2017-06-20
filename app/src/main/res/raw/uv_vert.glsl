precision mediump float;

// Position in object space
attribute vec4 position;
// UV coordinates of the vertex
attribute vec2 uv;

//Object -> World space
uniform mat4 model;
// World -> View space
uniform mat4 view;
// View -> Clip space
uniform mat4 projection;

// Interpolated UV coordinates
varying vec2 v_uv;

void main() {
    // Pass through UV coordinates
    v_uv = uv;

    // Calculate the position in clip space
    gl_Position = projection * view * model * position;
}