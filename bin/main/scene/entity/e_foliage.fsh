#version 330 core

@import "scene/shared/celshade.fsh"

in vec2 f_TexCoord;
in vec3 f_Normal;
in vec4 f_Color;

uniform sampler2D f_Diffuse; 

out vec4 f_FragmentColor;

void main() {    
	vec4 diffuse = texture(f_Diffuse, f_TexCoord);
	
	if (diffuse.a < 0.9)
		discard;
		
	@include celShade
		
	f_FragmentColor = vec4(celShadeColor, 1.0);
}  