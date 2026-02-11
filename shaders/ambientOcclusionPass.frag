#version 460 core

out vec3 FragColor;

in vec2 v_uv;

uniform sampler2D gPosition;
uniform sampler2D gNormal;
uniform sampler2D gAlbedo;
uniform sampler2D noise;

uniform mat4x4 projection, view;

uniform vec3[128] samples;

uniform float radius = 0.3f;
uniform int n_samples = 64;
uniform float power = 1;
uniform float bias = 0.02f;

vec2 getProjectedUv(vec3 position) {
    vec4 projectedPos = projection * view * vec4(position, 1);

    projectedPos.xyz /= projectedPos.w;
    projectedPos.xyz  = projectedPos.xyz * 0.5 + 0.5;

    vec2 uv = projectedPos.xy;

    return uv;
}

float getDepth(vec3 position) {
    vec4 viewSpacePos = view * vec4(position, 1);
    return viewSpacePos.z;
}

vec3 getView(vec3 position) {
    vec4 viewSpacePos = view * vec4(position, 1);
    return viewSpacePos.xyz;
}

float getOcclusion(vec3 samplePos, vec3 origin) {
    vec2 uv = getProjectedUv(samplePos);
    vec3 p = texture(gPosition, uv).xyz;

    float originDepth = abs(getDepth(origin));
    float sampleDepth = abs(getDepth(samplePos));
    float worldDepth = abs(getDepth(p));

    float diff =  (sampleDepth) - worldDepth;

    float rangeCheck = smoothstep(0.0, 1.0, radius / abs(originDepth - worldDepth));
    return (sampleDepth - bias >= worldDepth ? 1 : 0) * rangeCheck;
}

vec3 drawSphere(float r, vec3 pos, vec3 ogColor, vec3 color) {
    vec3 position = texture(gPosition, v_uv).xyz;

    return length(pos.xyz - position.xyz) < r ? color : ogColor;
}

void main()
{
    vec3 color = texture(gAlbedo, v_uv).rgb;
    float shininess = texture(gAlbedo, v_uv).w;
    vec3 normal = texture(gNormal, v_uv).rgb;
    vec3 position = texture(gPosition, v_uv).xyz;

    vec2 scale = textureSize(gPosition, 0) / 4;

    vec3 randomVector = texture(noise, v_uv * scale).xyz;

    float total = 0;

    vec3 tangent = normalize(randomVector - dot(randomVector, normal) * normal);
    vec3 bi_tangent = cross(normal, tangent);

    mat3 tbn = mat3(tangent, bi_tangent, normal);

    float occlusion = 0;

    for (int i = 0; i < n_samples; i++) {
        vec3 s = tbn * samples[i];
        vec3 samplePos = position + s * radius;

        vec2 uv = getProjectedUv(samplePos);
        vec3 p = texture(gPosition, uv).xyz;

        occlusion += getOcclusion(samplePos, position);
    }

    occlusion = 1.0 - occlusion / n_samples;

    FragColor = vec3(pow(occlusion, power));
}