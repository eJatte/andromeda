#version 460 core

out vec4 FragColor;

in vec3 v_position;

void main()
{
    vec3 gamma_corrected = pow(vec3(0,1,0), vec3(1 / 2.2));

    FragColor = vec4(gamma_corrected, 1);
}