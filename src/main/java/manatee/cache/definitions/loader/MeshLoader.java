package manatee.cache.definitions.loader;

import static org.lwjgl.assimp.Assimp.AI_MATKEY_COLOR_DIFFUSE;
import static org.lwjgl.assimp.Assimp.aiGetMaterialColor;
import static org.lwjgl.assimp.Assimp.aiGetMaterialTextureCount;
import static org.lwjgl.assimp.Assimp.aiImportFile;
import static org.lwjgl.assimp.Assimp.aiProcess_CalcTangentSpace;
import static org.lwjgl.assimp.Assimp.aiProcess_FlipUVs;
import static org.lwjgl.assimp.Assimp.aiProcess_GenBoundingBoxes;
import static org.lwjgl.assimp.Assimp.aiProcess_GenSmoothNormals;
import static org.lwjgl.assimp.Assimp.aiProcess_LimitBoneWeights;
import static org.lwjgl.assimp.Assimp.aiProcess_Triangulate;
import static org.lwjgl.assimp.Assimp.aiReleaseImport;
import static org.lwjgl.assimp.Assimp.aiReturn_FAILURE;
import static org.lwjgl.assimp.Assimp.aiReturn_OUTOFMEMORY;
import static org.lwjgl.assimp.Assimp.aiTextureType_DIFFUSE;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.assimp.AIAABB;
import org.lwjgl.assimp.AIAnimation;
import org.lwjgl.assimp.AIBone;
import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMatrix4x4;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIString;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.AIVector3D.Buffer;
import org.lwjgl.assimp.AIVertexWeight;
import org.lwjgl.assimp.Assimp;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import manatee.cache.definitions.loader.component.EmptyAIBuffer3D;
import manatee.cache.definitions.mesh.AnimatedMesh;
import manatee.cache.definitions.mesh.AssimpModel;
import manatee.cache.definitions.mesh.Bone;
import manatee.cache.definitions.mesh.GenericMesh;
import manatee.cache.definitions.mesh.IMesh;
import manatee.cache.exceptions.LoadAssetException;

public class MeshLoader
{

	private static final Logger logger = (Logger) LoggerFactory.getLogger("IO");
	
	private static final int WEIGHT_PER_VERTEX = 4;
	
	private static final int DEFAULT_FLAGS = 
			aiProcess_Triangulate |
			aiProcess_GenSmoothNormals |
			aiProcess_GenBoundingBoxes |
			aiProcess_FlipUVs |
			aiProcess_CalcTangentSpace |
			aiProcess_LimitBoneWeights;

	public static IMesh loadMesh(File file) throws LoadAssetException
	{
		IMesh[] mesh = loadModel(file, 1);
		
		if (mesh == null)
			return null;
		
		return mesh[0];
	}
	
	public static IMesh[] loadModel(File file) throws LoadAssetException
	{
		return loadModel(file, -1);
	}
	
