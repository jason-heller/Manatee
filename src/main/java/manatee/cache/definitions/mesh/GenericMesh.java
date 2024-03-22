package manatee.cache.definitions.mesh;

import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Vector3f;

import manatee.cache.definitions.MeshUtil;

public class GenericMesh extends BaseMeshIndexed
{
	protected int vertexCount;
	
	protected Vector3f color;

	public GenericMesh(FloatBuffer positions, IntBuffer indices, Vector3f color)
	{
		load(positions, indices);

		vertexCount = indices.capacity();
		
		this.color = color;
	}
	
	public GenericMesh(float[] positions, int[] indices, Vector3f color)
	{
		load(positions, indices);

		vertexCount = indices.length;

		this.color = color;
	}

	public Vector3f getColor()
	{
		return color;
	}
	
	@Override
	protected void setAttribPointers()
	{
		MeshUtil.attribInterlacedf(3, 2, 3);
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
	}
	
	@Override
	public void unbind()
	{
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(2);
		glBindVertexArray(0);
	}
}
