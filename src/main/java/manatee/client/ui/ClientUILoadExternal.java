package manatee.client.ui;

import org.lwjgl.glfw.GLFW;

import lwjgui.loader.UILoader;
import lwjgui.scene.Scene;
import lwjgui.scene.Window;
import lwjgui.scene.control.TextArea;
import lwjgui.scene.control.TextInputControl;
import lwjgui.scene.layout.OpenGLPane;
import manatee.client.dev.Command;
import manatee.client.dev.DeveloperConsole;
import manatee.client.gl.camera.ControllableCamera;
import manatee.client.gl.camera.ICamera;
import manatee.client.input.Input;
import manatee.client.input.KeybindsInternal;
import manatee.client.scene.IScene;

public class ClientUILoadExternal extends ClientUI
{
	private TextInputControl chatInput;
	private TextInputControl chatDisplay;
	
	private String html, css;
	
	public ClientUILoadExternal(IScene gameScene)
	{
		super(gameScene);
		
		html = "ui/hud.html";
		css = "ui/hud.css";
		
		// Broken (possibly b/c Window is visible?)
		//Command.add("loadUI", "htmlfile", this, false);
	}

	public void loadUI()
	{
		UILoader uiLoader = new UILoader();
		
		uiLoader.addRenderer(UILoader.WORLDSPACE_RENDERER_NAME, clientRenderer);
		
		Scene scene = window.getScene();// LWJGUI.getThreadWindow().getScene();
		
		uiLoader.loadUI(scene, html, css);
		
		linkControls(uiLoader);
		
		chatInput.setOnKeyPressed((keyEvent) -> {
			if (keyEvent.getKey() == GLFW.GLFW_KEY_ENTER)
			{
				String input = chatInput.getText();
				
				if (input.indexOf('/') == 0)
				{
					DeveloperConsole.println(input);
					Command.processCommand(input.substring(1));
				}
				else 
				{
					chatDisplay.appendText(input + "\n");
				}
				
				chatInput.clear();
			}
		});
		
		chatDisplay.setDisabled(true);
		
		if (devConsole != null)
			devConsole.attachToUI(scene);
		
		devConsole = new DeveloperConsole(window.getScene());
		devConsole.attachToUI(window.getScene());
	}

	private void linkControls(UILoader uiLoader)
	{
		//final Node noPane = new FloatingPane();
		final TextArea noText = new TextArea();
		
		chatInput = (TextInputControl) uiLoader.getFromID("chat-input");
		chatDisplay = (TextInputControl) uiLoader.getFromID("chat-display");
		worldRenderPane = (OpenGLPane) uiLoader.getFromID(UILoader.WORLDSPACE_RENDERER_NAME);
		
		if (chatInput == null)
			chatInput = noText;
		
		if (chatDisplay == null)
			chatDisplay = noText;
		
		// Force selectable
		if (worldRenderPane != null)
		{
			worldRenderPane.setMouseTransparent(false);
			
			worldRenderPane.setOnMouseEntered(e -> {
				ICamera camera = gameScene.getCamera();
				
				if (camera instanceof ControllableCamera)
					((ControllableCamera)camera).setDraggable(true);
			});
			
			worldRenderPane.setOnMouseExited(e -> {;
				ICamera camera = gameScene.getCamera();
				
				if (camera instanceof ControllableCamera)
					((ControllableCamera)camera).setDraggable(false);
			});
			
			/*window.addEventListener(new WindowSizeListener() {

				@Override
				public void invoke(long window, int newWidth, int newHeight)
				{
					
					
				}
				
			});*/
		}
	}

	public void update()
	{
		if (Input.isPressed(KeybindsInternal.DEVELOPER_CONSOLE))
			devConsole.toggle();
		
		// TODO: This is dumb, fix later
		gameScene.getCamera().setViewport(worldRenderPane.getX(), worldRenderPane.getY(), worldRenderPane.getWidth(), worldRenderPane.getHeight());
	}

	public ClientRenderer getRenderer()
	{
		return clientRenderer;
	}
}
