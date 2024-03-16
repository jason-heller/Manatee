package manatee.client.scene;

import java.io.File;

import org.slf4j.LoggerFactory;

import manatee.cache.definitions.IAsset;
import manatee.cache.definitions.Model;
import manatee.cache.definitions.loader.MeshLoader;
import manatee.cache.definitions.mesh.IMesh;
import manatee.cache.definitions.sound.Sound;
import manatee.cache.definitions.texture.ITexture;
import manatee.cache.definitions.texture.Texture2D;
import manatee.util.HashCollection;

public abstract class Assets
{
	protected HashCollection<String, IMesh> meshes = new HashCollection<>();
	protected HashCollection<String, ITexture> textures = new HashCollection<>();
	protected HashCollection<String, Sound> sounds = new HashCollection<>();
	
	protected HashCollection<String, Model> models = new HashCollection<>();
	
	public static final String RESOURCE_PATH = "src/main/resources/";

	public abstract void loadAssets();
	
	public void dispose()
	{
		for(IAsset asset : meshes.values())
			if (asset != null)
				asset.dispose();
		
		for(IAsset asset : textures.values())
			if (asset != null)
				asset.dispose();
		
		for(IAsset asset : sounds.values())
			if (asset != null)
				asset.dispose();
	}
	
	protected void addMesh(IMesh mesh)
	{
		meshes.add(mesh);
	}
	
	protected void addTexture(ITexture texture)
	{
		textures.add(texture);
	}
	
	protected void addSound(Sound sound)
	{
		sounds.add(sound);
	}
	
	protected IMesh loadMesh(String path)
	{
		StringBuilder str = new StringBuilder()
				.append(RESOURCE_PATH)
				.append(path);
		
		IMesh mesh = MeshLoader.loadMesh(new File(str.toString()));
		meshes.add(mesh);
		
		return mesh;
	}
	
	protected IMesh loadMesh(String key, String path)
	{
		StringBuilder str = new StringBuilder()
				.append(RESOURCE_PATH)
				.append(path);
		
		IMesh mesh = MeshLoader.loadMesh(new File(str.toString()));
		meshes.put(key, mesh);
		
		return mesh;
	}
	
	public Model loadModel(String key, String modelPath, String texturePath)
	{
		Model model = loadModel(modelPath, texturePath);
		
		models.put(key, model);
		
		return model;
	}
	
	public Model loadModel(String key, String[] modelPath, String texturePath)
	{
		IMesh[] meshGroup = new IMesh[modelPath.length];

		for(int i = 0; i < meshGroup.length; i++)
		{
			meshGroup[i] = MeshLoader.loadMesh(new File(RESOURCE_PATH + modelPath[i]));
			
			if (meshGroup[i] == null)
			{
				LoggerFactory.getLogger("IO").error("Could not find: " + modelPath[i]);
				return null;
			}
		}
		
		for(IMesh mesh : meshGroup)
			meshes.add(mesh);
		
		ITexture texture = null;
		
		texture = (texturePath != null) ? loadTexture2D(texturePath) : GlobalAssets.NO_TEX;

		Model model = new Model(meshGroup, texture);
		
		models.put(key, model);
		
		return model;
	}
	
	public Model loadModel(String modelPath, String texturePath)
	{
		StringBuilder str = new StringBuilder()
				.append(RESOURCE_PATH)
				.append(modelPath);
		
		IMesh[] meshGroup = MeshLoader.loadModel(new File(str.toString()));
		
		if (meshGroup == null)
			return null;
		
		for(IMesh mesh : meshGroup)
			meshes.add(mesh);
		
		ITexture texture = null;
		
		texture = (texturePath != null) ? loadTexture2D(texturePath) : GlobalAssets.NO_TEX;

		Model model = new Model(meshGroup, texture);
		
		return model;
	}
	
	protected Texture2D loadTexture2D(String path)
	{
		Texture2D texture = Texture2D.load(RESOURCE_PATH + path);
		textures.add(texture);
		
		return texture;
	}
	
	protected Texture2D loadTexture2D(String key, String path)
	{
		Texture2D texture = Texture2D.load(RESOURCE_PATH + path);
		textures.put(key, texture);
		
		return texture;
	}
	
	protected Sound loadSound(String path)
	{
		StringBuilder str = new StringBuilder()
				.append(RESOURCE_PATH)
				.append(path);
		
		Sound sound = new Sound(str.toString());
		sounds.add(sound);
		
		return sound;
	}
	
	protected Sound loadSound(String key, String path)
	{
		Sound sound = new Sound(path);
		sounds.put(key, sound);
		
		return sound;
	}
	
	public IMesh getMesh(String name)
	{
		IMesh mesh = meshes.get(name);
		
		return mesh == null ? GlobalAssets.MISSING_MESH : mesh;
	}

	public ITexture getTexture(String name)
	{
		ITexture texture = textures.get(name);
		return texture == null ? GlobalAssets.MISSING_TEX : texture;
	}
	
	public Model getModel(String name)
	{
		Model model = models.get(name);
		
		return model == null ? GlobalAssets.MISSING_MODEL : model;
	}
	
	public Sound getSound(String name)
	{
		Sound sound = sounds.get(name);
		
		return sound == null ? GlobalAssets.MISSING_SOUND : sound;
	}
	
	public HashCollection<String, Model> getModels()
	{
		return models;
	}
}
