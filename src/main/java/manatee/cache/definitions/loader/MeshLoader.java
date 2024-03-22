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
import static org.lwjgl.assimp.Assimp.aiProcess_PopulateArmatureData;
import static org.lwjgl.assimp.Assimp.aiProcess_Triangulate;
import static org.lwjgl.assimp.Assimp.aiReleaseImport;
import static org.lwjgl.assimp.Assimp.aiReturn_FAILURE;
import static org.lwjgl.assimp.Assimp.aiReturn_OUTOFMEMORY;
import static org.lwjgl.assimp.Assimp.aiTextureType_DIFFUSE;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIAABB;
import org.lwjgl.assimp.AIAnimation;
import org.lwjgl.assimp.AIBone;
import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMatrix4x4;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AINodeAnim;
import org.lwjgl.assimp.AIQuatKey;
import org.lwjgl.assimp.AIQuaternion;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIString;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.AIVector3D.Buffer;
import org.lwjgl.assimp.AIVectorKey;
import org.lwjgl.assimp.AIVertexWeight;
import org.lwjgl.assimp.Assimp;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import manatee.cache.definitions.loader.component.EmptyAIBuffer3D;
import manatee.cache.definitions.mesh.AssimpModel;
import manatee.cache.definitions.mesh.Bone;
import manatee.cache.definitions.mesh.GenericMesh;
import manatee.cache.definitions.mesh.IMesh;
import manatee.cache.definitions.mesh.MeshGroup;
import manatee.cache.definitions.mesh.anim.AnimFrame;
import manatee.cache.definitions.mesh.anim.AnimNode;
import manatee.cache.definitions.mesh.anim.AnimatedMesh;
import manatee.cache.definitions.mesh.anim.MeshAnimation;
import manatee.cache.definitions.mesh.anim.VertexWeight;
import manatee.cache.exceptions.LoadAssetException;
import manatee.maths.MCache;

public class MeshLoader
{

	private static final Logger logger = (Logger) LoggerFactory.getLogger("IO");

	private static final int WEIGHT_PER_VERTEX = 4;
	
	public static final int MAX_BONES = 25;

	private static final int DEFAULT_FLAGS = aiProcess_Triangulate | aiProcess_GenSmoothNormals
			| aiProcess_GenBoundingBoxes | aiProcess_FlipUVs | aiProcess_CalcTangentSpace | aiProcess_LimitBoneWeights | aiProcess_PopulateArmatureData;

	public static IMesh loadMesh(File file) throws LoadAssetException
	{
		MeshGroup meshes = loadModel(file, 1);

		if (meshes == null)
			return null;

		return meshes.getMeshes()[0];
	}

	public static MeshGroup loadModel(File file) throws LoadAssetException
	{
		return loadModel(file, -1);
	}

