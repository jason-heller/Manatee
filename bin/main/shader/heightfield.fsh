#version 330 core

in vec2 f_TexCoord;
in vec4 f_Color;

uniform sampler2D f_Terrain0;
uniform sampler2D f_Terrain1;

out vec4 f_FragmentColor;

void main() {    
	vec4 tex0 = texture(f_Terrain0, f_TexCoord);
	vec4 tex1 = texture(f_Terrain1, f_TexCoord);

	f_FragmentColor = vec4(f_Color.xyz, 1.0) * mix(tex0, tex1, f_Color.a);
	f_FragmentColor.a = 1.0;
}  