	private static IMesh[] loadModel(File file, int maxMeshes) throws LoadAssetException
	{
		AIScene scene = aiImportFile(file.toString(), DEFAULT_FLAGS);

		// logger.debug("loading mesh: " + file);
		
		if (scene == null)
		{
			logger.error("Failed to import file: " + file);
			return null;
		}

		/*{
			StringBuilder sb = new StringBuilder();
			sb.append("meshes=").append(scene.mNumMeshes());
			sb.append("  |  texs=").append(scene.mNumTextures());
			sb.append("  |  mats=").append(scene.mNumMaterials());
			sb.append("  |  anims=").append(scene.mNumAnimations());
			
			logger.debug(sb.toString());
		}*/
		
		boolean isStatic = (scene.mNumAnimations() == 0);
		
		Vector3f offset = new Vector3f();

		final int sizeOfVertex = 19;
		final int sizeOfVertexUnrigged = 11;
		
		int numMeshes = maxMeshes == -1 ? scene.mNumMeshes() : maxMeshes;
		
		if (maxMeshes != -1 && numMeshes > maxMeshes)
			logger.warn("Loading " + maxMeshes + " of " + scene.mNumMeshes() + ", there may be some data loss. File: " + file);
		
		FloatBuffer[] sceneVertices = new FloatBuffer[numMeshes];
		IntBuffer[] sceneIndices = new IntBuffer[numMeshes];
		
		Vector3f[] mins = new Vector3f[numMeshes];
		Vector3f[] maxs = new Vector3f[numMeshes];
		
		Vector3f sceneMax = new Vector3f(Float.NEGATIVE_INFINITY);
		Vector3f sceneMin = new Vector3f(Float.POSITIVE_INFINITY);
		
		Bone[][] sceneBones = new Bone[numMeshes][];
		Matrix4f[] inverseRootTransformations = new Matrix4f[numMeshes];
		
		List<String> texturePaths = new LinkedList<String>();
		
		AIMesh[] meshes = new AIMesh[numMeshes];

		// bbox
		for(int meshId = 0; meshId < numMeshes; meshId++)
		{
			AIMesh mesh = AIMesh.create(scene.mMeshes().get(meshId));
			meshes[meshId] = mesh;
			
			AIAABB aabb = mesh.mAABB();
			AIVector3D aabbMax = aabb.mMax();
			AIVector3D aabbMin = aabb.mMin();

			maxs[meshId] = new Vector3f(aabbMax.x(), aabbMax.y(), aabbMax.z());
			mins[meshId] = new Vector3f(aabbMin.x(), aabbMin.y(), aabbMin.z());
			
			sceneMax.max(maxs[meshId]);
			sceneMin.min(mins[meshId]);
		}
		
		offset.set(
				sceneMin.x() + ((sceneMax.x() - sceneMin.x()) * 0.5f),
				sceneMin.y() + ((sceneMax.y() - sceneMin.y()) * 0.5f),
				sceneMin.z() + ((sceneMax.z() - sceneMin.z()) * 0.5f)
				);
		
		for(int meshId = 0; meshId < numMeshes; meshId++)
		{
			AIMesh mesh = meshes[meshId];
			
			// Material
			int mtlId = mesh.mMaterialIndex();
			AIMaterial mtl = AIMaterial.create(scene.mMaterials().get(mtlId));
			
			AIColor4D mtlDiffuse = AIColor4D.create();
			int success = aiGetMaterialColor(mtl, AI_MATKEY_COLOR_DIFFUSE, 0, 0, mtlDiffuse);
			
			if (success == aiReturn_FAILURE)
				logger.error("Load mesh failure (mtl color): aiReturn_FAILURE ");
			
			if (success == aiReturn_OUTOFMEMORY)
				logger.error("Load mesh failure (mtl color): aiReturn_OUTOFMEMORY ");

			
			// Texture
			int texCount = aiGetMaterialTextureCount(mtl, aiTextureType_DIFFUSE);
			
			if (texCount > 1)
				logger.warn("Models with more than one texture per model currently not supported");
			
			int texId = 0;
			
			if (texCount != 0)
			{
				// AIMaterialProperty
				AIString mtlPath = AIString.calloc();
			    success = Assimp.aiGetMaterialTexture(mtl, aiTextureType_DIFFUSE, 0, mtlPath,
			    		(IntBuffer) null, null, null, null, null, null);
				
				if (success == aiReturn_FAILURE)
				{
					logger.error("Load mesh failure (mtl tex): aiReturn_FAILURE ");
					return null;
				}
				
				String path = mtlPath.dataString();
				
				if (path != null && path.length() > 0) {
					if (!texturePaths.contains(path))
						texturePaths.add(path);
					
					texId = texturePaths.indexOf(path);
				}
			}
			
			boolean isTextured = (scene.mNumTextures() == 0);

			// boolean hasColors = (mesh.mColors(0) != null);
			
			
			// Vertices
			final Buffer positionBuffer = mesh.mVertices();
			final Buffer textureBuffer = isTextured ? mesh.mTextureCoords(texId) : EmptyAIBuffer3D.INSTANCE;
			final Buffer normalBuffer = mesh.mNormals();
			// final IColorBufferWrapper cBuf = hasColors ? new ColorBufferWrapper(mesh.mColors(0)) : new EmptyColorBuffer();
			// final Buffer tangentBuffer = mesh.mTangents();
			
			final int boneWeightCount = isStatic ? 0 : WEIGHT_PER_VERTEX;
			final int vertexBufSize = isStatic ? sizeOfVertexUnrigged : sizeOfVertex;
			
			FloatBuffer vertices = MemoryUtil.memAllocFloat(mesh.mNumVertices() * vertexBufSize);
			IntBuffer indices = MemoryUtil.memAllocInt(mesh.mNumFaces() * mesh.mFaces().get(0).mNumIndices());
			
			sceneVertices[meshId] = vertices;
			sceneIndices[meshId] = indices;

			for(int v = 0; v < mesh.mNumVertices(); v++)
			{
				AIVector3D position = positionBuffer.get(v);
				AIVector3D texCoord = textureBuffer.get(v);
				AIVector3D normal   = normalBuffer.get(v);
				// Vector3f color  = cBuf.get(v);

				vertices.put(position.x() - offset.x);
				vertices.put(position.y() - offset.y);
				vertices.put(position.z() - offset.z);

				vertices.put(texCoord.x());
				vertices.put(1f - texCoord.y());
				
				vertices.put(normal.x());
				vertices.put(normal.y());
				vertices.put(normal.z());

				// TODO: Remove
				vertices.put(mtlDiffuse.r());
				vertices.put(mtlDiffuse.g());
				vertices.put(mtlDiffuse.b());
				
				for(int i = 0; i < boneWeightCount; i++) {
					vertices.put(0);
					vertices.put(0);
				}
			}

			for(int f = 0; f < mesh.mNumFaces(); f++)
			{
				AIFace face = mesh.mFaces().get(f);
				
				for(int ind = 0; ind < face.mNumIndices(); ind++)
					indices.put(face.mIndices().get(ind));
			}
			
			if (isStatic)
			{
				vertices.flip();
				indices.flip();
				continue;
			}
			
			
			// Animation
			HashMap<String, Integer> boneMap = new HashMap<>();
			HashMap<Integer, Integer> bone_index_map0 = new HashMap<>();
			HashMap<Integer, Integer> bone_index_map1 = new HashMap<>();

			for(int b = 0; b < mesh.mNumBones(); b++)
			{
				AIBone bone = AIBone.create(mesh.mBones().get(b));
				boneMap.put(bone.mName().dataString(), b);

				for(int w = 0; w < bone.mNumWeights(); w++)
				{
					AIVertexWeight weight = bone.mWeights().get(w);
					int vertexIndex = weight.mVertexId();
					int findex = vertexIndex * sizeOfVertex;

					if(!bone_index_map0.containsKey(vertexIndex))
					{
						vertices.put(findex + sizeOfVertexUnrigged + 0, b);
						vertices.put(findex + sizeOfVertexUnrigged + 2, weight.mWeight());
						bone_index_map0.put(vertexIndex, 0);

					}
					else if(bone_index_map0.get(vertexIndex) == 0)
					{
						vertices.put(findex + sizeOfVertexUnrigged + 1, b);
						vertices.put(findex + sizeOfVertexUnrigged + 3, weight.mWeight());
						bone_index_map0.put(vertexIndex, 1);

					}
					else if(!bone_index_map1.containsKey(vertexIndex))
					{
						vertices.put(findex + sizeOfVertexUnrigged + 4, b);
						vertices.put(findex + sizeOfVertexUnrigged + 6, weight.mWeight());
						bone_index_map1.put(vertexIndex, 0);

					}
					else if(bone_index_map1.get(vertexIndex) == 0)
					{
						vertices.put(findex + sizeOfVertexUnrigged + 5, b);
						vertices.put(findex + sizeOfVertexUnrigged + 7, weight.mWeight());
						bone_index_map1.put(vertexIndex, 1);
						
					} else {
						MemoryUtil.memFree(indices);
						throw new LoadAssetException("Max weight is " + Integer.toString(WEIGHT_PER_VERTEX) + " bones per vertex");
					}
				}
			}
			
			AIMatrix4x4 inverseRootTransform = scene.mRootNode().mTransformation();
			inverseRootTransformations[meshId] = LoaderUtil.getAIMatrix(inverseRootTransform);

			Bone bones[] = new Bone[boneMap.size()];
			sceneBones[meshId] = bones;

			for(int b = 0; b < mesh.mNumBones(); b++)
			{
				AIBone bone = AIBone.create(mesh.mBones().get(b));
				bones[b] = new Bone();

				bones[b].name = bone.mName().dataString();
				bones[b].offset = LoaderUtil.getAIMatrix(bone.mOffsetMatrix());
			}

			vertices.flip();
			indices.flip();
		}

		
		// Export
		IMesh[] parsed = new IMesh[numMeshes];
		
		if (isStatic)
		{
			for(int i = 0; i < numMeshes; i++)
			{
				GenericMesh staticMesh = new GenericMesh(sceneVertices[i], sceneIndices[i]);

				staticMesh.setBounds(mins[i], maxs[i]);
				
				parsed[i] = staticMesh;

				MemoryUtil.memFree(sceneVertices[i]);
				MemoryUtil.memFree(sceneIndices[i]);
			}
			
			aiReleaseImport(scene);
			
			return parsed;
		}

		for(int i = 0; i < numMeshes; i++)
		{
			AnimatedMesh animatedMesh = new AnimatedMesh();

			animatedMesh.load(sceneVertices[i], sceneIndices[i]);
			animatedMesh.setBounds(mins[i], maxs[i]);
			animatedMesh.animation = AIAnimation.create(scene.mAnimations().get(0));
			animatedMesh.bones = sceneBones[i];
			animatedMesh.boneTransforms = new Matrix4f[sceneBones[i].length];
			animatedMesh.root = scene.mRootNode();
			animatedMesh.globalInverseTransform = inverseRootTransformations[i];

			parsed[i] = animatedMesh;

			MemoryUtil.memFree(sceneVertices[i]);
			MemoryUtil.memFree(sceneIndices[i]);
		}

		aiReleaseImport(scene);
		
		return parsed;
	}

	public static AssimpModel getAssimpMeshes(File file) throws LoadAssetException
	{
		AIScene scene = aiImportFile(file.toString(), DEFAULT_FLAGS);

		if (scene == null)
			throw new LoadAssetException("Failed to import file: " + file.getAbsolutePath());

		boolean isTextured = (scene.mNumTextures() == 0);

		AIMesh[] meshes = new AIMesh[scene.mNumMeshes()];
		
		for(int i = 0; i < meshes.length; i++)
			meshes[i] = AIMesh.create(scene.mMeshes().get(i));
		
		return new AssimpModel(meshes, isTextured);
	}
}
