#version 460 core

out vec3 FragColor;

in vec2 v_uv;

uniform sampler2D shadow_map;

float linearize_depth(float d,float zNear,float zFar)
{
    return zNear * zFar / (zFar + d * (zNear - zFar));
}

void main()
{
    FragColor = vec3(linearize_depth(texture(shadow_map, v_uv).x, 0.1f, 100.0f)) * 2;
}