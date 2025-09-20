#version 460 core

layout(location = 0) out vec3 gPosition;
layout(location = 1) out vec3 gNormal;
layout(location = 2) out vec4 gAlbedoSpecular;
layout(location = 3) out vec3 gSpecular;

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

uniform bool has_diffuse_texture = false, has_normal_texture = false, has_roughness_texture = false;

uniform Material material;

void main()
{
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

    gPosition = v_position;
    gNormal = n;
    gAlbedoSpecular.rgb = t_diffuse * material.diffuse;
    // we divide by 256.0f to normalize to 0-1. This means that the max value for shininess is 256.0f.
    // the reason we do this is because we dont use a floating point texture for this.
    gAlbedoSpecular.w = material.shininess / 256.0f;
    gSpecular = material.specular * rougness;

}