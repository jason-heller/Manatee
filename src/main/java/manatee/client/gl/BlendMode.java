package manatee.client.gl;

import static org.lwjgl.opengl.GL11.*;

public enum BlendMode
{
	MULTIPLY(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA),
	ADDITIVE(GL_ONE, GL_ONE),
	SUBTRACTIVE(GL_ONE_MINUS_DST_COLOR, GL_ONE_MINUS_SRC_ALPHA),
	GLOW(GL_SRC_ALPHA, GL_ONE),
	INVERT(GL_DST_COLOR, GL_ONE_MINUS_SRC_ALPHA),
	EXCLUDE(GL_ONE_MINUS_DST_COLOR, GL_ONE),
	REPLACE(GL_ZERO, GL_SRC_COLOR),
	TINT(GL_DST_COLOR, GL_ONE),	// Spooky
	PREMUL_ALPHA(GL_ONE_MINUS_DST_ALPHA, GL_DST_ALPHA);

	private final int src, dest;
	
	BlendMode(int src, int dest)
	{
		this.src = src;
		this.dest = dest;
	}
	
	public int source()
	{
		return src;
	}
	
	public int dest()
	{
		return dest;
	}
}
