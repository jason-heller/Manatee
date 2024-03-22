package manatee.client.scene.editor;

import manatee.client.entity.EntityAssets;

public class EditorAssets extends EntityAssets
{
	
	@Override
	public void loadAssets()
	{
		super.loadAssets();
		
		loadModel("player_start", "mesh/editor/player_start.fbx", NULL);
		loadModel("spotlight", "mesh/editor/spotlight.fbx", NULL);
		loadModel("camera", "mesh/editor/camera.fbx", NULL);
		loadModel("gizmo", "mesh/editor/gizmo.fbx", NULL);
		
		loadTexture2D("sun", "texture/editor/light_environment.png");
		loadTexture2D("water", "texture/editor/water.png");
		loadTexture2D("wind", "texture/editor/wind.png");
		loadTexture2D("light", "texture/editor/light.png");
		loadTexture2D("soundscape", "texture/editor/soundscape.png");
		loadTexture2D("particle", "texture/editor/particle.png");
	}

}
