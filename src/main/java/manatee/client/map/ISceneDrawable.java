package manatee.client.map;

import manatee.client.scene.IScene;

public interface ISceneDrawable
{
	public void load();

	public void render(IScene scene);
	
	public void dispose();
}
