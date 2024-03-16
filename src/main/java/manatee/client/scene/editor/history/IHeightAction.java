package manatee.client.scene.editor.history;

import java.util.List;

import org.joml.Vector2i;

public interface IHeightAction extends ReversableAction
{
	public void addChange(float x, float y, float delta);
	
	public void updateTilesets();
}
