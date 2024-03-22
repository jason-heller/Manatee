package manatee.cache.definitions.mesh;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Vector3f;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.AIVector3D.Buffer;
import org.lwjgl.system.MemoryUtil;

import manatee.maths.MCache;

public class AssimpModel
{
	protected AIMesh[] meshes;
	
	/** The total # of vertices of all meshes added up
	 * 
	 */
	protected int totalVertexCount;
	
	private boolean isTextured;
	
	private Vector3f min;
	private Vector3f max;
	
	public AssimpModel() {}
	
	public AssimpModel(AIMesh[] meshes, boolean isTextured)
	{
		set(meshes);
		this.isTextured = isTextured;
	}
	
	private void set(AIMesh[] meshes)
	{
		this.meshes = meshes;
		
		min = new Vector3f(Float.POSITIVE_INFINITY);
		max = new Vector3f(Float.NEGATIVE_INFINITY);
		
		for(AIMesh mesh : meshes)
		{
			totalVertexCount += mesh.mNumVertices();
			
			AIVector3D mMax = mesh.mAABB().mMax();
			AIVector3D mMin = mesh.mAABB().mMin();
			Vector3f meshMin = new Vector3f(mMin.x(), mMin.y(), mMin.z());
			Vector3f meshMax = new Vector3f(mMax.x(), mMax.y(), mMax.z());
			
			min.min(meshMin);
			max.max(meshMax);
		}
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

	// Assumes index in range
	public AIMesh getMesh(int index)
	{
		return meshes[index];
	}
	
	public int getMeshCount()
	{
		return meshes.length;
	}
	
	public int getVertexCount()
	{
		return totalVertexCount;
	}

	public boolean isTextured()
	{
		return isTextured;
	}

	public void setTextured(boolean isTextured)
	{
		this.isTextured = isTextured;
	}
	
	public boolean isMissing()
	{
		return meshes == null;
	}

	public AIMesh[] getMeshes()
	{
		return meshes;
	}

	public GenericMesh toGenericMesh(int index)
	{
		AIMesh mesh = meshes[index];
		
		FloatBuffer vertices = MemoryUtil.memAllocFloat(mesh.mNumVertices() * 6);
		IntBuffer indices = MemoryUtil.memAllocInt(mesh.mNumFaces() * mesh.mFaces().get(0).mNumIndices());
		
		final Buffer positionBuffer = mesh.mVertices();
		final Buffer normalBuffer = mesh.mNormals();
		
		for(int v = 0; v < mesh.mNumVertices(); v++)
		{
			AIVector3D position = positionBuffer.get(v);
			AIVector3D normal   = normalBuffer.get(v);

			vertices.put(position.x());
			vertices.put(position.y());
			vertices.put(position.z());
			
			vertices.put(normal.x());
			vertices.put(normal.y());
			vertices.put(normal.z());
		}
		
		for(int f = 0; f < mesh.mNumFaces(); f++)
		{
			AIFace face = mesh.mFaces().get(f);
			
			for(int ind = 0; ind < face.mNumIndices(); ind++)
				indices.put(face.mIndices().get(ind));
		}
		
		vertices.flip();
		indices.flip();
		
		GenericMesh genericMesh = new GenericMesh(vertices, indices, MCache.ONE);
		
		MemoryUtil.memFree(vertices);
		MemoryUtil.memFree(indices);
		
		return genericMesh;
	}
}
