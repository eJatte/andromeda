#version 460 core
layout (location = 0) in vec3 l_position;
layout (location = 1) in vec3 l_normal;

out VS_OUT {
    vec3 normal;
} vs_out;

uniform mat4x4 projection, view, model;

void main() {
    vec4 w_position = model * vec4(l_position, 1.0);

    mat3x3 normal_matrix = transpose(inverse(mat3x3(model)));
    vec3 w_normal = normalize(normal_matrix * l_normal);

    gl_Position = w_position;
    vs_out.normal = w_normal;
}