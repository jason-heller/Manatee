package manatee.client.scene;

import java.io.File;

import org.slf4j.LoggerFactory;

import manatee.cache.definitions.IAsset;
import manatee.cache.definitions.Model;
import manatee.cache.definitions.loader.MeshLoader;
import manatee.cache.definitions.mesh.IMesh;
import manatee.cache.definitions.mesh.MeshGroup;
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
	
	public static final String NULL = (String)null;

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
	
	public Model loadModel(String key, String modelPath, String[] texturePaths)
	{
		return loadModel(key, new String[] {modelPath}, texturePaths);
	}
	
	public Model loadModel(String key, String[] modelPath, String texturePath)
	{
		return loadModel(key, modelPath, texturePath == null ? null : new String[] {texturePath});
	}
	
	public Model loadModel(String key, String[] modelPaths, String[] texturePaths)
	{
		MeshGroup[] meshGroups = new MeshGroup[modelPaths.length];
		int nMeshes = 0;
		
		for(int i = 0; i < meshGroups.length; i++)
		{
			meshGroups[i] = MeshLoader.loadModel(new File(RESOURCE_PATH + modelPaths[i]));
			
			if (meshGroups[i] == null)
			{
				LoggerFactory.getLogger("IO").error("Could not find mesh: " + modelPaths[i]);
				return null;
			}
			
			nMeshes += meshGroups[i].getMeshes().length;
			
			for(IMesh m : meshGroups[i].getMeshes())
				meshes.add(m);
		}
		
		ITexture[] modelTextures = new ITexture[texturePaths.length];
		
		for(int i = 0; i < modelTextures.length; i++)
		{
			modelTextures[i] = (texturePaths[i] != null) ? loadTexture2D(texturePaths[i]) : GlobalAssets.NO_TEX;	
			
			if (modelTextures[i] == null)
			{
				LoggerFactory.getLogger("IO").error("Could not find texture: " + modelTextures[i]);
				return null;
			}
			
			
		}
		
		IMesh[] modelMeshes = new IMesh[nMeshes];
		int i = 0;
		for(MeshGroup meshGroup : meshGroups)
		{
			for(IMesh mesh : meshGroup.getMeshes())
			{
				modelMeshes[i] = mesh;
				i++;
			}
		}
	
		Model model = new Model(modelMeshes, modelTextures);
				
		if (!meshGroups[0].isStatic())
		{
			model.setAnimations(meshGroups[0].getAnimations());
			model.setAnimRootNode(meshGroups[0].getAnimRootNode());
		}
		
		models.put(key, model);
		
		return model;
	}
	
	public Model loadModel(String modelPath, String texturePath)
	{
		StringBuilder str = new StringBuilder()
				.append(RESOURCE_PATH)
				.append(modelPath);
		
		MeshGroup meshGroup = MeshLoader.loadModel(new File(str.toString()));
		
		if (meshGroup == null)
			return null;
		
		for(IMesh mesh : meshGroup.getMeshes())
			meshes.add(mesh);
		
		ITexture texture = null;
		
		texture = (texturePath != null) ? loadTexture2D(texturePath) : GlobalAssets.NO_TEX;
		
		Model model = new Model(meshGroup.getMeshes(), texture);
		
		if (!meshGroup.isStatic())
		{
			model.setAnimations(meshGroup.getAnimations());
			model.setAnimRootNode(meshGroup.getAnimRootNode());
		}
		
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
