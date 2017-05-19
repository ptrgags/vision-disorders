precision mediump float;

// simulation / max simulations
uniform int num_simulations;
uniform int selected_simulation;

// variation / max variations
uniform int num_variations;
uniform int selected_variation;

// UV coordinates
varying vec2 v_uv;

// Thickness of the indicator bars
const float THICKNESS = 0.5;

// radius of each indicator circle.
const float CIRCLE_RADIUS = 0.02;

// Separation between circles
const float CIRCLE_SEP = 0.1;

// Colors for the indicator circles and the selected circles.
const vec4 COLOR_SELECTED = vec4(1.0, 0.5, 0.5, 0.75);
const vec4 COLOR_CIRCLE = vec4(1.0, 1.0, 1.0, 0.5);

/**
 * Draw circles for an indicator bar.
 *
 * @param selected index of the circle that is selected
 * @param max the max number of circles
 * @param origin the center of the first circle
 * @param sep vector that represents the offset to the next circle.
 */
vec4 draw_indicator_bar(int selected, int max, vec2 origin, vec2 sep) {
    vec4 color = vec4(0.0);
    int i = 0;
    while (i < max) {
        vec2 circle_pos = origin + float(i) * sep;
        float dist = distance(v_uv, circle_pos);
        if (dist < CIRCLE_RADIUS)
            color = (i == selected) ? COLOR_SELECTED : COLOR_CIRCLE;
        i++;
    }
    return color;
}

void main() {
    // Draw simulation indicator on the bottom of the screen
    vec2 sim_origin = vec2(
        0.5 - 0.5 * (float(num_simulations) - 1.0) * CIRCLE_SEP, THICKNESS / 2.0);
    vec2 sim_sep = vec2(CIRCLE_SEP, 0.0);
    vec4 sim_color = draw_indicator_bar(
        selected_simulation, num_simulations, sim_origin, sim_sep);

    // Draw variation indicator on the left of the screen
    vec2 var_origin = vec2(
        THICKNESS / 2.0, 0.5 - 0.5 * (float(num_variations) - 1.0) * CIRCLE_SEP);
    vec2 var_sep = vec2(0.0, CIRCLE_SEP);
    vec4 var_color = draw_indicator_bar(
        selected_variation, num_variations, var_origin, var_sep);

	gl_FragColor = sim_color + var_color;
}
