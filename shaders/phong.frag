#version 460 core

struct Light {
    vec3 position;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

struct Material {
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    float shininess;
};

out vec4 FragColor;

in vec3 v_position;
in vec3 v_normal;
in vec2 v_uv;
in vec3 v_tangent;
in vec3 v_bitangent;
in mat3 m_tangent_to_world;

uniform sampler2D diffuse_texture;
uniform sampler2D normal_texture;

uniform Light lights[10];
uniform int lightCount = 0;
uniform vec3 eyePos;

uniform bool has_diffuse_texture = true, has_normal_texture = true;

uniform Material material;

void main()
{
    vec3 v = normalize(eyePos - v_position);

    vec3 n = normalize(v_normal);
    vec3 t = normalize(v_tangent);
    vec3 bt = normalize(v_bitangent);

    vec3 t_diffuse = vec3(1);

    if (has_diffuse_texture) {
        t_diffuse = texture(diffuse_texture, v_uv).rgb;
    }
    if (has_normal_texture) {
        vec3 t_normal = texture(normal_texture, v_uv).rgb;
        // remap from [0,1] to [-1,1]
        t_normal = t_normal * 2.0f - 1.0f;

        n = normalize(m_tangent_to_world * normalize(t_normal));
    }

    vec3 c_ambient = vec3(0.2f) * t_diffuse * material.ambient;
    vec3 c_specular = vec3(0);
    vec3 c_diffuse = vec3(0);



    for (int i = 0; i < lightCount; i++) {
        Light light = lights[i];

        vec3 d = light.position - v_position;
        vec3 l = normalize(d);
        vec3 r = reflect(-l, n);

        float specular = pow(max(dot(r, v), 0), material.shininess);
        float diffuse = max(dot(l, n), 0);

        c_specular += specular * material.specular * light.specular;
        c_diffuse += diffuse * light.diffuse * t_diffuse * material.diffuse;
    }

    vec3 c_shaded = c_ambient + c_diffuse + c_specular;

    vec3 gamma_corrected = pow(c_shaded, vec3(1 / 2.2));

    FragColor = vec4(gamma_corrected, 0);
}