package manatee.client.scene.overworld;

import org.lwjgl.glfw.GLFW;

import lwjgui.geometry.Insets;
import lwjgui.geometry.Pos;
import lwjgui.paint.Color;
import lwjgui.scene.Node;
import lwjgui.scene.control.TextArea;
import lwjgui.scene.control.TextField;
import lwjgui.scene.layout.BorderPane;
import lwjgui.scene.layout.HBox;
import lwjgui.scene.layout.OpenGLPane;
import lwjgui.scene.layout.VBox;
import lwjgui.scene.layout.floating.FloatingPane;
import lwjgui.style.Percentage;
import manatee.client.dev.Command;
import manatee.client.dev.DeveloperConsole;
import manatee.client.gl.camera.ControllableCamera;
import manatee.client.gl.camera.ICamera;
import manatee.client.scene.IScene;
import manatee.client.ui.ClientRenderer;
import manatee.client.ui.UIBuilder;

@Deprecated
public class OverworldUIBuilder implements UIBuilder
{

	private OpenGLPane oglPane;
	
	@Override
	public Node buildUI(IScene gameScene, ClientRenderer clientRenderer)
	{
		FloatingPane root = new FloatingPane();

		BorderPane hud;
		// OpenGLPane oglPane;

		VBox chatbox;
		HBox bottomBar;
		TextArea chatDisplay;
		TextField chatInput;
		{
			hud = new BorderPane();
			hud.setBackground(null);
			{
				oglPane = new OpenGLPane();
				oglPane.setFillToParentHeight(true);
				oglPane.setFillToParentWidth(true);
				oglPane.setMouseTransparent(false);

				bottomBar = new HBox();
				bottomBar.setAlignment(Pos.TOP_LEFT);
				bottomBar.setSpacing(24);
				bottomBar.setPadding(new Insets(12));
				bottomBar.setFillToParentWidth(true);
				bottomBar.setPrefHeightRatio(Percentage.TEN);
				bottomBar.setBackgroundLegacy(new Color(89, 41, 24, 191));
				{
					chatbox = new VBox();
					chatbox.setMinWidth(600);
					chatbox.setBackgroundLegacy(new Color("#3b1b10"));
					{
						chatDisplay = new TextArea();
						chatDisplay.setFillToParentWidth(true);
						chatDisplay.setEditable(false);

						chatInput = new TextField();
						chatInput.setFillToParentWidth(true);
					}
				}

			}

			root.getChildren().add(hud);
			hud.setCenter(oglPane);
			hud.setBottom(bottomBar);
			bottomBar.getChildren().add(chatbox);
			chatbox.getChildren().add(chatDisplay);
			chatbox.getChildren().add(chatInput);

			oglPane.setOnMouseEntered(e ->
			{
				ICamera camera = gameScene.getCamera();

				if (camera instanceof ControllableCamera)
					((ControllableCamera) camera).setDraggable(true);
			});

			oglPane.setOnMouseExited(e ->
			{
				;
				ICamera camera = gameScene.getCamera();

				if (camera instanceof ControllableCamera)
					((ControllableCamera) camera).setDraggable(false);
			});

			oglPane.setRendererCallback(clientRenderer);
		}

		chatInput.setOnKeyPressed((keyEvent) ->
		{
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
		
		return root;
	}

	@Override
	public OpenGLPane getWorldRendererPane()
	{
		return oglPane;
	}

	@Override
	public void initImages()
	{
	}
}
