package manatee.client.scene;

import java.util.List;

import org.joml.Vector4f;

import manatee.client.gl.camera.ICamera;
import manatee.client.gl.renderer.nvg.NVGObject;
import manatee.client.ui.ClientUI;
import manatee.client.ui.UIBuilder;

public interface IScene
{
	public void init(ClientUI ui);

	public void tick();

	public void dispose();

	public void render();

	public void setCamera(ICamera camera);

	public ICamera getCamera();

	public List<NVGObject> getNVGObjects();

	public UIBuilder createUIBuilder();
	
	public Vector4f getColor();

	public Assets getAssets();
}
