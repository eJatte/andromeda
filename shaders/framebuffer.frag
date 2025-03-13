#version 460 core

out vec3 FragColor;

in vec2 v_uv;

uniform sampler2D renderedTexture;

void main()
{

    FragColor = texture(renderedTexture, v_uv).xyz;
}