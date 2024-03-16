package manatee.client.ui;

import org.joml.Vector3f;

import lwjgui.scene.Node;
import lwjgui.scene.Scene;
import lwjgui.scene.Window;
import lwjgui.scene.layout.OpenGLPane;
import manatee.client.dev.DeveloperConsole;
import manatee.client.input.Input;
import manatee.client.input.KeybindsInternal;
import manatee.client.scene.IScene;

public class ClientUI
{
	protected ClientRenderer clientRenderer;
	
	protected OpenGLPane worldRenderPane;
	
	protected DeveloperConsole devConsole;
	
	protected IScene gameScene;
	
	protected Window window;
	
	public ClientUI(IScene gameScene)
	{
		clientRenderer = new ClientRenderer(gameScene);
		this.gameScene = gameScene;
		
		//Theme.setTheme(new ThemeDark());
	}
	
	public void loadUI()
	{
		UIBuilder uiBuilder = gameScene.createUIBuilder();
		Node root = uiBuilder.buildUI(gameScene, clientRenderer);
		worldRenderPane = uiBuilder.getWorldRendererPane();
		
		Scene newScene = new Scene(root, 1280, 720);
		window.setScene(newScene);
		
		devConsole = new DeveloperConsole(newScene);
		devConsole.attachToUI(newScene);
	}
	
	public void setWorldRenderPane(OpenGLPane worldRenderPane)
	{
		this.worldRenderPane = worldRenderPane;
	}

	public void update()
	{
		if (Input.isPressed(KeybindsInternal.DEVELOPER_CONSOLE))
			devConsole.toggle();
		
		// TODO: This is dumb, fix later
		gameScene.getCamera().setViewport(worldRenderPane.getX(), worldRenderPane.getY(), worldRenderPane.getWidth(), worldRenderPane.getHeight());
	}

	public Window getWindow()
	{
		return window;
	}

	public void setWindow(Window window)
	{
		this.window = window;
	}

	public ClientRenderer getRenderer()
	{
		return clientRenderer;
	}

	// Convenience function
	public Node getSelected()
	{
		return window.getContext().getSelected();
	}

	public OpenGLPane getWorldRenderPane()
	{
		return worldRenderPane;
	}
}
