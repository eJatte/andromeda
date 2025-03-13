#version 460 core
layout (triangles) in;
layout (line_strip, max_vertices = 6) out;

in VS_OUT {
    vec3 normal;
} gs_in[];

out vec3 normal_test;

uniform mat4x4 projection, view, model;

void createLine(int index)
{
    normal_test = abs(gs_in[index].normal);
    gl_Position = projection * view * gl_in[index].gl_Position;
    EmitVertex();
    vec4 normalOffset = vec4(gs_in[index].normal, 0.0) * 0.1;
    normal_test = abs(gs_in[index].normal);
    gl_Position = projection * view * (gl_in[index].gl_Position + normalOffset);
    EmitVertex();
    EndPrimitive();
}

void main()
{
    createLine(0);
    createLine(1);
    createLine(2);
}