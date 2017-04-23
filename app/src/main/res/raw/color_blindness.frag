precision mediump float;
varying vec4 v_color;

/**
 * Corresponds to one of the vision modes
 * below.
 */
uniform int colorblind_mode;

/** Color modes */
const int MODE_NORMAL = 0;
const int MODE_PROTANOPIA = 1;
/**
const int MODE_PROTANOMALY = 2;
const int MODE_DEUTERANOPIA = 3;
const int MODE_DEUTERANOMALY = 4;
const int MODE_TRITANOPIA = 5;
*/

//Colorblind matrices ====================================
mat4 PROTANOPIA = mat4(
    0.567, 0.558, 0.0,   0.0,
    0.433, 0.442, 0.242, 0.0,
    0.0,   0.0,   0.758, 0.0,
    0.0,   0.0,   0.0,   1.0
);

mat4 TRITANOPIA = mat4(
    0.95, 0.0,   0.0,   0.0,
    0.05, 0.433, 0.475, 0.0,
    0.0,  0.567, 0.525, 0.0,
    0.0,  0.0,   0.0,   1.0
);

void main() {
    if (colorblind_mode == MODE_NORMAL)
        gl_FragColor = v_color;
    else if (colorblind_mode == MODE_PROTANOPIA)
        gl_FragColor = PROTANOPIA * v_color;
    else {
        //We have a problem make everything magenta as an error.
         gl_FragColor = vec4(1.0, 0.0, 1.0, 1.0);
    }
}
