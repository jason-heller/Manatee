package manatee.client.scene.editor;

import manatee.client.entity.EntityAssets;

public class EditorAssets extends EntityAssets
{
	
	@Override
	public void loadAssets()
	{
		super.loadAssets();
		
		loadModel("player_start", "scene/editor/player_start.fbx", null);
		loadModel("spotlight", "scene/editor/spotlight.fbx", null);
		loadModel("camera", "scene/editor/camera.fbx", null);
		loadModel("gizmo", "scene/editor/gizmo.fbx", null);
		
		loadTexture2D("sun", "scene/editor/light_environment.png");
		loadTexture2D("water", "scene/editor/water.png");
		loadTexture2D("wind", "scene/editor/wind.png");
		loadTexture2D("light", "scene/editor/light.png");
		loadTexture2D("soundscape", "scene/editor/soundscape.png");
		loadTexture2D("particle", "scene/editor/particle.png");
	}

}