	private static MeshGroup loadModel(File file, int maxMeshes) throws LoadAssetException
	{
		AIScene scene = aiImportFile(file.toString(), DEFAULT_FLAGS);

		logger.debug("loading mesh: " + file);

		if (scene == null)
		{
			logger.error("Failed to import file: " + file);
			return null;
		}

		{
			StringBuilder sb = new StringBuilder();
			sb.append("meshes=").append(scene.mNumMeshes());
			sb.append("  |  texs=").append(scene.mNumTextures());
			sb.append("  |  mats=").append(scene.mNumMaterials());
			sb.append("  |  anims=").append(scene.mNumAnimations());

			logger.debug(sb.toString());
		}

		boolean isStatic = (scene.mNumAnimations() == 0);

		Vector3f offset = new Vector3f();

		final int sizeOfVertexUnrigged = 8;
		final int sizeOfVertexRigged = sizeOfVertexUnrigged + WEIGHT_PER_VERTEX;

		int numMeshes = maxMeshes == -1 ? scene.mNumMeshes() : maxMeshes;

		if (maxMeshes != -1 && numMeshes > maxMeshes)
			logger.warn("Loading " + maxMeshes + " of " + scene.mNumMeshes() + ", there may be some data loss. File: " + file);

		AIMesh[] meshes = new AIMesh[numMeshes];
		List<String> texturePaths = new LinkedList<String>();
		
		FloatBuffer[] sceneVertices = new FloatBuffer[numMeshes];
		IntBuffer[] sceneIndices = new IntBuffer[numMeshes];
		IntBuffer[] sceneBoneIndices = null;
		Vector3f[] sceneColors = new Vector3f[numMeshes];
		
		List<Bone> allBones = new ArrayList<>();
		Map<String, Integer> boneIndices = new HashMap<>();
		
		// Get skeleton
		if (!isStatic)
		{
			sceneBoneIndices = new IntBuffer[numMeshes];
		}
		
		Map<String, MeshAnimation> animations = null;
		AnimNode animRootNode = null;

		// Get bounding box
		Vector3f[] mins = new Vector3f[numMeshes];
		Vector3f[] maxs = new Vector3f[numMeshes];

		Vector3f sceneMax = new Vector3f(Float.NEGATIVE_INFINITY);
		Vector3f sceneMin = new Vector3f(Float.POSITIVE_INFINITY);

		for (int meshId = 0; meshId < numMeshes; meshId++)
		{
			AIMesh mesh = AIMesh.create(scene.mMeshes().get(meshId));
			// Sort alphabetically by name to make assigning textures easier
			String meshName = mesh.mName().dataString();
			int i = 0;
			for(; i < numMeshes; i++)
			{
				if (meshes[i] == null || meshName.compareTo(meshes[i].mName().dataString()) < 0)
					break;
			}
			
			for(int j = numMeshes - 1; j != i; j--)
				meshes[j] = meshes[j-1];
			
			meshes[i] = mesh;

			AIAABB aabb = mesh.mAABB();
			AIVector3D aabbMax = aabb.mMax();
			AIVector3D aabbMin = aabb.mMin();

			maxs[meshId] = new Vector3f(aabbMax.x(), aabbMax.y(), aabbMax.z());
			mins[meshId] = new Vector3f(aabbMin.x(), aabbMin.y(), aabbMin.z());

			sceneMax.max(maxs[meshId]);
			sceneMin.min(mins[meshId]);
		}

		if (isStatic)
		{
			offset.set(sceneMin.x() + ((sceneMax.x() - sceneMin.x()) * 0.5f),
					sceneMin.y() + ((sceneMax.y() - sceneMin.y()) * 0.5f),
					sceneMin.z() + ((sceneMax.z() - sceneMin.z()) * 0.5f));
		}
		
		//sceneMin.sub(offset);
		//sceneMax.sub(offset);
		
		// Process each mesh
		for (int meshId = 0; meshId < numMeshes; meshId++)
		{
			AIMesh mesh = meshes[meshId];
			
			// Material
			int mtlId = mesh.mMaterialIndex();
			AIMaterial mtl = AIMaterial.create(scene.mMaterials().get(mtlId));

			AIColor4D mtlDiffuse = AIColor4D.create();
			int success = aiGetMaterialColor(mtl, AI_MATKEY_COLOR_DIFFUSE, 0, 0, mtlDiffuse);
			
			sceneColors[meshId] = new Vector3f(mtlDiffuse.r(), mtlDiffuse.g(), mtlDiffuse.b());

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
				success = Assimp.aiGetMaterialTexture(mtl, aiTextureType_DIFFUSE, 0, mtlPath, (IntBuffer) null, null,
						null, null, null, null);

				if (success == aiReturn_FAILURE)
				{
					logger.error("Load mesh failure (mtl tex): aiReturn_FAILURE ");
					return null;
				}

				String path = mtlPath.dataString();

				if (path != null && path.length() > 0)
				{
					if (!texturePaths.contains(path))
						texturePaths.add(path);

					texId = texturePaths.indexOf(path);
				}
			}

			boolean isTextured = (scene.mNumTextures() == 0);
			
			// boolean hasColors = (mesh.mColors(0) != null);

			// Animation data
			Map<Integer, List<VertexWeight>> weightSet = new HashMap<>();

			for (int b = 0; b < mesh.mNumBones(); b++)
			{
				AIBone aiBone = AIBone.create(mesh.mBones().get(b));
				Bone bone = new Bone();
				bone.name = aiBone.mName().dataString();
				bone.inverseBindMatrix = toMatrix(aiBone.mOffsetMatrix());
				bone.aiNode = aiBone.mNode();
				
				int index = boneIndices.keySet().size();
				
				//bone.id = index;
				allBones.add(bone);
				
				if (!boneIndices.containsKey(bone.name))
					boneIndices.put(bone.name, index);
				else
					index = boneIndices.get(bone.name);
				
				int numWeights = aiBone.mNumWeights();
				AIVertexWeight.Buffer aiWeights = aiBone.mWeights();
				for (int j = 0; j < numWeights; j++)
				{
					AIVertexWeight aiWeight = aiWeights.get(j);
					VertexWeight vw = new VertexWeight(index, aiWeight.mVertexId(), aiWeight.mWeight());
					List<VertexWeight> vertexWeightList = weightSet.get(vw.vertexId);
					if (vertexWeightList == null)
					{
						vertexWeightList = new ArrayList<>();
						weightSet.put(vw.vertexId, vertexWeightList);
					}
					vertexWeightList.add(vw);
				}
			}
			
			// Vertices
			final Buffer positionBuffer = mesh.mVertices();
			final Buffer textureBuffer = isTextured ? mesh.mTextureCoords(texId) : EmptyAIBuffer3D.INSTANCE;
			final Buffer normalBuffer = mesh.mNormals();

			final int vertexBufSize = isStatic ? sizeOfVertexUnrigged : sizeOfVertexRigged;

			FloatBuffer vertices = MemoryUtil.memAllocFloat(mesh.mNumVertices() * vertexBufSize);
			IntBuffer indices = MemoryUtil.memAllocInt(mesh.mNumFaces() * mesh.mFaces().get(0).mNumIndices());
			IntBuffer meshBoneIndices = isStatic ? null : MemoryUtil.memAllocInt(mesh.mNumVertices() * WEIGHT_PER_VERTEX);

			sceneVertices[meshId] = vertices;
			sceneIndices[meshId] = indices;

			for (int v = 0; v < mesh.mNumVertices(); v++)
			{
				AIVector3D position = positionBuffer.get(v);
				AIVector3D texCoord = textureBuffer.get(v);
				AIVector3D normal = normalBuffer.get(v);

				vertices.put(position.x() - offset.x);
				vertices.put(position.y() - offset.y);
				vertices.put(position.z() - offset.z);

				vertices.put(texCoord.x());
				vertices.put(texCoord.y());

				vertices.put(normal.x());
				vertices.put(normal.y());
				vertices.put(normal.z());

				if (isStatic)
					continue;
				
				List<VertexWeight> vertexWeightList = weightSet.get(v);
				int size = vertexWeightList != null ? vertexWeightList.size() : 0;
				for (int j = 0; j < WEIGHT_PER_VERTEX; j++)
				{
					if (j < size)
					{
						VertexWeight vw = vertexWeightList.get(j);
						vertices.put(vw.weight);
						meshBoneIndices.put(vw.id);
					}
					else
					{
						vertices.put(0f);
						meshBoneIndices.put(0);
					}
				}
			}

			for (int f = 0; f < mesh.mNumFaces(); f++)
			{
				AIFace face = mesh.mFaces().get(f);

				for (int ind = 0; ind < face.mNumIndices(); ind++)
					indices.put(face.mIndices().get(ind));
			}

			
			vertices.flip();
			indices.flip();
			
			// Static meshes stop here, after this block is animation-only mesh related
			if (isStatic)
				continue;
			
			meshBoneIndices.flip();
			sceneBoneIndices[meshId] = meshBoneIndices;
		}
		
