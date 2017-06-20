// Simple vertex shader for use
// in diffuse shading.
// Based on tutorial found here:
// http://www.learnopengles.com/android-lesson-three-moving-to-per-fragment-lighting/

precision mediump float;

// Position in object space
attribute vec4 position;
// Color of the vertex
attribute vec4 color;
//Normal vector
attribute vec3 normal;

//Object -> World space
uniform mat4 model;
// World -> View space
uniform mat4 view;
// View -> Clip space
uniform mat4 projection;

// Position in view space
varying vec3 v_position;
// Pass the color through
varying vec4 v_color;
// Normal in view space
varying vec3 v_normal;

void main() {
    //Convert position and normal to view space
    v_position = vec3(view * model * position);
    v_normal = vec3(view * model * vec4(normal, 0.0));

    //Color passes through to the fragment shader
    v_color = color;

    // Calculate the position in clip space
    gl_Position = projection * view * model * position;
}