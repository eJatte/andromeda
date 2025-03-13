#version 460 core

out vec4 FragColor;

in vec3 normal_test;

void main()
{
    FragColor = vec4(abs(normal_test), 1);
}