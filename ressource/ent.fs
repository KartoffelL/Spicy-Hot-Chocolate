#version 330 core

layout (location = 0) out vec4 FragColor;
layout (location = 1) out vec4 FragPos;
layout (location = 2) out vec4 FragNormal;
layout (location = 3) out vec4 FragMaterial;

uniform sampler2D sampler0;

uniform vec2 setSize;
uniform vec2 tileOffset;

in vec2 tex_coords;
in vec2 coords;

uniform vec4 tint;

void main() {
	vec2 co = tex_coords;
	
	vec4 color = texture(sampler0, (mod(tex_coords, 1)+tileOffset)/setSize);
	
	
	
	float tb = (tint.r+tint.g+tint.b)/3;
	FragColor = color*tint/tb;
	FragPos = vec4(0, 0, 0, 1);
	FragNormal = vec4(0, 0, 0, 1);
	FragMaterial = vec4(0, 0, 0, 1);
}
