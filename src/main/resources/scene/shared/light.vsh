@require {
	vec4 worldPos;
	vec3 f_Normal;
	vec3 v_Color;
	vec4 f_Color;
}

#define MAX_LIGHTS 4
#define AMBIENT_MIN 0.6
#define AMBIENT_VARIANCE 0.4

uniform vec3 v_AmbientColor;
uniform vec3 v_AmbientVector;

uniform vec3 v_LightOrigins[MAX_LIGHTS];
uniform vec3 v_LightVectors[MAX_LIGHTS];
uniform vec3 v_LightColors[MAX_LIGHTS];
uniform float v_LightRadii[MAX_LIGHTS];

uniform int v_LightNum;


@mixin lighting {
	float brightness = AMBIENT_MIN + (dot(v_AmbientVector, f_Normal) * AMBIENT_VARIANCE);
	
	f_Color = vec4(v_Color * (brightness * v_AmbientColor), 1.0);
	
	for(int i = 0; i < v_LightNum; i++) {
	
		vec3 toLight = v_LightOrigins[i] - worldPos.xyz;
		float dist = length(toLight);

		float falloff = 0.0;
		float r = dist / v_LightRadii[i];
		r = r * r * r * r;
		falloff = clamp(1.0 - r, 0.0, 1.0);
		falloff *= falloff;
		float sDist = dist / (v_LightRadii[i]);
		falloff /= (sDist * sDist) + 1.0;
		falloff *= max(dot(f_Normal, normalize(toLight)) + 0.25, 0.0);
		
		if (v_LightVectors[i] != vec3(0.0)) {
			falloff *= max(dot(v_LightVectors[i], f_Normal), 0.0);
		}
		
		brightness += falloff;
		
		f_Color.xyz += (falloff * v_LightColors[i]);
	}
}