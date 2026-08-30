#version 460 core

out vec4 FragColor;

in vec4 v_normalized_position;
in vec4 v_world_position;

uniform sampler2D g_position;

uniform vec3 eyePos;
uniform vec3 color = vec3(0,1,0);

void main()
{
    vec2 uv = v_normalized_position.xy / v_normalized_position.w * 0.5 + 0.5;
    vec4 position = texture(g_position, uv);

    float distance_world = distance(position.xyz, eyePos);
    float distance_debug = distance(v_world_position.xyz, eyePos);

    if (distance_world > distance_debug) {
        FragColor = vec4(color, 1);
    }
    else {
        FragColor = vec4(color*0.7, 1);
    }
}