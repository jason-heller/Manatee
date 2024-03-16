#version 330 core

uniform mat4 v_ProjectionView;
uniform mat4 v_ModelMatrix;

uniform vec4 v_Diffuse;

uniform vec3 v_WindVector;
uniform float v_WindStrength;
uniform float v_WindTime;

@import "scene/shared/light.vsh"

layout (location = 0) in vec3 v_Position;
layout (location = 1) in vec2 v_TexCoord;
layout (location = 2) in vec3 v_Normal;

out vec2 f_TexCoord;
out vec3 f_Normal;
out vec4 f_Color;



void main() {

	f_TexCoord = v_TexCoord;
	
	float windOffset = sin(v_WindTime + length(v_Position * v_WindVector)) * v_WindStrength;
	vec3 displacement = v_WindVector.xyz * windOffset;
	
	vec4 worldPos = v_ModelMatrix * vec4(v_Position + displacement, 1.0);
	f_Normal = (mat3(v_ModelMatrix) * v_Normal);
	vec3 v_Color = vec3(1.0);
	
	@include lighting
	
	f_Color.xyz = v_Diffuse.xyz * f_Color.xyz;
	f_Color.a = brightness;
	
	gl_Position = v_ProjectionView * worldPos;
} 