#version 330 core

uniform mat4 ProjectionView;
uniform mat3 Rotation;
uniform vec3 OffsetA;
uniform vec3 OffsetB;
uniform vec3 Scale;

layout (location = 0) in vec3 Vertex;

out vec3 Color;

uniform vec3 EdgeColor;

void main() {
	vec3 vertex = Vertex * Scale * Rotation;
	
	if (mod(gl_VertexID, 2) == 0) {
		vertex += OffsetA;
	} else {
		vertex += OffsetB;
	}

    Color = EdgeColor;

    gl_Position = ProjectionView * vec4(vertex, 1.0);
} 