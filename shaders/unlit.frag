#version 460 core

layout(location = 0) out vec3 color;

struct Material {
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    float shininess;
    vec2 texture_scale;
};

out vec4 FragColor;

in vec3 v_position;

uniform Material material;

void main()
{
    vec3 gamma_corrected = pow(material.diffuse, vec3(1 / 2.2));

    color = vec3(gamma_corrected);
}