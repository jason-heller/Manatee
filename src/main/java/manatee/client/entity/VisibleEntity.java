package manatee.client.entity;

import manatee.cache.definitions.mesh.IMesh;
import manatee.cache.definitions.texture.ITexture;

public interface VisibleEntity
{
	public IMesh getMesh();
	
	public ITexture getTexture();
	
	public boolean isVisible();
}
