#version 330

layout(location = 0) in vec2 v_Position;
layout(location = 1) in vec4 v_ModelView0;
layout(location = 2) in vec4 v_ModelView1;
layout(location = 3) in vec4 v_ModelView2;
layout(location = 4) in vec4 v_TexOffsets;
layout(location = 5) in float v_Alpha;

out vec2 f_TexCoords1;
out vec2 f_TexCoords2;

out float f_Blend;

uniform float v_Scale;
uniform mat4 v_Projection;

void main(void) {

	mat4 modelView;
	
	modelView[0][0] = v_ModelView0.x;
	modelView[1][0] = v_ModelView0.y;
	modelView[2][0] = v_ModelView0.z;
	modelView[3][0] = v_ModelView0.w;
	modelView[0][1] = v_ModelView1.x;
	modelView[1][1] = v_ModelView1.y;
	modelView[2][1] = v_ModelView1.z;
	modelView[3][1] = v_ModelView1.w;
	modelView[0][2] = v_ModelView2.x;
	modelView[1][2] = v_ModelView2.y;
	modelView[2][2] = v_ModelView2.z;
	modelView[3][2] = v_ModelView2.w;
	modelView[3][3] = 1.0;

	vec2 textureCoords = vec2(v_Position.x + 0.5, -v_Position.y + 0.5);
	textureCoords *= v_Scale;

	f_TexCoords1 = textureCoords + v_TexOffsets.xy;
	f_TexCoords2 = textureCoords + v_TexOffsets.zw;
	
	f_Blend = v_Alpha;
	
	gl_Position = v_Projection * modelView * vec4(v_Position, 0.0, 1.0);

}
