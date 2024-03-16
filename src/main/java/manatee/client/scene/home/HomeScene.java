package manatee.client.scene.home;

import manatee.client.map.tile.Tile;
import manatee.client.scene.overworld.OverworldScene;
import manatee.client.ui.ClientUI;

public class HomeScene extends OverworldScene
{
	private boolean editing = false;

	@Override
	public void init(ClientUI ui)
	{
		mapName = "src/main/resources/maps/home_test.map";
		super.init(ui);
		
		this.getColor().set(0,0,0,1);
		map.setSkipTerrain(true);
	}
	
	@Override
	protected void onTerrainPicked(Tile tileAt, float heightAt)
	{
		if (editing)
		{
			
		}
		else
		{
			super.onTerrainPicked(tileAt, heightAt);
		}
	}
}
