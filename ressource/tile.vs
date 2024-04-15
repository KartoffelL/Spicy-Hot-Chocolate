#version 330 core

in vec3 vertices;
in vec2 textures;

out vec2 tex_coords;

uniform bool stat;

void main() {
	tex_coords = textures;
	gl_Position =  vec4(vertices, 1);
	
}
