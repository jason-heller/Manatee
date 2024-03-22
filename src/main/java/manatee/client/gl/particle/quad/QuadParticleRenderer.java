package manatee.client.gl.particle.quad;

import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL31.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import manatee.client.gl.Shader;
import manatee.client.gl.camera.ICamera;
import manatee.client.gl.particle.Particle;
import manatee.client.gl.particle.ParticleAtlas;
import manatee.maths.MCache;

public class QuadParticleRenderer
{
	private static final float[] VERTICES = new float[]
	{
			-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, -0.5f
	};
	
	private static final int MAX_PARTICLES = 1000;
	private static final int INSTANCE_DATA_LENGTH = 17;
	private static final int FLOAT_SIZE = 4;
	private static final int ATTRIB_COUNT = 6;
	
	private Shader shader;
	
	private int vao, vertexVbo, instanceVbo;
	
	private int pointer;
	
	private static final FloatBuffer buffer = BufferUtils.createFloatBuffer(MAX_PARTICLES * INSTANCE_DATA_LENGTH);

	public QuadParticleRenderer()
	{
		shader = new Shader("shader/particle/quad.vert", "shader/particle/quad.frag");
		
		createMesh();
		createInstancedVbo(INSTANCE_DATA_LENGTH * MAX_PARTICLES);

		addInstancedAttrib(1, 4, INSTANCE_DATA_LENGTH, 0);
		addInstancedAttrib(2, 4, INSTANCE_DATA_LENGTH, 4);
		addInstancedAttrib(3, 4, INSTANCE_DATA_LENGTH, 8);
		addInstancedAttrib(4, 4, INSTANCE_DATA_LENGTH, 12);
		addInstancedAttrib(5, 1, INSTANCE_DATA_LENGTH, 16);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}
	
	public void render(ICamera camera, Collection<QuadParticleSystem> prtSystems)
	{
		shader.bind();
		shader.setUniform("v_Projection", camera.getProjectionMatrix());
		glBindVertexArray(vao);
		
		for(int i = 0; i < ATTRIB_COUNT; i++)
			glEnableVertexAttribArray(i);
		
		glEnable(GL_BLEND);
		glEnable(GL_ALPHA_TEST);
		glDepthMask(false);

		for(QuadParticleSystem prtSystem : prtSystems)
			addParticleSystem(camera, prtSystem.getParticleAtlas(), prtSystem.getParticles());

		for(int i = 0; i < ATTRIB_COUNT; i++)
			glDisableVertexAttribArray(i);
		
		glBindVertexArray(0);
		glDisable(GL_BLEND);
		glDisable(GL_ALPHA_TEST);
		glDepthMask(true);
		glUseProgram(0);
	}
	
	private void addParticleSystem(ICamera camera, ParticleAtlas prtAtlas, List<Particle> particles)
	{
		shader.setTexture("f_Diffuse", prtAtlas.getTexture(), 0);
		shader.setUniform("v_Scale", prtAtlas.getScale());
		
		Iterator<Particle> iter = particles.iterator();
		pointer = 0;
		float[] vboData = new float[particles.size() * INSTANCE_DATA_LENGTH];
		while(iter.hasNext())
		{
			Particle part = iter.next();
			buildModelviewMatrix(part.getPosition(), part.getRotation().angle, part.getScale(), camera.getViewMatrix(), vboData);
			float lifeRatio = part.getLife() / part.getLifeSpan();
			prtAtlas.getTextureCoords(lifeRatio, vboData, pointer);
			pointer += 5;

			part.tick();
			
			if (part.isExpired())
				iter.remove();
		}
		
		updateVbo(instanceVbo, vboData, buffer);

		glDrawArraysInstanced(GL_TRIANGLE_STRIP, 0, 4, particles.size());
	}

	private void createMesh()
	{
		vao = glGenVertexArrays();
		glBindVertexArray(vao);

		vertexVbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vertexVbo);
		glBufferData(GL_ARRAY_BUFFER, VERTICES, GL_STATIC_DRAW);

		glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
	}

	private void createInstancedVbo(int numFloats)
	{
		instanceVbo = glGenBuffers();
		
		glBindBuffer(GL_ARRAY_BUFFER, instanceVbo);
		glBufferData(GL_ARRAY_BUFFER, FLOAT_SIZE * numFloats, GL_STREAM_DRAW);
	}
	
	protected void addInstancedAttrib(int attrib, int dataSize, int stride, int offset)
	{
		glVertexAttribPointer(attrib, dataSize, GL_FLOAT, false, stride * FLOAT_SIZE, offset * FLOAT_SIZE);
		glVertexAttribDivisor(attrib, 1);
	}

	private void updateVbo(int vbo, float[] data, FloatBuffer buffer)
	{
		buffer.clear();
		buffer.put(data);
		buffer.flip();
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, buffer.capacity() * FLOAT_SIZE, GL_STREAM_DRAW);
		glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	private void buildModelviewMatrix(Vector3f pos, float rot, Vector3f scale, Matrix4f viewMatrix, float[] vboData)
	{
		Matrix4f matrix = new Matrix4f();
		
		matrix.translate(pos);
		
		matrix.m00(viewMatrix.m00());
		matrix.m01(viewMatrix.m10());
		matrix.m02(viewMatrix.m20());
		matrix.m10(viewMatrix.m01());
		matrix.m11(viewMatrix.m11());
		matrix.m12(viewMatrix.m21());
		matrix.m20(viewMatrix.m02());
		matrix.m21(viewMatrix.m12());
		matrix.m22(viewMatrix.m22());
		
		matrix.rotate((float) rot, MCache.Z_AXIS);
		matrix.scale(scale);
		
		viewMatrix.mul(matrix, matrix);

		vboData[pointer++] = matrix.m00();
		vboData[pointer++] = matrix.m10();
		vboData[pointer++] = matrix.m20();
		vboData[pointer++] = matrix.m30();
		vboData[pointer++] = matrix.m01();
		vboData[pointer++] = matrix.m11();
		vboData[pointer++] = matrix.m21();
		vboData[pointer++] = matrix.m31();
		vboData[pointer++] = matrix.m02();
		vboData[pointer++] = matrix.m12();
		vboData[pointer++] = matrix.m22();
		vboData[pointer++] = matrix.m32();
	}
	
	public void dispose()
	{
		glDeleteBuffers(vertexVbo);
		glDeleteBuffers(instanceVbo);
		glDeleteVertexArrays(vao);
		
		shader.dispose();
	}
}
