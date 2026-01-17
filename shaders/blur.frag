#version 460 core

out vec3 FragColor;

in vec2 v_uv;

uniform sampler2D inputTexture;

void main()
{
    vec2 texelSize = 1.0 / vec2(textureSize(inputTexture, 0));
    int uBlurSize = 4;
    float result = 0.0;
    vec2 hlim = vec2(float(-uBlurSize) * 0.5 + 0.5);
    for (int i = 0; i < uBlurSize; ++i) {
        for (int j = 0; j < uBlurSize; ++j) {
            vec2 offset = vec2(float(i), float(j)) * texelSize;
            result += texture(inputTexture, v_uv + offset).r;
        }
    }

    FragColor = vec3(result / float(uBlurSize * uBlurSize));
}