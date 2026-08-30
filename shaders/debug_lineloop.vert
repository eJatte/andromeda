#version 460 core
layout (location = 0) in vec3 l_position;

out vec4 v_normalized_position;
out vec4 v_world_position;

uniform mat4x4 projection, view, model;

uniform float radius;
uniform bool screen_space = false;

void main() {

    if (screen_space) {
        vec4 w_position = model * vec4(0, 0, 0, 1.0);

        vec3 cameraRight = vec3(view[0][0], view[1][0], view[2][0]);
        vec3 cameraUp    = vec3(view[0][1], view[1][1], view[2][1]);

        vec3 vertex_w_pos = w_position.xyz + vec3(cameraRight * l_position.x * radius) + vec3(cameraUp * l_position.y * radius);

        vec4 normalized_position = projection * view * vec4(vertex_w_pos, 1);
        v_normalized_position = normalized_position;
        v_world_position = vec4(vertex_w_pos, 1);

        gl_Position = normalized_position;
    }
    else {
        vec4 w_position = model * vec4(l_position*radius, 1.0);
        vec4 normalized_position = projection * view * w_position;
        v_normalized_position = normalized_position;
        v_world_position = w_position;
        gl_Position = normalized_position;
    }
}