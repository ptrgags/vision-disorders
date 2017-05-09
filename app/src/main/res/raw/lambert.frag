// Simple lambert shader
// Based on tutorial found here:
// http://www.learnopengles.com/android-lesson-three-moving-to-per-fragment-lighting/
//
// This will be the basis for other shaders such as the
// color blindness shader

precision mediump float;

// Position of the light in view space
uniform vec3 light_pos;

// Position in view space, interpolated for this fragment
varying vec3 v_position;
// Color, interpolated between vertices
varying vec4 v_color;
// Normal in view space, interpolated for this fragment
varying vec3 v_normal;

float diffuse_lighting() {
    float dist = length(light_pos - v_position);

    // Direction from surface to light
    vec3 L = normalize(light_pos - v_position);

    //Diffuse shading + attenuation
    float diffuse = max(dot(v_normal, L), 0.5);
    diffuse *= 1.0 / (1.0 + (0.00001 * dist * dist));

    return diffuse;
}

void main() {
    gl_FragColor = v_color * diffuse_lighting();
}