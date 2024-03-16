package manatee.client.entity;

import manatee.client.scene.MapScene;

public interface Entity
{
	public void update(MapScene scene);

	public void updateInternal(MapScene scene);
	
	public void onSpawn();
	
	public void onDespawn();
}
