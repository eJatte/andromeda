#version 460 core

out vec4 FragColor;

in vec3 v_position;

uniform vec3 color;

void main()
{
    FragColor = vec4(color, 1);
}