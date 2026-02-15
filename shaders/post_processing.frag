#version 460 core

out vec3 FragColor;

in vec2 v_uv;

uniform sampler2D renderedTexture;

uniform sampler2D g_position;

uniform vec3 eyePos;

uniform mat4x4 projection, view;

uniform vec3 fogColor = vec3(0.3f, 0.5f, 0.6f);
uniform float fogDistance = 100.0f;
uniform float fogPower = 3;

float getNormalizedDistance(vec4 position) {
    float normalized_dist = length(eyePos - position.xyz) / fogDistance;
    return normalized_dist;
}

float getFogMultiplier(vec4 position) {
    float depth = getNormalizedDistance(position);
    depth = min(pow(depth+0.05, fogPower), 1);
    return position.w == -1 ? 1 : depth;
}

void main()
{
    vec4 position = texture(g_position, v_uv);

    vec3 color = texture(renderedTexture, v_uv).xyz;

    vec3 tonemapped =  color / (color + vec3(1.0));;

    float depth = getFogMultiplier(position);
    vec3 outPutColor = depth * fogColor + (1 - depth) * tonemapped;

    vec3 gamma_corrected = pow(outPutColor, vec3(1 / 2.2));

    FragColor = gamma_corrected;
}