		// This is the large bulk of animation processing
		AINode root = getRoot(scene, allBones);
		
		animRootNode = buildNodesTree(root, null);
		Matrix4f globalInvTransform = toMatrix(root.mTransformation()).invert();
		animations = processAnimations(scene, allBones, boneIndices, animRootNode, globalInvTransform);
		
		// printRoots("", animRootNode);

		// Parse all the meshes
		IMesh[] parsedMeshes = new IMesh[numMeshes];

		// Parse the base mesh
		if (isStatic)
		{
			for (int i = 0; i < numMeshes; i++)
			{
				GenericMesh staticMesh = new GenericMesh(sceneVertices[i], sceneIndices[i], sceneColors[i]);

				staticMesh.setBounds(mins[i], maxs[i]);

				parsedMeshes[i] = staticMesh;

				MemoryUtil.memFree(sceneVertices[i]);
				MemoryUtil.memFree(sceneIndices[i]);
			}

			aiReleaseImport(scene);

			return new MeshGroup(parsedMeshes);
		}
		
		// Parse animations
		for (int i = 0; i < numMeshes; i++)
		{
			
			AnimatedMesh animatedMesh = new AnimatedMesh();
			animatedMesh.load(sceneVertices[i], sceneBoneIndices[i], sceneIndices[i], sceneColors[i]);
			animatedMesh.setBounds(mins[i], maxs[i]);

			parsedMeshes[i] = animatedMesh;

			MemoryUtil.memFree(sceneVertices[i]);
			MemoryUtil.memFree(sceneIndices[i]);
			MemoryUtil.memFree(sceneBoneIndices[i]);
		}

