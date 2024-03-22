#version 330 core

uniform mat4 v_ProjectionView;

uniform float v_Stride;
uniform float v_Resolution;
uniform vec2 v_Position;

uniform vec3 v_AmbientVector;
uniform vec3 v_AmbientColor;

#define MAX_LIGHTS 4
#define AMBIENT_MIN 0.6
#define AMBIENT_VARIANCE 0.4

uniform vec3 v_LightOrigins[MAX_LIGHTS];
uniform vec3 v_LightVectors[MAX_LIGHTS];
uniform vec3 v_LightColors[MAX_LIGHTS];
uniform float v_LightRadii[MAX_LIGHTS];

uniform int v_LightNum;

layout (location = 0) in float v_Height;
layout (location = 1) in vec3 v_Normal;

out vec2 f_TexCoord;
out vec4 f_Color;

// 7.75
#define TEX_RESOLUTION 8.0

float getTexAlpha(float v) {
	return float(floatBitsToInt(v) & 7) / 7.0;
}

void main() {

	float verticesPerRun = ((v_Stride + 1.0) * 2.0);
	float clampedVerticesPerRun = verticesPerRun - 3.0;
	
	float rowIndex = mod(gl_VertexID, verticesPerRun);
	float clampedIndex = clamp(rowIndex - 1.0, 0.0, clampedVerticesPerRun);
	
	vec3 position = vec3(
		floor(clampedIndex / 2.0),
		mod(clampedIndex, 2.0),
		v_Height
	);
	
	position.y += floor(gl_VertexID / verticesPerRun);

	position.x *= v_Resolution;
	position.y *= v_Resolution;

	position.x += v_Position.x;
	position.y += v_Position.y;
	
	f_TexCoord = (position.xy) / TEX_RESOLUTION;
	
	float brightness = AMBIENT_MIN + (dot(v_AmbientVector, v_Normal) * AMBIENT_VARIANCE);
	f_Color = vec4(brightness * v_AmbientColor, getTexAlpha(v_Height));
	
	for(int i = 0; i < v_LightNum; i++) {
	
		vec3 toLight = v_LightOrigins[i] - position;
		float dist = length(toLight);
		
		float falloff = 0.0;
		float r = dist / v_LightRadii[i];
		r = r * r * r * r;
		falloff = clamp(1.0 - r, 0.0, 1.0);
		falloff *= falloff;
		float sDist = dist / (v_LightRadii[i]);
		falloff /= (sDist * sDist) + 1.0;
		falloff *= max(dot(v_Normal, normalize(toLight)) + 0.25, 0.0);
		
		if (v_LightVectors[i] != vec3(0.0)) {
			falloff *= max(dot(v_LightVectors[i], v_Normal), 0.0);
		}
	
		f_Color.xyz += (falloff * v_LightColors[i]);
		
	}
	
	gl_Position = v_ProjectionView * vec4(position, 1.0);
} 