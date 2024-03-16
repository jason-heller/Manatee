package manatee.client.gl.mesh;

import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

import org.joml.Vector3f;

import manatee.cache.definitions.MeshUtil;
import manatee.cache.definitions.mesh.BaseMesh;
import manatee.maths.Maths;

public class HeightFieldMesh extends BaseMesh
{

	protected int vertexCount;
	
	public HeightFieldMesh(float[][] heights, int width, int height)
	{
		// Assuming more data will need to be loaded like (like UV data for multitexturing)
		// Hence why we aren't just using the heights array
		int tileCount = height - 1;
		
		vertexCount = tileCount + (tileCount - 1) + (width * tileCount * 2);
		
		float[] vertices = new float[vertexCount * 4];
		
		int v = 0;
		
		Vector3f normal = new Vector3f();

		for (int y = 0; y < tileCount; y++)
		{
			vertices[v++] = Float.NaN;
			vertices[v++] = 0;
			vertices[v++] = 0;
			vertices[v++] = 0;

			for (int x = 0; x < width; x++)
			{
				vertices[v++] = heights[x][y];
				calcBrightness(normal, heights, x, y);
				vertices[v++] = normal.x;
				vertices[v++] = normal.y;
				vertices[v++] = normal.z;
				
				vertices[v++] = heights[x][y + 1];
				calcBrightness(normal, heights, x, y + 1);
				vertices[v++] = normal.x;
				vertices[v++] = normal.y;
				vertices[v++] = normal.z;
			}

			if (y != tileCount - 1)
			{
				vertices[v++] = Float.NaN;
				vertices[v++] = 0;
				vertices[v++] = 0;
				vertices[v++] = 0;
			}

		}

		load(vertices);
	}

	private void calcBrightness(Vector3f normal, float[][] heights, int x, int y)
	{
		float left 	= getHeight(heights, x-1, y);
		float right = getHeight(heights, x+1, y);
		float up 	= getHeight(heights, x, y-1);
		float down 	= getHeight(heights, x, y+1);
		
		normal.set(left - right, down - up, 2f);
		//normal.mul(1f / normal.lengthSquared());	// Approximate normal
		normal.normalize();
	}
	
	private float getHeight(float[][] heights, int x, int y)
	{
		int arrX = Maths.clamp(x, 0, heights.length - 1);
		int arrY = Maths.clamp(y, 0, heights[0].length - 1);
		
		return heights[arrX][arrY];
	}
	
	@Override
	public void bind()
	{
		glBindVertexArray(vao);
		
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
	}
	
	@Override
	public void unbind()
	{
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		
		glBindVertexArray(0);
	}

	@Override
	protected void setAttribPointers()
	{
		//glVertexAttribPointer(0, 1, GL_FLOAT, false, 16, 0);
		//glVertexAttribPointer(1, 12, GL_FLOAT, false, 16, 4);
		MeshUtil.attribInterlacedf(1, 3);
	}

	@Override
	public int getVertexCount()
	{
		return vertexCount;
	}

}
