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

in vec3 vPosition;
in vec3 vNormal;
in vec2 vUv;
in vec3 vTangent;
in vec3 vBiTangent;

uniform sampler2D normal_texture;

uniform Light lights[10];
uniform int lightCount = 0;
uniform vec3 eyePos;

uniform Material material;

void main()
{
    vec3 n = normalize(vNormal);
    vec3 v = normalize(eyePos - vPosition);
    vec3 t = normalize(vTangent);
    vec3 bt = normalize(vBiTangent);

    mat3x3 transform = mat3x3(t, bt, n);

    vec3 t_normal = texture(normal_texture, vUv).rgb;
    // remap from [0,1] to [-1,1]
    t_normal = t_normal * 2.0f - 1.0f;

    vec3 c_ambient = vec3(0.2f) * material.ambient;
    vec3 c_specular = vec3(0);
    vec3 c_diffuse = vec3(0);

    n = normalize(transform * normalize(t_normal));

    Light light = lights[0];

    vec3 d = light.position - vPosition;
    vec3 l = normalize(d);
    vec3 r = reflect(-l, n);

    float specular = pow(max(dot(r, v), 0), material.shininess);
    float diffuse = max(dot(l, n), 0);

    c_specular += specular * material.specular * light.specular;
    c_diffuse += diffuse * light.diffuse * material.diffuse;


    vec3 c_shaded = c_ambient + c_diffuse + c_specular;

    vec3 gamma_corrected = pow(c_shaded, vec3(1 / 2.2));

    //gamma_corrected = vec3(vUv.x, vUv.y, 0);

    //gamma_corrected = n;
    //gamma_corrected = (n + vec3(1)) * 0.5;

    //gamma_corrected = t_normal;

    vec3 normal_debug;
    if (vUv.x > 0.25 && vUv.x < 0.75 && vUv.y > 0.25 && vUv.y < 0.75) {
        normal_debug = normalize(vNormal);
    }
    else if (abs(vUv.x - 0.5f) > abs(vUv.y - 0.5f)) {
        normal_debug = t;
    }
    else {
        normal_debug = bt;
    }

    //gamma_corrected = (normal_debug + vec3(1)) * 0.5;
    //gamma_corrected = normal_debug;

    if (abs(vUv.y - 0.5f) < 0.05f && abs(vUv.x - 0.5f) > 0.4f) {
        //gamma_corrected = vec3(1, 0, 0);
        if (vUv.x < 0.025f) {
            //gamma_corrected = vec3(0, 1, 1);
        }

    }
    if (abs(vUv.x - 0.5f) < 0.05f && abs(vUv.y - 0.5f) > 0.4f) {
        //gamma_corrected = vec3(0, 1, 0);
        if (vUv.y < 0.025f) {
            //gamma_corrected = vec3(1, 0, 1);
        }
    }

    FragColor = vec4(gamma_corrected, 0);
}