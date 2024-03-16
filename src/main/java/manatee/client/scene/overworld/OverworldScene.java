package manatee.client.scene.overworld;

import org.joml.Vector2f;
import org.joml.Vector3f;

import manatee.client.gl.camera.ICamera;
import manatee.client.gl.camera.TrackingCamera;
import manatee.client.map.tile.Tile;
import manatee.client.scene.Assets;
import manatee.client.scene.PlayableScene;
import manatee.client.ui.ClientUI;
import manatee.client.ui.UIBuilder;

public class OverworldScene extends PlayableScene
{
	protected String mapName = "src/main/resources/maps/level.map";

	@Override
	public void init(ClientUI ui)
	{
		super.init(ui);
		
		this.importMap(mapName);
		
		setCamera(new TrackingCamera());
	}
	
	@Override
	public void setCamera(ICamera camera)
	{
		super.setCamera(camera);
		
		if (camera instanceof TrackingCamera)
		{
			if (player != null)
			{
				((TrackingCamera) camera).setTrackingTarget(player.getPosition());
			}
		}
	}
	
	@Override
	protected void onTerrainPicked(Tile tileAt, float heightAt)
	{
		Vector3f position = player.getPosition();
		float dist = mouseWorldPos.distanceSquared(position);
		
		if (dist >= 1600)
			return;
		
		Vector2f[] path = map.getGeometry().createPath(
				position.x, position.y,
				mouseWorldPos.x, mouseWorldPos.y
		);
		
		player.setPath(path);
	}

	@Override
	protected void onMouseHoverChange()
	{
		// Hm
	}
	
	@Override
	public UIBuilder createUIBuilder()
	{
		return new OverworldUIBuilder();
	}

	@Override
	protected Assets createAssets()
	{
		return new OverworldAssets();
	}

	
}
