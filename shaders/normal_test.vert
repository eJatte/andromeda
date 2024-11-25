#version 460 core
layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal;
layout (location = 2) in vec2 uv;
layout (location = 3) in vec3 tangent;
layout (location = 4) in vec3 bitangent;

out vec3 vPosition;
out vec3 vNormal;
out vec2 vUv;
out vec3 vTangent;
out vec3 vBiTangent;

uniform mat4x4 projection, view, model;

void main()
{
    mat3 normal_matrix = transpose(inverse(mat3(model)));

    vec3 w_normal = normal_matrix * normal;
    vec3 w_tangent = normal_matrix * tangent;
    vec3 w_bitangent = normal_matrix * bitangent;
    vec4 w_position = model * vec4(position, 1.0f);

    vPosition = w_position.xyz;
    vNormal = w_normal;
    vUv = uv;
    vTangent = w_tangent;
    vBiTangent = w_bitangent;

    gl_Position = projection * view * w_position;
}