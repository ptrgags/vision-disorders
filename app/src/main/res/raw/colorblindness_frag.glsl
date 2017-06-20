precision mediump float;

// Position of the light in view space
uniform vec3 light_pos;

// Position in view space, interpolated for this fragment
varying vec3 v_position;
// Color, interpolated between vertices
varying vec4 v_color;
// Normal in view space, interpolated for this fragment
varying vec3 v_normal;

/**
 * Corresponds to one of the vision modes
 * below.
 */
uniform int colorblind_mode;

/** Color modes */
const int MODE_NORMAL = 0;
const int MODE_PROTANOPIA = 1;
const int MODE_DEUTERANOPIA = 2;
const int MODE_TRITANOPIA = 3;
const int MODE_ACHROMATOPSIA = 4;

//These ones didn't look so great so I'm excluding them in my demo but
//leaving them in the shader for posterity.
const int MODE_PROTANOMALY = 5;
const int MODE_DEUTERANOMALY = 6;
const int MODE_TRITANOMALY = 7;
const int MODE_ACHROMATOMALY = 8;

//Colorblind matrices ====================================
//These are translated from the JavaScript code from:
//http://web.archive.org/web/20081014161121/http://www.colorjack.com/labs/colormatrix/

//Deficiency in red vision
mat4 PROTANOMALY = mat4(
    0.817, 0.333, 0.0,   0.0,
    0.183, 0.667, 0.125, 0.0,
    0.0,   0.0,   0.875, 0.0,
    0.0,   0.0,   0.0,   1.0
);

// Loss of red vision
mat4 PROTANOPIA = mat4(
    0.567, 0.558, 0.0,   0.0,
    0.433, 0.442, 0.242, 0.0,
    0.0,   0.0,   0.758, 0.0,
    0.0,   0.0,   0.0,   1.0
);

//Deficiency in green vision
mat4 DEUTERANOMALY = mat4(
    0.8, 0.258, 0.0,   0.0,
    0.2, 0.742, 0.142, 0.0,
    0.0, 0.0,   0.858, 0.0,
    0.0, 0.0,   0.0,   1.0
);

//Loss of green vision
mat4 DEUTERANOPIA = mat4(
    0.625, 0.7, 0.0, 0.0,
    0.375, 0.3, 0.3, 0.0,
    0.0,   0.0, 0.7, 0.0,
    0.0,   0.0, 0.0, 1.0
);

//Deficiency in blue vision
mat4 TRITANOMALY = mat4(
    0.967, 0.0,   0.0,   0.0,
    0.033, 0.733, 0.183, 0.0,
    0.0,   0.267, 0.817, 0.0,
    0.0,   0.0,   0.0,   1.0
);

//Loss of blue vision
mat4 TRITANOPIA = mat4(
    0.95, 0.0,   0.0,   0.0,
    0.05, 0.433, 0.475, 0.0,
    0.0,  0.567, 0.525, 0.0,
    0.0,  0.0,   0.0,   1.0
);

//Deficiency in all colors
mat4 ACHROMATOMALY = mat4(
    0.618, 0.163, 0.163, 0.0,
    0.320, 0.775, 0.320, 0.0,
    0.062, 0.062, 0.516, 0.0,
    0.0,   0.0,   0.0,   1.0
);

//Loss of color
mat4 ACHROMATOPSIA = mat4(
    0.299, 0.299, 0.299, 0.0,
    0.587, 0.587, 0.597, 0.0,
    0.114, 0.114, 0.114, 0.0,
    0.0,   0.0,   0.0,   1.0
);

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
    //OpenGL ES doesn't support switch statements :(
    if (colorblind_mode == MODE_NORMAL)
        gl_FragColor = v_color;
    else if (colorblind_mode == MODE_PROTANOMALY)
        gl_FragColor = PROTANOMALY * v_color;
    else if (colorblind_mode == MODE_PROTANOPIA)
        gl_FragColor = PROTANOPIA * v_color;
    else if (colorblind_mode == MODE_DEUTERANOMALY)
        gl_FragColor = DEUTERANOMALY * v_color;
    else if (colorblind_mode == MODE_DEUTERANOPIA)
        gl_FragColor = DEUTERANOPIA * v_color;
    else if (colorblind_mode == MODE_TRITANOMALY)
        gl_FragColor = TRITANOMALY * v_color;
    else if (colorblind_mode == MODE_TRITANOPIA)
        gl_FragColor = TRITANOPIA * v_color;
    else if (colorblind_mode == MODE_ACHROMATOMALY)
        gl_FragColor = ACHROMATOMALY * v_color;
    else if (colorblind_mode == MODE_ACHROMATOPSIA)
        gl_FragColor = ACHROMATOPSIA * v_color;
    else
        //We have a problem, make everything magenta as an error.
         gl_FragColor = vec4(1.0, 0.0, 1.0, 1.0);

    //Apply lambert_frag shading
    gl_FragColor = gl_FragColor * diffuse_lighting();
}
