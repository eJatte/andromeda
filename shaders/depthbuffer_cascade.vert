#version 460 core
layout (location = 0) in vec3 l_position;
layout (location = 2) in vec2 l_uv;

uniform mat4x4 model;

out VS_OUT {
    vec2 uv;
} vs_out;

void main() {
    vs_out.uv = l_uv;

    gl_Position = model * vec4(l_position, 1);
}