#version 460 core
layout (triangles, invocations = 3) in;
layout (triangle_strip, max_vertices = 3) out;

in VS_OUT {
    vec2 uv;
} gs_in[];

out vec2 v_uv;
flat out int layer;

void main()
{
    for (int i = 0; i < 3; i++) {
        gl_Position = gl_in[i].gl_Position + vec4(0.225f*gl_InvocationID,0,0,0);
        layer = gl_InvocationID;
        v_uv = gs_in[i].uv;
        EmitVertex();
    }
    EndPrimitive();
}