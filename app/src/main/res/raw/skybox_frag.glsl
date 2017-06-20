precision mediump float;

// The texture to use
uniform sampler2D texture;

// UV coordinates
varying vec2 v_uv;

void main() {
    // Just display the texture coloring, forget the ligthing for now
    gl_FragColor = texture2D(texture, v_uv);
}