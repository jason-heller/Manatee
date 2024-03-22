#version 330 core

in vec2 f_TexCoord;
in vec3 f_Normal;
in vec4 f_Color;

uniform sampler2D f_Diffuse; 

out vec4 f_FragmentColor;

void main() {    
	f_FragmentColor = texture(f_Diffuse, f_TexCoord) * f_Color;
}  