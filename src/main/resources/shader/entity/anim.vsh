#version 330 core

const int MAX_WEIGHTS = 4;
const int MAX_BONES = 150;

uniform mat4 v_ProjectionView;
uniform mat4 v_ModelMatrix;
uniform mat4 v_BonesMatrices[MAX_BONES];

uniform vec4 v_Diffuse;

uniform vec3 v_Color;

@import "shader/shared/light.vsh"


layout (location = 0) in vec3 v_Position;
layout (location = 1) in vec2 v_TexCoord;
layout (location = 2) in vec3 v_Normal;
layout (location = 3) in vec4 v_BoneWeights;
layout (location = 4) in ivec4 v_BoneIndices;

out vec2 f_TexCoord;
out vec3 f_Normal;
out vec4 f_Color;

void main() {

	f_TexCoord = v_TexCoord;
	
	vec4 worldPos = vec4(0.0);
	vec4 worldNormal = vec4(0.0);

	int count = 0;

	for(int i = 0; i < MAX_WEIGHTS; i++)  {
		float weight = v_BoneWeights[i];
		if (weight > 0) {
			count++;
			mat4 jointTransform = v_BonesMatrices[v_BoneIndices[i]];
			
			vec4 posePosition = jointTransform * vec4(v_Position, 1.0);
			worldPos += posePosition * weight;
			
			vec4 normal = jointTransform * vec4(v_Normal, 0.0);
			worldNormal += normal * weight;
		}
	}

	if (count == 0) {
		worldPos = vec4(v_Position, 1.0);
		worldNormal = vec4(v_Normal, 0.0);
	}
	
	worldPos = v_ModelMatrix * worldPos;
	f_Normal = (v_ModelMatrix * worldNormal).xyz;
	
	@include lighting
	
	f_Color = v_Diffuse * f_Color;

	gl_Position = v_ProjectionView * worldPos;
} 