		aiReleaseImport(scene);
		
		MeshGroup meshGroup = new MeshGroup(parsedMeshes, animations, animRootNode);

		return meshGroup;
	}

	/*private static void printRoots(String sp, AnimNode node)
	{
		for(AnimNode ch : node.children)
		{
			System.out.println(sp + " " + ch.name);
			printRoots(sp + "  ", ch);
		}
	}*/

	public static AssimpModel getAssimpMeshes(File file) throws LoadAssetException
	{
		AIScene scene = aiImportFile(file.toString(), DEFAULT_FLAGS);

		if (scene == null)
			throw new LoadAssetException("Failed to import file: " + file.getAbsolutePath());

		boolean isTextured = (scene.mNumTextures() == 0);

		AIMesh[] meshes = new AIMesh[scene.mNumMeshes()];

		for (int i = 0; i < meshes.length; i++)
			meshes[i] = AIMesh.create(scene.mMeshes().get(i));

		return new AssimpModel(meshes, isTextured);
	}
	
	private static AINode getRoot(AIScene scene, List<Bone> allBones)
	{
		AINode root = scene.mRootNode();

		for(Bone bone : allBones)
		{
			if (isRoot(bone, allBones))
				return bone.aiNode;
		}
		
		return root;
	}

	private static boolean isRoot(Bone bone, List<Bone> allBones)
	{
		for(Bone other : allBones)
		{
			if (other == bone)
				continue;
			
			if (bone.aiNode.mParent() == other.aiNode)
				return false;
		}
		
		return true;
	}

	private static AnimNode buildNodesTree(AINode aiNode, AnimNode parentNode)
	{
		String nodeName = aiNode.mName().dataString();
		AnimNode node = new AnimNode(nodeName, parentNode, toMatrix(aiNode.mTransformation()));

		int numChildren = aiNode.mNumChildren();
		PointerBuffer aiChildren = aiNode.mChildren();
		for (int i = 0; i < numChildren; i++)
		{
			
			AINode aiChildNode = AINode.create(aiChildren.get(i));
			AnimNode childNode = buildNodesTree(aiChildNode, node);
			node.addChild(childNode);
		}
		return node;
	}

	private static Map<String, MeshAnimation> processAnimations(AIScene aiScene, List<Bone> bones, Map<String, Integer> boneIndices, AnimNode rootNode,
			Matrix4f globalInverseTransformation)
	{
		Map<String, MeshAnimation> animations = new HashMap<>();

		int numAnimations = aiScene.mNumAnimations();
		PointerBuffer aiAnimations = aiScene.mAnimations();
		for (int i = 0; i < numAnimations; i++)
		{
			AIAnimation aiAnimation = AIAnimation.create(aiAnimations.get(i));
			int maxFrames = calcAnimFrameCount(aiAnimation);

			List<AnimFrame> frames = new ArrayList<>();
			for (int j = 0; j < maxFrames; j++)
			{
				Matrix4f[] boneMatrices = new Matrix4f[MAX_BONES];
				Arrays.fill(boneMatrices, MCache.MAT4_IDENTITY);
				
				AnimFrame animatedFrame = new AnimFrame(boneMatrices);
				
				buildFrameMatrices(aiAnimation, bones, boneIndices, animatedFrame, j, rootNode, rootNode.matrix, globalInverseTransformation);
				
				frames.add(animatedFrame);
			}
			
			MeshAnimation animation = new MeshAnimation((float)aiAnimation.mDuration(), frames);
			animations.put(aiAnimation.mName().dataString(), animation);
		}
		return animations;
	}

	private static int calcAnimFrameCount(AIAnimation aiAnimation)
	{
		int maxFrames = 0;
		int numNodeAnims = aiAnimation.mNumChannels();
		PointerBuffer aiChannels = aiAnimation.mChannels();
		for (int i = 0; i < numNodeAnims; i++)
		{
			AINodeAnim aiNodeAnim = AINodeAnim.create(aiChannels.get(i));
			int numFrames = Math.max(Math.max(aiNodeAnim.mNumPositionKeys(), aiNodeAnim.mNumScalingKeys()),
					aiNodeAnim.mNumRotationKeys());
			maxFrames = Math.max(maxFrames, numFrames);
		}

		return maxFrames + 1;
	}

	private static void buildFrameMatrices(AIAnimation aiAnimation, List<Bone> bones,
			Map<String, Integer> boneIndices, AnimFrame animatedFrame, int frame, AnimNode node, Matrix4f parentTransformation,
			Matrix4f globalInverseTransform)
	{
		String nodeName = node.name;
		AINodeAnim aiNodeAnim = findAIAnimNode(aiAnimation, nodeName);
		Matrix4f nodeTransform = node.matrix;
		
		if (aiNodeAnim != null)
			nodeTransform = buildNodeTransformationMatrix(aiNodeAnim, frame);
		
		Matrix4f nodeGlobalTransform = new Matrix4f(parentTransformation).mul(nodeTransform);
		
		List<Bone> affectedBones = bones.stream().filter(b -> b.name.equals(nodeName)).toList();

		for (Bone bone : affectedBones)
		{
			Matrix4f boneTransform = new Matrix4f(globalInverseTransform).mul(nodeGlobalTransform).mul(bone.inverseBindMatrix);
			animatedFrame.boneMatrices[boneIndices.get(nodeName)] = boneTransform;
		}

		for (AnimNode childNode : node.children)
		{
			buildFrameMatrices(aiAnimation, bones, boneIndices, animatedFrame, frame, childNode, nodeGlobalTransform,
					globalInverseTransform);
		}
	}

	private static AINodeAnim findAIAnimNode(AIAnimation aiAnimation, String nodeName)
	{
		AINodeAnim result = null;
		int numAnimNodes = aiAnimation.mNumChannels();
		PointerBuffer aiChannels = aiAnimation.mChannels();
		for (int i = 0; i < numAnimNodes; i++)
		{
			AINodeAnim aiNodeAnim = AINodeAnim.create(aiChannels.get(i));
			if (nodeName.equals(aiNodeAnim.mNodeName().dataString()))
			{
				result = aiNodeAnim;
				break;
			}
		}
		return result;
	}
	
	private static Matrix4f buildNodeTransformationMatrix(AINodeAnim aiNodeAnim, int frame)
	{
		AIVectorKey.Buffer positionKeys = aiNodeAnim.mPositionKeys();
		AIVectorKey.Buffer scalingKeys = aiNodeAnim.mScalingKeys();
		AIQuatKey.Buffer rotationKeys = aiNodeAnim.mRotationKeys();

		AIVectorKey aiVecKey;
		AIVector3D vec;
		
		Matrix4f nodeTransform = new Matrix4f();
		
		int numPositions = aiNodeAnim.mNumPositionKeys();
		if (numPositions > 0)
		{
			aiVecKey = positionKeys.get(Math.min(numPositions - 1, frame));
			vec = aiVecKey.mValue();
			nodeTransform.translate(vec.x(), vec.y(), vec.z());
		}
		int numRotations = aiNodeAnim.mNumRotationKeys();
		if (numRotations > 0)
		{
			AIQuatKey quatKey = rotationKeys.get(Math.min(numRotations - 1, frame));
			AIQuaternion aiQuat = quatKey.mValue();
			Quaternionf quat = new Quaternionf(aiQuat.x(), aiQuat.y(), aiQuat.z(), aiQuat.w());
			nodeTransform.rotate(quat);
		}
		int numScalingKeys = aiNodeAnim.mNumScalingKeys();
		if (numScalingKeys > 0)
		{
			aiVecKey = scalingKeys.get(Math.min(numScalingKeys - 1, frame));
			vec = aiVecKey.mValue();
			nodeTransform.scale(vec.x(), vec.y(), vec.z());
		}

		return nodeTransform;
	}
	
	private static Matrix4f toMatrix(AIMatrix4x4 aiMatrix4x4)
	{
		Matrix4f result = new Matrix4f();
		
		result.m00(aiMatrix4x4.a1());
		result.m10(aiMatrix4x4.a2());
		result.m20(aiMatrix4x4.a3());
		result.m30(aiMatrix4x4.a4());
		result.m01(aiMatrix4x4.b1());
		result.m11(aiMatrix4x4.b2());
		result.m21(aiMatrix4x4.b3());
		result.m31(aiMatrix4x4.b4());
		result.m02(aiMatrix4x4.c1());
		result.m12(aiMatrix4x4.c2());
		result.m22(aiMatrix4x4.c3());
		result.m32(aiMatrix4x4.c4());
		result.m03(aiMatrix4x4.d1());
		result.m13(aiMatrix4x4.d2());
		result.m23(aiMatrix4x4.d3());
		result.m33(aiMatrix4x4.d4());

		return result;
	}
}
