#version 330

#define AMBIENT_MIN 0.6
#define AMBIENT_VARIANCE 0.4

layout(location = 0) in vec3 v_Position;
layout(location = 1) in vec3 v_Normal;
layout(location = 2) in mat4 v_Model;
layout(location = 6) in vec4 v_Color;

uniform mat4 v_ProjView;

uniform vec3 v_AmbientColor;
uniform vec3 v_AmbientVector;

out vec4 f_Color;

void main(void) {
	vec3 normal = (mat3(v_Model) * v_Normal);
	float brightness = AMBIENT_MIN + (dot(v_AmbientVector, normal) * AMBIENT_VARIANCE);
	
	f_Color.xyz = v_Color.xyz * (brightness * v_AmbientColor);
	f_Color.a = v_Color.a;
	
	gl_Position = v_ProjView * v_Model * vec4(v_Position, 1.0);

}
