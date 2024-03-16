#version 330

in vec2 f_TexCoords1;
in vec2 f_TexCoords2;
in float f_Blend;

out vec4 f_FragmentColor;

uniform sampler2D f_Diffuse;

void main(void){

	vec4 color1 = texture(f_Diffuse, f_TexCoords1);
	vec4 color2 = texture(f_Diffuse, f_TexCoords2);

	f_FragmentColor = mix(color1, color2, f_Blend);
}
