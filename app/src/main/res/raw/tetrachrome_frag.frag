precision mediump float;
#define PI 3.1415

// Set this to something low (e.g. 8) for the normal vision analogy and
// 256 for tetrachromacy
uniform float num_colors;

// Current frame number.
uniform float time;

// UV coordinates
varying vec2 v_uv;

/**
 * Convert from HSV back to RGB.
 * Yes, this isn't the most efficient way, but this
 * shader isn't that involved.
 */
vec3 hsv2rgb(vec3 hsv) {
    float h = hsv.x / 60.0;
    float s = hsv.y;
    float v = hsv.z;

    float chroma = s * v;
    float x = chroma * (1.0 - abs(mod(h, 2.0) - 1.0));

    vec3 rgb;
    if (h <= 1.0)
        rgb = vec3(chroma, x, 0.0);
   	else if (h <= 2.0)
        rgb = vec3(x, chroma, 0.0);
    else if (h <= 3.0)
        rgb = vec3(0, chroma, x);
    else if (h <= 4.0)
        rgb = vec3(0, x, chroma);
    else if (h <= 5.0)
        rgb = vec3(x, 0, chroma);
    else
        rgb = vec3(chroma, 0, x);

    float m = v - chroma;

    return rgb + m;
}

// Calculate the spiral colors.
// UV should be calculated relative to the center of one of the
// skybox sides.
vec4 spiral(vec2 uv) {
    //Convert from rectangular to polar. Theta goes from 0 to 1
    float r = length(uv);
    float theta = 0.5 + 0.5 * (atan(uv.y, uv.x) / PI);

    //Twist the colors as we move outwards
    float twist = 3.0 * sin(time);
    float f = theta + twist * r;

    // Calculate the hue from the normalized f value
    const float NUM_CYCLES = 1.0;
    const float CYCLE = 360.0;
    float hue = mod(f * CYCLE * NUM_CYCLES, CYCLE);

    // Convert to RGB
    vec3 hsv = vec3(hue, 1.0, 0.8);
    vec3 rgb = hsv2rgb(hsv);
    return vec4(rgb, 1.0);

}

vec4 skybox(vec2 uv, vec2 box_uv) {
    // Front face
    if (0.25 <= uv.x && uv.x <= 0.5 && 0.75 <= uv.y && uv.y <= 1.0)
        return spiral(box_uv);
    // Bottom face
    else if (0.25 <= uv.x && uv.x <= 0.5 && 0.5 <= uv.y && uv.y <= 0.75)
        return spiral(vec2(box_uv.x, -0.5));
    // Back face
    else if (0.25 <= uv.x && uv.x <= 0.5 && 0.25 <= uv.y && uv.y <= 0.5)
        return spiral(vec2(box_uv.x, - box_uv.y));
    // Left face
    else if (0.0 <= uv.x && uv.x <= 0.25 && 0.5 <= uv.y && uv.y <= 0.75)
        return spiral(vec2(-0.5, -box_uv.x));
    // Right face
    else if (0.5 <= uv.x && uv.x <= 0.75 && 0.5 <= uv.y && uv.y <= 0.75)
        return spiral(vec2(0.5, box_uv.x));
    // Top face
    else if (0.25 <= uv.x && uv.x <= 0.5 && 0.0 <= uv.y && uv.y <= 0.25)
        return spiral(vec2(box_uv.x, 0.5));
    else
        return vec4(0.0);
}

void main() {
    //Divide into 16 boxes and move the origin to the center of each box
    vec2 box_uv = fract(4.0 * v_uv) - 0.5;

    // Calculate the spiral skybox
    gl_FragColor = skybox(v_uv, box_uv);

    // Change the color depth from 256 colors to num_colors. I will use this
    // for the tetrachromacy analogy
    gl_FragColor = floor(gl_FragColor * num_colors) / num_colors;
}