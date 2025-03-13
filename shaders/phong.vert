#version 460 core
layout (location = 0) in vec3 l_position;
layout (location = 1) in vec3 l_normal;
layout (location = 2) in vec2 l_uv;
layout (location = 3) in vec3 l_tangent;

out vec3 view_position;
out vec3 v_position;
out vec3 v_normal;
out vec2 v_uv;
out vec3 v_tangent;
out vec3 v_bitangent;
out mat3 m_tangent_to_world;

uniform mat4x4 projection, view, model;

void main() {
    vec4 w_position = model * vec4(l_position, 1.0);

    mat3x3 normal_matrix = transpose(inverse(mat3x3(model)));
    vec3 w_normal = normal_matrix * l_normal;
    vec3 w_tangent = normal_matrix * l_tangent;
    // gram schmidth
    w_tangent = normalize(w_tangent - dot(w_tangent, w_normal) * w_normal);
    vec3 w_bitangent = normalize(cross(w_normal, w_tangent));

    m_tangent_to_world = mat3(w_tangent, w_bitangent, w_normal);

    v_position = w_position.xyz;

    v_normal = w_normal;
    v_tangent = w_tangent;
    v_bitangent = w_bitangent;

    v_uv = l_uv;

    view_position = vec3(view * w_position);

    gl_Position = projection * view * w_position;
}