#version 460 core

out vec3 FragColor;

in vec2 v_uv;

uniform sampler2D renderedTexture;

uniform sampler2D g_position;

uniform sampler2D g_normal;

uniform vec3 eyePos;

uniform mat4x4 projection, view;

uniform float fogDensity = 0.5;
uniform vec3 fogColor = vec3(0.5,0.6,0.7);

uniform int selectedEntityId = -1;


vec3 applyFog(vec3 color, float d) {
    float fogAmount = 1.0 - exp(-d*fogDensity*0.01);
    return mix(color, fogColor, fogAmount);
}

void main()
{
    vec4 position = texture(g_position, v_uv);

    float entityId = position.w;

    vec3 color = texture(renderedTexture, v_uv).xyz;

    vec3 highlight = vec3(1, 0, 0.3);
    highlight = selectedEntityId == entityId && selectedEntityId != -1 ? highlight : color;

    float strength = 0.3;

    color =  color * (1 - strength) + highlight * strength;

    float distance = length(eyePos - position.xyz);
    color = applyFog(color, distance);

    vec3 tonemapped =  color / (color + vec3(1.0));;

    vec3 gamma_corrected = pow(tonemapped, vec3(1 / 2.2));

    FragColor = gamma_corrected;
}