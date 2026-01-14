#version 460 core

out vec3 FragColor;

in vec2 v_uv;

struct Light {
    vec3 position;
    vec3 diffuse;
    vec3 specular;
    float radius;
    int type;
    bool castShadows;
};

uniform sampler2D gPosition;
uniform sampler2D gNormal;
uniform sampler2D gAlbedoSpec;
uniform sampler2D gSpecular;

uniform sampler2DArray shadow_map;

uniform mat4x4 lightSpaceMatrices[5];
uniform float frustumSizes[5];
uniform float levelDistances[5];
uniform int cascadeLevels = 0;

uniform Light lights[10];
uniform int lightCount = 0;
uniform vec3 eyePos;

uniform mat4x4 projection, view;

float near = 0.1;
float far  = 100.0;

int getCascadeLevel(float normalized_dist) {
    int level = 0;

    for (int i = 0; i < cascadeLevels; i++) {
        if (normalized_dist < levelDistances[i]) {
            return i;
        }
    }

    return cascadeLevels-1;
}

vec3 getColor(int cascadeLevel) {
    vec3 colors[5] = vec3[](vec3(0.3, 1, 0.3), vec3(1, 1, 0.3), vec3(1, 0.3, 0.3), vec3(1, 0.3, 1), vec3(0.3, 0.3, 1));

    return colors[cascadeLevel];
}

vec3 getLightVector(vec3 position, int cascadeLevel) {
    vec4 projected = lightSpaceMatrices[cascadeLevel] * vec4(position, 1);
    vec3 normalized = (projected.xyz + vec3(1))*0.5;
    return normalized;
}

float getLightDepthCascade(vec3 normalized, int cascadeLevel) {
    return texture(shadow_map, vec3(normalized.xy, cascadeLevel)).x;
}

float getSize(int cascadeLevel) {
    return frustumSizes[cascadeLevel];
}

float rand(vec2 co){
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

float isInLight(vec3 position, int cascadeLevel, vec3 l, vec3 n) {
    vec3 normalized = getLightVector(position, cascadeLevel);
    float is_in_light = 0;
    float n_times = 0;
    vec2 texelSize = 1.0 / textureSize(shadow_map, 0).xy;
    int size = 5;
    float worldSize = getSize(cascadeLevel) / 10.0f;
    for (int x = -size; x <= size; x++) {
        for (int y = -size; y <= size; y++) {
            float jitter_x = mod(rand(vec2(position.x + x, position.y + y)), 1);

            float jitter_y = mod(rand(vec2(position.y + x, position.x + y)), 1);

            vec2 sampleUv = vec2(x + jitter_x, y + jitter_y);

            vec3 uv = normalized + vec3(sampleUv*texelSize / worldSize, 0) * 1.0f;

            float depth = getLightDepthCascade(uv, cascadeLevel);

            float slope_bias = (1 - dot(l, n))*0.0014f;
            float bias = 0.0002f;

            int is_in_light_local = int((uv.z - bias - slope_bias) < depth);
            if (uv.x > 1.0 || uv.y > 1.0 || uv.x < 0.0 || uv.y < 0.0 || uv.z > 1.0) {
                is_in_light_local = 1;
            }
            is_in_light += is_in_light_local;
            n_times++;
        }
    }

    is_in_light /= n_times;

    return is_in_light;
}

void main()
{
    vec3 albedo = texture(gAlbedoSpec, v_uv).rgb;
    float shininess = texture(gAlbedoSpec, v_uv).w * 256.0f;
    vec3 t_specular = texture(gSpecular, v_uv).rgb;
    vec3 normal = texture(gNormal, v_uv).rgb;
    vec3 position = texture(gPosition, v_uv).xyz;

    vec3 c_ambient = vec3(0.1f, 0.1f, 0.1f) * albedo;
    vec3 c_specular = vec3(0);
    vec3 c_diffuse = vec3(0);

    vec3 view_position = vec3(view * vec4(position, 1));

    vec3 v = normalize(eyePos - position);
    float dist = -view_position.z;
    float near = 0.1f;
    float far = 100.0f;
    float normalized_dist = dist / (far - near);
    int cascadeLevel = getCascadeLevel(normalized_dist);

    vec3 fogColor = vec3(0.3f, 0.5f, 0.6f);

    if (shininess == 0) {
        FragColor = fogColor;
    }
    else {
        for (int i = 0; i < lightCount; i++) {
            Light light = lights[i];

            if (light.type == 0) {
                vec3 l = normalize(light.position);

                vec3 r = reflect(-l, normal);

                float specular = pow(max(dot(r, v), 0), shininess);
                float diffuse = max(dot(l, normal), 0);

                float is_in_light = 1;
                if (light.castShadows) {
                    is_in_light = isInLight(position, cascadeLevel, l, normal);
                }

                c_specular += specular * t_specular * light.specular * is_in_light;
                c_diffuse += diffuse * light.diffuse * albedo * is_in_light;
            }
            else if (light.type == 1) {
                vec3 d = light.position - position;
                vec3 l = normalize(d);

                vec3 r = reflect(-l, normal);

                float distance = length(d);
                float attenuation = pow(max(1 - pow(distance / light.radius, 4), 0), 2);

                float specular = pow(max(dot(r, v), 0), shininess);
                float diffuse = max(dot(l, normal), 0);

                c_specular += attenuation * specular * t_specular * light.specular;
                c_diffuse += attenuation * diffuse * light.diffuse * albedo;
            }
        }

        vec3 c_shaded = c_ambient + c_diffuse + c_specular;

        float depth = normalized_dist;
        depth = min(pow(depth+0.05, 5), 1);

        FragColor = depth * fogColor + (1 - depth) * c_shaded;
    }
}