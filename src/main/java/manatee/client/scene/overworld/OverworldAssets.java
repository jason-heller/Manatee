package manatee.client.scene.overworld;

import manatee.client.scene.Assets;

public class OverworldAssets extends Assets
{
	@Override
	public void loadAssets()
	{
		String[] plTextures = {
				"texture/player/feet.png",
				NULL,						// Hands
				"texture/player/head.png",
				"texture/player/legs.png",
				"texture/player/torso.png"
		};

		loadModel("player", "mesh/entity/player.fbx", plTextures);
		loadModel("hat", "mesh/entity/helmet.fbx", NULL);
	}
}
