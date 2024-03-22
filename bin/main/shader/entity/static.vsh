#version 330 core

uniform mat4 v_ProjectionView;
uniform mat4 v_ModelMatrix;

uniform vec4 v_Diffuse;

uniform vec3 v_Color;

@import "shader/shared/light.vsh"

layout (location = 0) in vec3 v_Position;
layout (location = 1) in vec2 v_TexCoord;
layout (location = 2) in vec3 v_Normal;

out vec2 f_TexCoord;
out vec3 f_Normal;
out vec4 f_Color;

void main() {

	f_TexCoord = v_TexCoord;
	
	vec4 worldPos = v_ModelMatrix * vec4(v_Position, 1.0);
	f_Normal = (mat3(v_ModelMatrix) * v_Normal);
	
	@include lighting
	
	f_Color = v_Diffuse * f_Color;

	gl_Position = v_ProjectionView * worldPos;
} 