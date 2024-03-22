#version 330 core

in vec2 f_TexCoord;
in vec3 f_Normal;
in vec4 f_ClipSpace;

uniform sampler2D f_Depth;
uniform sampler2D f_Diffuse;
uniform sampler2D f_Foam;

uniform float f_ShoreLine;
uniform float f_SeaLevelDelta;

uniform vec2 f_Viewport;
uniform vec2 v_Shift;

uniform vec4 f_WaterColor;

#define NEAR 0.1
#define FAR 1000.0

out vec4 f_FragmentColor;

float toWorldSpace(float value) {
	return 2.0 * NEAR * FAR / (FAR + NEAR - (2.0 * value - 1.0) * (FAR - NEAR));
}

void main() { 
	vec2 texCoords = f_TexCoord + v_Shift;
	float foamSample = texture(f_Foam, texCoords * 2.0).r;
	vec2 normDeviceSpace = ((f_ClipSpace.xy / f_ClipSpace.w) / 2.0 + 0.5);
	
	vec4 waterColor = texture(f_Diffuse, texCoords) * f_WaterColor;
	
	vec2 pixelCoord = gl_FragCoord.xy / f_Viewport;

	float depthMapSample = texture(f_Depth, normDeviceSpace).r;
	
	float surfaceDistance = toWorldSpace(gl_FragCoord.z);
	float floorDistance = toWorldSpace(depthMapSample);
	float depth = floorDistance - surfaceDistance;
	
	float shoreLine = 1.0 - f_ShoreLine;
	float opaqueness = 1.0 - clamp((depth - shoreLine), 0.0, 1.0);	// div by 2
	
	//opaqueness += waterColor.x;
	
	float foamAlpha = clamp((f_ShoreLine + 1.0) / 2.0, 0.0, 1.0);
	float foamMix = foamAlpha * opaqueness;
	foamMix = opaqueness;
	foamMix += (1.0 - (0.4 - (1.0-(depth*0.5)))) * foamSample;
	
	foamMix = foamMix > 0.4 ? 1.0 : 0.0;
	
	foamMix = clamp(foamMix, 0.0, 1.0);
	
	vec4 foamColor = vec4(1.0);
	
		
	if (opaqueness > 0.5) {
		// Wet sand
		f_FragmentColor = vec4(clamp(depth - 0.5, 0.0, 0.25));		// Change to 0.5 if u remove div by 2 above
	} else {

		f_FragmentColor = waterColor;
		
		f_FragmentColor += foamColor * foamMix; //mix(waterColor, foamColor, foamMix);
	
		f_FragmentColor.a = waterColor.a;
	}
}