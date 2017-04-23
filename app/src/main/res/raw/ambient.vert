precision mediump float;
// Position in object space
attribute vec4 position;
// Color of the vertex
attribute vec4 color;
// Vertex normal
attribute vec3 normal;

//Object -> World space
uniform mat4 model;
// World -> View space
uniform mat4 view;
// View -> Clip space
uniform mat4 projection;

// Color after ambient lighting
varying vec4 v_color;

void main() {
    float ambientStrength = 0.8;

    v_color = ambientStrength * color;
    gl_Position = projection * view * model * position;
}