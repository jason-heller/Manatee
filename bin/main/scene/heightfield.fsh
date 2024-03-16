#version 330 core

in vec2 f_TexCoord;
in vec4 f_Color;

uniform sampler2D v_Diffuse; 

out vec4 f_FragmentColor;

void main() {    
	f_FragmentColor = f_Color * texture(v_Diffuse, f_TexCoord);
	f_FragmentColor.a = 1.0;
}  