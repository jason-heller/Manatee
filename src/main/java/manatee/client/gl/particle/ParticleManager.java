package manatee.client.gl.particle;

import static manatee.client.scene.Assets.RESOURCE_PATH;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joml.Vector3f;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.AIVector3D.Buffer;
import org.lwjgl.system.MemoryUtil;

import manatee.cache.definitions.IAsset;
import manatee.cache.definitions.loader.MeshLoader;
import manatee.cache.definitions.texture.ITexture;
import manatee.cache.definitions.texture.Texture2D;
import manatee.client.gl.camera.ICamera;
import manatee.client.gl.particle.attribs.IParticleAttrib;
import manatee.client.gl.particle.mesh.MeshParticleRenderer;
import manatee.client.gl.particle.mesh.MeshParticleSystem;
import manatee.client.gl.particle.quad.QuadParticleRenderer;
import manatee.client.gl.particle.quad.QuadParticleSystem;
import manatee.client.scene.MapScene;

public class ParticleManager
{
	
	private List<QuadParticleSystem> quadSystems = new ArrayList<>();
	private List<MeshParticleSystem> meshSystems = new ArrayList<>();
	
	private Map<String, ParticleSystem> partSystemNames = new HashMap<>();
	
	List<IAsset> assets = new ArrayList<>();
	
	private QuadParticleRenderer quadRenderer;
	private MeshParticleRenderer meshRenderer;
	
	public static Map<String, String> meshes = new LinkedHashMap<>();
	public static Map<String, Integer> meshIndices = new HashMap<>();
	public static Map<String, ITexture> textures = new LinkedHashMap<>();
	public static Map<String, Integer> textureAtlasSizes = new HashMap<>();
	
	public ParticleManager()
	{
		quadRenderer = new QuadParticleRenderer();
		meshRenderer = new MeshParticleRenderer();
		
		initAssets();
	}
	
	private void initAssets()
	{
		meshes.clear();

		putMesh("cube", "mesh/particle/cube.obj");
		putMesh("smoke", "mesh/particle/sphere.obj");
		
		putTexture("flame", "texture/particle/flame.png", 2);
			
	}
	
	private void putTexture(String key, String value, int atlasWidth)
	{
		textures.put(key, Texture2D.load(value));
		textureAtlasSizes.put(key, atlasWidth);
	}
	
	private void putMesh(String key, String value)
	{
		meshIndices.put(key, meshes.size());
		meshes.put(key, value);
		addMesh(value);
	}

	public void render(MapScene scene)
	{
		ICamera camera = scene.getCamera();
		
		for(QuadParticleSystem quadSystem : quadSystems)
			quadSystem.update(camera);
		
		for(MeshParticleSystem meshSystem : meshSystems)
			meshSystem.update(camera);
		
		quadRenderer.render(camera, quadSystems);
		meshRenderer.render(scene, meshSystems);
	}
	
	public void dispose()
	{
		quadRenderer.dispose();
		meshRenderer.dispose();
		
		for(IAsset asset : assets)
			asset.dispose();
	}
	
	public ParticleSystem addSystem(String name, String texturePath, int widthInFrames, IParticleAttrib[] attribs)
	{
		return addSystem(name, texturePath, widthInFrames, true, false, attribs);
	}
	
	public ParticleSystem addSystem(String name, String texturePath, int widthInFrames, boolean additive, boolean lit, IParticleAttrib[] attribs)
	{
		ITexture texture = Texture2D.load(texturePath);
		assets.add(texture);
		
		return addSystem(name, texture, widthInFrames, additive, lit, attribs);
	}
	
	public ParticleSystem addSystem(String name, ITexture texture, int widthInFrames, boolean additive, boolean lit, IParticleAttrib[] attribs)
	{
		ParticleAtlas atlas = new ParticleAtlas(texture, widthInFrames, additive, lit);
		QuadParticleSystem prtSystem = new QuadParticleSystem(attribs, atlas);
		quadSystems.add(prtSystem);
		partSystemNames.put(name, prtSystem);
		
		return prtSystem;
	}
	
	public ParticleSystem addSystem(String name, String meshPath, IParticleAttrib[] attribs)
	{
		int partMeshIndex = addMesh(meshPath);
		return addSystem(name, partMeshIndex, attribs);
	}
	
	public int addMesh(String meshPath)
	{
		// Yes, you can only use the first mesh. Does not support models. Too bad!
		AIMesh mesh = MeshLoader.getAssimpMeshes(new File(RESOURCE_PATH + meshPath)).getMesh(0);

		FloatBuffer vertices = MemoryUtil.memAllocFloat(mesh.mNumVertices() * 6);
		IntBuffer indices = MemoryUtil.memAllocInt(mesh.mNumFaces() * mesh.mFaces().get(0).mNumIndices());

		final Buffer positionBuffer = mesh.mVertices();
		final Buffer normalBuffer = mesh.mNormals();

		for (int v = 0; v < mesh.mNumVertices(); v++)
		{
			AIVector3D position = positionBuffer.get(v);
			AIVector3D normal = normalBuffer.get(v);

			vertices.put(position.x());
			vertices.put(position.y());
			vertices.put(position.z());

			vertices.put(normal.x());
			vertices.put(normal.y());
			vertices.put(normal.z());
		}

		for (int f = 0; f < mesh.mNumFaces(); f++)
		{
			AIFace face = mesh.mFaces().get(f);

			for (int ind = 0; ind < face.mNumIndices(); ind++)
			{
				indices.put(face.mIndices().get(ind));
			}
		}

		vertices.flip();
		indices.flip();

		int partMeshIndex = meshRenderer.addMesh(vertices, indices);

		MemoryUtil.memFree(vertices);
		MemoryUtil.memFree(indices);

		return partMeshIndex;
	}

	public ParticleSystem addSystem(String name, int partMeshIndex, IParticleAttrib[] attribs)
	{
		MeshParticleSystem prtSystem = new MeshParticleSystem(attribs, partMeshIndex);
		meshSystems.add(prtSystem);
		partSystemNames.put(name, prtSystem);
		
		return prtSystem;
	}

	public void addEmitter(String name, Vector3f position, boolean enabled)
	{
		ParticleSystem ps = partSystemNames.get(name);
		
		if (ps == null)
			return;
		
		ps.addEmitter(position, enabled);
	}
	
	public void addEmitter(String name, ParticleEmitter emitter)
	{
		ParticleSystem ps = partSystemNames.get(name);
		
		if (ps == null)
			return;
		
		ps.addEmitter(emitter);
	}

	public Set<String> getParticleSystemNames()
	{
		return partSystemNames.keySet();
	}
	
	public ParticleSystem getParticleSystem(String name)
	{
		return partSystemNames.get(name);
	}
	
	public void clear()
	{
		for(ParticleSystem ps : quadSystems)
		{
			ps.clearEmitters();
			ps.clearParticles();
		}
		
		for(ParticleSystem ps : meshSystems)
		{
			ps.clearEmitters();
			ps.clearParticles();
		}
	}

	public void removeSystem(String name)
	{
		ParticleSystem ps = partSystemNames.remove(name);
		if (ps != null)
		{
			quadSystems.remove(ps);
			meshSystems.remove(ps);
		}
	}
}
