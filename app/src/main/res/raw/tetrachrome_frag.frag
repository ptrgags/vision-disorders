precision mediump float;

// The texture to use
uniform sampler2D texture;

// Set this to something low (e.g. 8) for the normal vision analogy and
// 256 for tetrachromacy
uniform float num_colors;

// UV coordinates
varying vec2 v_uv;

void main() {
    gl_FragColor = texture2D(texture, v_uv);

    // Change the color depth from 256 colors to num_colors. I will use this
    // for the tetrachromacy analogy
    gl_FragColor = floor(gl_FragColor * num_colors) / num_colors;
}