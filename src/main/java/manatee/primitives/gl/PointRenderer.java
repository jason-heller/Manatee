package manatee.primitives.gl;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import manatee.primitives.Primitive;

public class PointRenderer implements PrimitiveRenderer
{

	private int numIndices;

	private Vector3f scale = new Vector3f(.1f, .1f, .1f);

	public PointRenderer(int numIndices)
	{
		this.numIndices = numIndices;
	}

	@Override
	public void preRender(PrimitiveShader shader)
	{

	}

	@Override
	public void render(Primitive primitive, PrimitiveShader shader)
	{
		shader.setUniform("Scale", new Vector3f(scale).mul(primitive.getScale()));
		shader.setUniform("OffsetA", primitive.getStart());
		shader.setUniform("OffsetB", primitive.getEnd());
		shader.setUniform("EdgeColor", primitive.getColor());

		GL11.glDrawElements(GL11.GL_TRIANGLES, numIndices, GL11.GL_UNSIGNED_INT, 0);
	}

	@Override
	public void postRender(PrimitiveShader shader)
	{
	}
}
