#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 sampleStep;

uniform float Radius;
uniform float RadiusMultiplier;
uniform float Spread;

out vec4 fragColor;

void main() {
    float dist = length((texCoord - 0.5) * 2.0);
    float edge = mix(1.6, -0.9, Spread);
    float mask = smoothstep(edge, edge + 0.7, dist);
    float actualRadius = Radius * RadiusMultiplier * mask;
    vec4 blurred = texture(DiffuseSampler, texCoord);
    float total = 1.0;
    for (int i = 1; i <= 8; i++) {
        float t = float(i) / 8.0;
        float offset = actualRadius * t;
        float weight = 1.0 - t;
        blurred += texture(DiffuseSampler, texCoord + sampleStep * offset) * weight;
        blurred += texture(DiffuseSampler, texCoord - sampleStep * offset) * weight;
        total += weight * 2.0;
    }
    fragColor = blurred / total;
}
