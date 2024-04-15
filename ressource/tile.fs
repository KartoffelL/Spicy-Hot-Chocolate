#version 330 core

layout (location = 0) out vec4 FragColor;
layout (location = 1) out vec4 FragPos;
layout (location = 2) out vec4 FragNormal;
layout (location = 3) out vec4 FragMaterial;

uniform sampler2D sampler0;
uniform sampler2D sampler1;

uniform vec2 setSize;
uniform vec2 mapSize;

uniform vec2 scale;
uniform vec2 offset;

uniform vec2 background;

uniform vec4 tint;

in vec2 tex_coords;

void main() {
	vec2 co = tex_coords/scale+offset/mapSize;
	if(abs(co.x*2-1) > 1 || abs(co.y*2-1) > 1) {
		FragColor = vec4(0.1, 0.1, 0.1, 1);
		return;
	}
	vec4 r = texture(sampler1, co);
	vec2 a = (mod(tex_coords/scale*mapSize+offset, 1)+r.rg*255)/setSize;
	vec4 color = texture(sampler0, a);
	
	if(color.a != 1) {
		vec4 back = texture(sampler0, (mod(tex_coords/scale*mapSize+offset, 1)+background)/setSize);
		color = vec4(mix(back.rgb, color.rgb, color.a), 1);
	}
	vec2 shadowV = (1-abs(co*2-1))*mapSize;
	float shadow = clamp(min(shadowV.x,shadowV.y), 0, 1);
	float torch = pow(1-distance(tex_coords, vec2(0.5)), 6);
	float tb = (tint.r+tint.g+tint.b)/3;
	FragColor = color*tint/tb*max(tb, torch);
	FragColor.rgb = mix(vec3(0.1, 0.1, 0.1), FragColor.rgb, shadow);
	FragPos = vec4(0, 0, 0, 1);
	FragNormal = vec4(0, 0, 0, 1);
	FragMaterial = vec4(0, 0, 0, 1);
}
