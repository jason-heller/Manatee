#version 330 core

uniform mat4 v_ProjectionView;

layout (location = 0) in vec3 v_Position;
layout (location = 1) in vec2 v_TexCoord;
layout (location = 2) in vec3 v_Normal;
layout (location = 3) in vec3 v_Color;

out vec2 f_TexCoord;
out vec3 f_Normal;
out vec4 f_ClipSpace;

void main() {
	f_TexCoord = v_TexCoord;
	f_Normal = v_Normal;

	f_ClipSpace = v_ProjectionView * vec4(v_Position, 1.0);
	gl_Position = f_ClipSpace;
} 