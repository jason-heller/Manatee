package manatee.cache.definitions.mesh;

import java.util.Map;

import manatee.cache.definitions.mesh.anim.AnimNode;
import manatee.cache.definitions.mesh.anim.MeshAnimation;

public class MeshGroup
{
	private IMesh[] meshes;
	private Map<String, MeshAnimation> animations;
	private AnimNode animRootNode;
	
	public MeshGroup(IMesh[] meshes)
	{
		this.meshes = meshes;
	}
	
	public MeshGroup(IMesh[] meshes, Map<String, MeshAnimation> animations, AnimNode animRootNode)
	{
		this.meshes = meshes;
		this.animations = animations;
		this.animRootNode = animRootNode;
	}

	public IMesh[] getMeshes()
	{
		return meshes;
	}
	
	public Map<String, MeshAnimation> getAnimations()
	{
		return animations;
	}

	public AnimNode getAnimRootNode()
	{
		return animRootNode;
	}

	public boolean isStatic()
	{
		return animations == null;
	}
}
