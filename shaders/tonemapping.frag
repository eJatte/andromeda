#version 460 core

out vec3 FragColor;

in vec2 v_uv;

uniform sampler2D renderedTexture;

void main()
{

    vec3 color = texture(renderedTexture, v_uv).xyz;

    vec3 tonemapped =  color / (color + vec3(1.0));;

    vec3 gamma_corrected = pow(tonemapped, vec3(1 / 2.2));

    FragColor = gamma_corrected;
}