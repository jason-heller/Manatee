package manatee.cache.definitions.mesh;

import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import manatee.cache.definitions.MeshUtil;

public class GenericMesh extends BaseMeshIndexed
{
	protected int vertexCount;

	public GenericMesh(FloatBuffer positions, IntBuffer indices)
	{
		load(positions, indices);

		vertexCount = indices.capacity();
	}
	
	public GenericMesh(float[] positions, int[] indices)
	{
		load(positions, indices);

		vertexCount = indices.length;
	}

	@Override
	protected void setAttribPointers()
	{
		MeshUtil.attribInterlacedf(3, 2, 3, 3);
	}

	@Override
	public int getVertexCount()
	{
		return vertexCount;
	}
	
	@Override
	public void bind()
	{
		glBindVertexArray(vao);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
		glEnableVertexAttribArray(3);
	}
	
	@Override
	public void unbind()
	{
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(2);
		glDisableVertexAttribArray(3);
		glBindVertexArray(0);
	}
}
