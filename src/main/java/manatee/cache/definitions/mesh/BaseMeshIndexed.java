package manatee.cache.definitions.mesh;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Vector3f;

/*
 * A mesh with avertex buffer and index buffer, with all relevant data interlaced within the vertex buffer
 * 
 */
public abstract class BaseMeshIndexed implements IMesh
{
	protected int vao = -1;

	protected int vertexBuffer;
	protected int indexBuffer;

	private Vector3f min;
	private Vector3f max;
	
	/*
	 * Loads the given data to OpenGL
	 * 
	 */
	protected void load(float[] vertices, int[] indices)
	{
		vao = glGenVertexArrays();
		glBindVertexArray(vao);

		setVertices(vertices);
		setIndices(indices);

		glBindVertexArray(0);
	}
	
	protected void load(FloatBuffer vertices, IntBuffer indices)
	{
		vao = glGenVertexArrays();
		glBindVertexArray(vao);

		setVertices(vertices);
		setIndices(indices);

		glBindVertexArray(0);
	}
	
	protected void setVertices(float[] vertices)
	{
		vertexBuffer = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);

		glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

		setAttribPointers();

		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	protected void setVertices(FloatBuffer vertices)
	{
		vertexBuffer = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);

		glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

		setAttribPointers();

		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	protected void setIndices(int[] indices)
	{
		indexBuffer = glGenBuffers();

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	protected void setIndices(IntBuffer indices)
	{
		indexBuffer = glGenBuffers();

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	public void setBounds(Vector3f min, Vector3f max)
	{
		this.min = min;
		this.max = max;
	}
	
	public Vector3f getMin()
	{
		return min;
	}
	
	public Vector3f getMax()
	{
		return max;
	}

	protected abstract void setAttribPointers();

	@Override
	public void dispose()
	{
		glDeleteBuffers(vertexBuffer);
		glDeleteBuffers(indexBuffer);
		
		glDeleteVertexArrays(vao);
	}

	@Override
	public void bind()
	{
		glBindVertexArray(vao);
		glEnableVertexAttribArray(0);
	}
	
	@Override
	public void unbind()
	{
		glDisableVertexAttribArray(0);
		glBindVertexArray(0);
	}

	@Override
	public int getId()
	{
		return vao;
	}
}
