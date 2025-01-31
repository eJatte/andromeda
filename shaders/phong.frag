#version 460 core

struct Light {
    vec3 position;
    vec3 diffuse;
    vec3 specular;
    float radius;
    int type;
};

struct Material {
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    float shininess;
    vec2 texture_scale;
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
uniform sampler2D roughness_texture;



uniform Light lights[10];
uniform int lightCount = 0;
uniform vec3 eyePos;

uniform bool has_diffuse_texture = false, has_normal_texture = false, has_roughness_texture = false;

uniform Material material;

void main()
{
    vec3 v = normalize(eyePos - v_position);

    vec3 n = normalize(v_normal);
    vec3 t = normalize(v_tangent);
    vec3 bt = normalize(v_bitangent);

    vec3 t_diffuse = vec3(1);

    if (has_diffuse_texture) {
        t_diffuse = texture(diffuse_texture, v_uv*material.texture_scale).rgb;
    }
    if (has_normal_texture) {
        vec3 t_normal = texture(normal_texture, v_uv*material.texture_scale).rgb;
        // remap from [0,1] to [-1,1]
        t_normal = t_normal * 2.0f - 1.0f;

        n = normalize(m_tangent_to_world * normalize(t_normal));
    }

    float rougness = 1.0;

    if (has_roughness_texture) {
        rougness = texture(roughness_texture, v_uv*material.texture_scale).r;
    }

    vec3 c_ambient = vec3(0.1f, 0.1f, 0.1f) * t_diffuse * material.ambient;
    vec3 c_specular = vec3(0);
    vec3 c_diffuse = vec3(0);



    for (int i = 0; i < lightCount; i++) {
        Light light = lights[i];

        if (light.type == 0) {
            vec3 l = normalize(light.position);

            vec3 r = reflect(-l, n);

            float specular = pow(max(dot(r, v), 0), material.shininess);
            float diffuse = max(dot(l, n), 0);

            c_specular += specular * material.specular * light.specular * rougness;
            c_diffuse += diffuse * light.diffuse * t_diffuse * material.diffuse;
        }
        else if (light.type == 1) {
            vec3 d = light.position - v_position;
            vec3 l = normalize(d);

            vec3 r = reflect(-l, n);

            float distance = length(d);
            float attenuation = pow(max(1 - pow(distance / light.radius, 4), 0), 2);

            float specular = pow(max(dot(r, v), 0), material.shininess);
            float diffuse = max(dot(l, n), 0);

            c_specular += attenuation * specular * material.specular * light.specular * rougness;
            c_diffuse += attenuation * diffuse * light.diffuse * t_diffuse * material.diffuse;
        }
    }

    vec3 c_shaded = c_ambient + c_diffuse + c_specular;

    vec3 gamma_corrected = pow(c_shaded, vec3(1 / 2.2));

    FragColor = vec4(gamma_corrected, 0);
}