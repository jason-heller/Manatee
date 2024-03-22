package manatee.cache.definitions.mesh.anim;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_INT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;

import manatee.cache.definitions.mesh.IMesh;

public class AnimatedMesh implements IMesh
{
	private int vao;
	private int positionBuffer;
	private int boneIndexBuffer;
	private int indexBuffer;

	private int vertexCount;
	
	private Vector3f min;
	private Vector3f max;
	
	private Vector3f color;

	private static final int COMP_BYTE_COUNT = (3 + 2 + 3 + 4) * 4;

	public void load(FloatBuffer positions, IntBuffer meshBoneIndices, IntBuffer indices, Vector3f color)
	{
		vao = glGenVertexArrays();
		glBindVertexArray(vao);

		positionBuffer = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, positionBuffer);
		glBufferData(GL_ARRAY_BUFFER, positions, GL_STATIC_DRAW);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, COMP_BYTE_COUNT, 0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, COMP_BYTE_COUNT, 12);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, COMP_BYTE_COUNT, 20);
		glVertexAttribPointer(3, 4, GL_FLOAT, false, COMP_BYTE_COUNT, 32);
		
		boneIndexBuffer = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, boneIndexBuffer);
		glBufferData(GL_ARRAY_BUFFER, meshBoneIndices, GL_STATIC_DRAW);
		GL30.glVertexAttribIPointer(4, 4, GL_INT, 16, 0);

		indexBuffer = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

		vertexCount = indices.capacity();
		
		this.color = color;
	}
	
	public Vector3f getColor()
	{
		return color;
	}
	
	@Override
	public void bind()
	{
		glBindVertexArray(vao);
		
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
		glEnableVertexAttribArray(3);
		glEnableVertexAttribArray(4);
	}

	@Override
	public void unbind()
	{
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(2);
		glDisableVertexAttribArray(3);
		glDisableVertexAttribArray(4);
		
		glBindVertexArray(0);
	}

	@Override
	public int getId()
	{
		return vao;
	}

	@Override
	public int getVertexCount()
	{
		return vertexCount;
	}

	@Override
	public void dispose()
	{
		glDeleteBuffers(indexBuffer);
		glDeleteBuffers(positionBuffer);
		glDeleteBuffers(boneIndexBuffer);
		glDeleteVertexArrays(vao);
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
}
