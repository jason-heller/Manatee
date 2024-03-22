package manatee.client;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwTerminate;

import org.lwjgl.glfw.GLFW;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lwjgui.LWJGUIUtil;
import lwjgui.scene.Window;
import lwjgui.scene.WindowManager;
import manatee.client.audio.AudioHandler;
import manatee.client.dev.Command;
import manatee.client.dev.Dev;
import manatee.client.dev.LoggerOutputStream;
import manatee.client.input.Input;
import manatee.client.input.Keybinds;
import manatee.client.inventory.item.ItemData;
import manatee.client.scene.Assets;
import manatee.client.scene.GlobalAssets;
import manatee.client.scene.IScene;
import manatee.client.scene.editor.EditorScene;
import manatee.client.scene.editor.particle.ParticleViewScene;
import manatee.client.scene.home.HomeScene;
import manatee.client.scene.overworld.OverworldScene;
import manatee.client.ui.ClientUI;
import manatee.client.ui.ClientUILoadExternal;
import manatee.primitives.Primitives;

public class Client
{
	private static Window window;
	
	private static IScene clientScene;
	private static ClientProfile clientProfile;
	private static ClientUI clientUI;
	
	private Time time;

	//private Node hovered;
	
	private Assets globalAssets = new GlobalAssets();
	
	public static final int WIDTH   = 1280;
	public static final int HEIGHT  = 720;
	
	public Client(boolean editorMode, boolean particleView)
	{
		WindowManager.init();
		
		Command.add("quit", "", this, "closeWindow", false);

		Command.add("particle_editor", "", this, "launchParticleEditor", true);
		Command.add("level_editor", "", this, "launchLevelEditor", true);
		Command.add("exit_editor", "", this, "launchBaseGame", true);
		
		Command.add("home", "", Client.class, "homeScene", true);
		
		// LWJGUI.initNodeOnCreation = false;
		
		long handle = LWJGUIUtil.createOpenGLCoreWindow("Hello World", WIDTH, HEIGHT, true, false, false);
		
		AudioHandler.init();
		
		globalAssets.loadAssets();
		
		Primitives.init();
		
		if (editorMode)
		{
			clientScene = new EditorScene();
			clientUI = new ClientUI(clientScene);
		}
		else if (particleView)
		{
			clientScene = new ParticleViewScene();
			clientUI = new ClientUI(clientScene);
		}
		else
		{
			clientScene = new OverworldScene();
			clientUI = new ClientUILoadExternal(clientScene);
		}

		ItemData.loadItems();
		clientProfile = new ClientProfile();
		
		window = WindowManager.generateWindow(handle);

		clientScene.init(clientUI);

		clientUI.setWindow(window);
		clientUI.loadUI();
		
		Input.setWindow(window);
		
		time = new Time(100);
		
		window.maximize();
		window.setWindowAutoClear(false);
		
		// window.initNodeHierachy();
		
		window.show();
		
		loop();
		
		dispose();
		
	}
	
	public static void homeScene()
	{
		setScene(new HomeScene());
	}
	
	public static void setScene(IScene s)
	{
		clientScene.dispose();
		
		clientScene = s;
		clientUI = new ClientUILoadExternal(clientScene);

		clientScene.init(clientUI);
		clientUI.setWindow(window);
		clientUI.loadUI();
	}

	private void loop()
	{
		final long handle = window.getHandle();
		
		while (!GLFW.glfwWindowShouldClose(handle))
		{
			WindowManager.update();
			Input.poll();
			
			glfwPollEvents();

			if (Input.isPressed(Keybinds.ESCAPE))
				closeWindow();	
			
			//if (window.getContext().getHovered() != null)
			//	hovered = window.getContext().getHovered();
			
			clientUI.getRenderer().drawUIDebugInfo();
			
			GLFW.glfwSwapBuffers(handle);

			clientUI.update();
			
			clientScene.tick();

			window.render();
			
			Dev.render();

			time.update();
			
			GlobalAssets.MISSING_COLOR.set(1f,1f,1f,1f);
			GlobalAssets.MISSING_COLOR.x = 0.75f + ((float)Math.sin(System.currentTimeMillis() / 200.0) * 0.25f);
		}
	}
	
	public static void closeWindow()
	{
		window.setVisible(false);
		window.close();
	}

	public void dispose()
	{
		final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		
		// Due to multithreaded nature of LWJGL, cannot immediately dealloc
		logger.debug("disposing client");
		
		clientUI.getRenderer().dispose();
		
		globalAssets.dispose();
		
		AudioHandler.dispose();
		
		clientScene.dispose();
		
		logger.debug("terminating glfw");

		glfwTerminate();
	}
	
	public static IScene scene()
	{
		return clientScene;
	}
	
	public static ClientProfile profile()
	{
		return clientProfile;
	}
	
	public static ClientUI ui()
	{
		return clientUI;
	}
	
	public static void launchLevelEditor()
	{
		restart(true, false);
	}
	
	public static void launchParticleEditor()
	{
		restart(false, true);
	}
	
	public static void launchBaseGame()
	{
		restart(false, false);
	}
	
	public static void launchParticleView()
	{
		restart(false, true);
	}
	
	private static void restart(boolean editorMode, boolean particleView)
	{
		LoggerOutputStream.clear();
		Client.closeWindow();
		
		try
		{
			new App().start(editorMode, particleView);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
