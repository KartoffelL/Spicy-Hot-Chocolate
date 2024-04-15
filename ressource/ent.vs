#version 330 core

in vec3 vertices;
in vec2 textures;

out vec2 tex_coords;
out vec2 coords;

uniform mat4 matrix;
uniform vec2 scale;

void main() {
	tex_coords = textures;
	gl_Position =  matrix*vec4(vertices.xy*scale, 1, 1);
	gl_Position.x -= 1;
	gl_Position.y += 1;
}
