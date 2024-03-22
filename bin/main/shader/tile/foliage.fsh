#version 330 core

@import "shader/shared/celshade.fsh"

in vec2 f_TexCoord;
in vec3 f_Normal;
in vec4 f_Color;

uniform sampler2D f_Diffuse; 

out vec4 f_FragmentColor;

void main() {    
	@include celShade

	f_FragmentColor = vec4(celShadeColor, 1.0);
}  