package manatee.cache.definitions;

import java.util.Map;

import manatee.cache.definitions.mesh.IMesh;
import manatee.cache.definitions.mesh.anim.AnimNode;
import manatee.cache.definitions.mesh.anim.MeshAnimation;
import manatee.cache.definitions.texture.ITexture;

public class Model
{

	private IMesh[] meshes;
	private ITexture[] textures;
	
	private Map<String, MeshAnimation> animations;
	private AnimNode animRootNode;
	
	public Model(IMesh[] meshes, ITexture[] textures)
	{
		this.meshes = meshes;
		this.textures = textures;
	}
	
	public Model(IMesh mesh, ITexture texture)
	{
		this.meshes = new IMesh[] {mesh};
		this.textures = new ITexture[] {texture};
	}
	
	public Model(IMesh[] meshes, ITexture texture)
	{
		this.meshes = meshes;
		this.textures = new ITexture[meshes.length];

		for(int i = 0; i < textures.length; i++)
			this.textures[i] = texture;
	}

	public IMesh[] getMeshes()
	{
		return meshes;
	}

	public void setMeshes(IMesh[] meshes)
	{
		this.meshes = meshes;
	}

	public ITexture[] getTextures()
	{
		return textures;
	}

	public void setTextures(ITexture[] textures)
	{
		this.textures = textures;
	}

	public boolean isAnimated()
	{
		return animations != null;
	}

	public int getNumMeshes()
	{
		return meshes == null ? 0 : meshes.length;
	}

	public Map<String, MeshAnimation> getAnimations()
	{
		return animations;
	}

	public void setAnimations(Map<String, MeshAnimation> animations)
	{
		this.animations = animations;
	}

	public AnimNode getAnimRootNode()
	{
		return animRootNode;
	}
	
	public void setAnimRootNode(AnimNode animRootNode)
	{
		this.animRootNode = animRootNode;
	}
}
