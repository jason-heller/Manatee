package manatee.cache.definitions.mesh;

import org.joml.Vector3f;

import manatee.cache.definitions.IBindableAsset;

public interface IMesh extends IBindableAsset
{
	public int getVertexCount();
	
	public Vector3f getMax();
	
	public Vector3f getMin();
	
	public Vector3f getColor();
}
