package manatee.client.gl.particle.mesh;

import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
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
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import manatee.client.gl.Shader;
import manatee.client.gl.camera.ICamera;
import manatee.client.gl.particle.Particle;
import manatee.client.scene.MapScene;

public class MeshParticleRenderer
{
	private static final int MAX_PARTICLES = 5000;
	private static final int INSTANCE_DATA_LENGTH = 20;
	private static final int FLOAT_SIZE = 4;
	private static final int ATTRIB_COUNT = 7;
	
	private static final FloatBuffer buffer = BufferUtils.createFloatBuffer(MAX_PARTICLES * INSTANCE_DATA_LENGTH);
	
	private int pointer;
	
	private Shader shader;
	
	private List<int[]> meshData = new ArrayList<>();

	public MeshParticleRenderer()
	{
		shader = new Shader("scene/particle/mesh.vert", "scene/particle/mesh.frag");
	}
	
	public int addMesh(FloatBuffer vertices, IntBuffer indices)
	{
		int vao = glGenVertexArrays();
		glBindVertexArray(vao);

		// Positions + Normals
		int vertexBuffer = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
		glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
		
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 24, 0);
		glVertexAttribPointer(1, 3, GL_FLOAT, false, 24, 12);
		
		// Indices
		int indexBuffer = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

		// Instances
		int instanceBuffer = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, instanceBuffer);
		glBufferData(GL_ARRAY_BUFFER, FLOAT_SIZE * (INSTANCE_DATA_LENGTH * MAX_PARTICLES), GL_STREAM_DRAW);
		
		addInstancedAttrib(2, 4, INSTANCE_DATA_LENGTH, 0);
		addInstancedAttrib(3, 4, INSTANCE_DATA_LENGTH, 4);
		addInstancedAttrib(4, 4, INSTANCE_DATA_LENGTH, 8);
		addInstancedAttrib(5, 4, INSTANCE_DATA_LENGTH, 12);
		addInstancedAttrib(6, 4, INSTANCE_DATA_LENGTH, 16);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
		
		meshData.add(new int[] {vao, vertexBuffer, indexBuffer, instanceBuffer, indices.capacity()});

		return meshData.size() - 1;
	}

	public void render(MapScene scene, List<MeshParticleSystem> prtSystems)
	{
		ICamera camera = scene.getCamera();
		
		shader.bind();
		shader.setUniform("v_ProjView", camera.getProjectionViewMatrix());
		
		shader.setUniform("v_AmbientColor", scene.getLightColor());
		shader.setUniform("v_AmbientVector", scene.getLightVector());

		for(int i = 0; i < prtSystems.size(); i++)
		{
			MeshParticleSystem ps = prtSystems.get(i);
			addParticleSystem(camera, ps, meshData.get(ps.getMeshIndex()));
		}

		glBindVertexArray(0);

		glBindVertexArray(0);
		glDisable(GL_BLEND);
		glDisable(GL_ALPHA_TEST);
		//glDepthMask(true);
		glUseProgram(0);
	}
	
	private void addParticleSystem(ICamera camera, MeshParticleSystem prtSystem, int[] meshData)
	{
		glBindVertexArray(meshData[0]);
		
		for(int i = 0; i < ATTRIB_COUNT; i++)
			glEnableVertexAttribArray(i);

		glEnable(GL_BLEND);
		glEnable(GL_ALPHA_TEST);
		//glDepthMask(false);
		
		List<Particle> particles = prtSystem.getParticles();

		Iterator<Particle> iter = particles.iterator();
		pointer = 0;
		float[] vboData = new float[particles.size() * INSTANCE_DATA_LENGTH];
		while(iter.hasNext())
		{
			Particle part = iter.next();
			

			if (part.isExpired())
			{
				iter.remove();
				continue;
			}
			
			Vector4f color = part.getColor();	
			
			shader.setUniform("v_Color", color);
			
			buildModelviewMatrix(part.getPosition(), part.getRotation(), part.getScale(), vboData);
			vboData[pointer++] = color.x;
			vboData[pointer++] = color.y;
			vboData[pointer++] = color.z;
			vboData[pointer++] = color.w;

			part.tick();
		}
		
		updateVbo(meshData[3], vboData, buffer);

		glDrawElementsInstanced(GL_TRIANGLES, meshData[4], GL_UNSIGNED_INT, 0, particles.size());
	
		for(int i = 0; i < ATTRIB_COUNT; i++)
			glDisableVertexAttribArray(i);
	}

	private void buildModelviewMatrix(Vector3f pos, AxisAngle4f rot, Vector3f scale, float[] vboData)
	{
		Matrix4f matrix = new Matrix4f();
		matrix.translate(pos);
		matrix.rotate(rot);
		matrix.scale(scale);
		
		vboData[pointer++] = matrix.m00();
		vboData[pointer++] = matrix.m01();
		vboData[pointer++] = matrix.m02();
		vboData[pointer++] = matrix.m03();
		vboData[pointer++] = matrix.m10();
		vboData[pointer++] = matrix.m11();
		vboData[pointer++] = matrix.m12();
		vboData[pointer++] = matrix.m13();
		vboData[pointer++] = matrix.m20();
		vboData[pointer++] = matrix.m21();
		vboData[pointer++] = matrix.m22();
		vboData[pointer++] = matrix.m23();
		vboData[pointer++] = matrix.m30();
		vboData[pointer++] = matrix.m31();
		vboData[pointer++] = matrix.m32();
		vboData[pointer++] = matrix.m33();
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
	
	public void dispose()
	{
		for(int[] mesh : meshData)
		{
			glDeleteBuffers(mesh[1]);
			glDeleteBuffers(mesh[2]);
			glDeleteBuffers(mesh[3]);
			glDeleteVertexArrays(mesh[0]);
		}
		
		shader.dispose();
	}
}
