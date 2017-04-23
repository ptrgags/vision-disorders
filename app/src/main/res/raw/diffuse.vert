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

// Color after diffuse lighting
varying vec4 v_color;

void main() {
    //TODO: better shading
    //Light position in world space
    vec4 light_world = vec4(1.0, 2.0, 0.0, 1.0);

    //Convert everything to view space
    vec3 vertex_view = vec3(view * model * position);
    vec3 normal_view = vec3(view * model * vec4(normal, 0.0));
    vec3 light_view = vec3(view * light_world);

    float distance = length(light_view - vertex_view);
    vec3 light_dir = normalize(light_view - vertex_view);
    float diffuse = max(dot(normal_view, light_dir), 0.5);

    diffuse *= (1.0 / (1.0 + (0.00001 * distance * distance)));
    v_color = vec4(color.rgb * diffuse, color.a);

    /*
    mat4 MV = view * model;

    //Point light at the origin in world space.
    vec4 light_world = vec4(0.0, 0.0, 0.0, 1.0);
    float intensity = 10.0;

    //Convert everything to view space.
    vec3 vertex_view = vec3(MV * position);
    vec3 normal_view = vec3(MV * vec4(normal, 0.0));
    vec3 light_view = vec3(view * light_world);

    //Calculate lambert factor
    vec3 L = normalize(light_view - vertex_view);
    float cosine = dot(normal_view, L);
    float lambert = max(cosine, 0.0);

    //Calculate the luminosity
    float dist = length(light_view - vertex_view);
    float luminosity = clamp(1.0 / (dist * dist), 0.0, 1.0);

    //Calculate the final color. Make sure the opacity stays at 1.0
    v_color = color * lambert * luminosity * intensity;
    v_color.a = 1.0;
    */

    //Transform the position to clip space
    gl_Position = projection * view * model * position;
}
