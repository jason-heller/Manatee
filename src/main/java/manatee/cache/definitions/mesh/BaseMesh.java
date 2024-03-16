package manatee.cache.definitions.mesh;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
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

import org.joml.Vector3f;

/*
 * A mesh with one vertex buffer, with all relevant data interlaced within
 * 
 */
public abstract class BaseMesh implements IMesh
{

	protected int vao = -1;

	protected int vertexBuffer;
	
	private Vector3f min;
	private Vector3f max;

	/*
	 * Loads the given data to OpenGL
	 * 
	 */
	protected void load(float[] positions)
	{
		vao = glGenVertexArrays();
		glBindVertexArray(vao);

		setVertices(positions);

		glBindVertexArray(0);
	}
	
	/*
	 * Loads the given data to OpenGL
	 * 
	 */
	protected void load(FloatBuffer positions)
	{
		vao = glGenVertexArrays();
		glBindVertexArray(vao);

		setVertices(positions);

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

	@Override
	public void dispose()
	{
		glDeleteBuffers(vertexBuffer);
		glDeleteVertexArrays(vao);
	}

}
