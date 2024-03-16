package manatee.primitives.gl;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import manatee.primitives.Primitive;
import manatee.primitives.Primitives;

public class ConeRenderer implements PrimitiveRenderer
{
	private int numIndices;
	
	private boolean cull;

	public ConeRenderer(int numIndices)
	{
		this.numIndices = numIndices;
	}

	@Override
	public void preRender(PrimitiveShader shader)
	{
		cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
		
		GL11.glDisable(GL11.GL_CULL_FACE);
	}

	@Override
	public void render(Primitive primitive, PrimitiveShader shader)
	{
		shader.setUniform("OffsetA", primitive.getStart());
        shader.setUniform("OffsetB", primitive.getStart());
        shader.setUniform("EdgeColor", primitive.getColor());
		shader.setUniform("Rotation", primitive.getRotation());
		shader.setUniform("Scale", new Vector3f(primitive.getScale(), primitive.getScale(), primitive.getScale()));
		
		GL11.glDrawElements(GL11.GL_TRIANGLES, numIndices, GL11.GL_UNSIGNED_INT, 0);
	}

	@Override
	public void postRender(PrimitiveShader shader)
	{
		shader.setUniform("Rotation", Primitives.NO_ROTATION);
		
		if (cull)
			GL11.glEnable(GL11.GL_CULL_FACE);
	}
}
