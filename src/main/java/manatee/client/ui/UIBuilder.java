package manatee.client.ui;

import lwjgui.scene.Node;
import lwjgui.scene.layout.OpenGLPane;
import manatee.client.scene.IScene;

public interface UIBuilder
{
	public Node buildUI(IScene gameScene, ClientRenderer clientRenderer);
	
	public OpenGLPane getWorldRendererPane();
	
	public void initImages();
}
