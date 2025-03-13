#version 460 core
layout (location = 0) in vec3 l_position;

out vec3 v_position;

uniform mat4x4 projection, view, model;

void main() {
    vec4 w_position = model * vec4(l_position, 1.0);

    v_position = w_position.xyz;

    gl_Position = projection * view * w_position;
}