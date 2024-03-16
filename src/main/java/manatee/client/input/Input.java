package manatee.client.input;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import lwjgui.glfw.input.KeyboardHandler;
import lwjgui.glfw.input.MouseHandler;
import lwjgui.scene.Window;

public class Input
{

	public static final int NO_KEY = 0;
	public static final int SCROLL_UP = GLFW.GLFW_KEY_LAST + GLFW.GLFW_MOUSE_BUTTON_LAST + 1;
	public static final int SCROLL_DOWN = SCROLL_UP + 1;
	public static final int LEFT_CLICK = GLFW.GLFW_KEY_LAST + GLFW.GLFW_MOUSE_BUTTON_LEFT;
	public static final int RIGHT_CLICK = GLFW.GLFW_KEY_LAST + GLFW.GLFW_MOUSE_BUTTON_RIGHT;
	public static final int MIDDLE_CLICK = GLFW.GLFW_KEY_LAST + GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

	private static Window currentWindow;

	private static Map<String, int[]> keybinds = new HashMap<>();
	private static Map<String, Integer> keystates = new HashMap<>();
	
	private static float mouseX;
	private static float mouseY;
	
	private static float mouseDX;
	private static float mouseDY;

	public static void setWindow(Window window)
	{
		currentWindow = window;
	}

	public static void setBind(Keybinds bind, int key)
	{
		setBind(bind.name(), key);
	}

	public static void setBind(String bind, int key)
	{
		setBind(bind, key, NO_KEY);
	}

	/*
	 * public static void setCommandBind(String command, int key) {
	 * commandBinds.put(key, command); }
	 */

	/**
	 * Sets the main key for the bind.
	 *
	 * @param bind The keybind to assign this key to
	 * @param key  the main key to be assigned as the bind
	 * @param alt  the alternative key to be assigned as the bind
	 */
	public static void setBind(Keybinds bind, int key, int alt)
	{
		setBind(bind.name(), key, alt);
	}

	public static void setBind(String bind, int key, int alt)
	{
		keybinds.put(bind, new int[]
		{ key, alt });
		keystates.put(bind, 0);
	}

	public static void resetBinds()
	{
		keybinds.clear();
		keystates.clear();
		// commandBinds.clear();

		for (Keybinds bind : Keybinds.values())
		{
			keybinds.put(bind.name(), new int[]
			{
					bind.getDefaultBind(), bind.getDefaultAltBind()
			});
			
			keystates.put(bind.name(), 0);
		}
		
		for (KeybindsInternal bind : KeybindsInternal.values())
		{
			if (keybinds.get(bind.name()) == null)
			{
				keybinds.put(bind.name(), new int[] { bind.getDefaultBind() });
				keystates.put(bind.name(), 0);
			}
		}
	}

	public static boolean isHeld(IKeybind bind)
	{
		return isHeld(bind.name());
	}

	private static boolean isHeld(String name)
	{
		return keystates.get(name) != 0;
	}
	
	public static boolean isPressed(IKeybind bind)
	{
		return isPressed(bind.name());
	}

	private static boolean isPressed(String name)
	{
		return keystates.get(name) == 1;
	}

	public static float getMouseX()
	{
		return mouseX;
	}

	public static float getMouseY()
	{
		return mouseY;
	}
	
	public static float getMouseDX()
	{
		return mouseDX;
	}

	public static float getMouseDY()
	{
		return mouseDY;
	}

	public static void poll()
	{
		float mouseWheelY = currentWindow.getMouseHandler().getYWheel();
		MouseHandler mouseHandler = currentWindow.getMouseHandler();
		KeyboardHandler keyboardHandler = currentWindow.getKeyboardHandler();
		
		mouseDX = mouseHandler.getDX();
		mouseDY = mouseHandler.getDY();
		
		mouseX = mouseHandler.getX();
		mouseY = mouseHandler.getY();
		
		mouseHandler.update();
		
		for(String bind : keybinds.keySet()) 
		{
			int state = keystates.get(bind);
			boolean pressed = false;
			
			int[] keys = keybinds.get(bind);
			for (int key : keys)
			{
				if ((key == SCROLL_UP && mouseWheelY > 0) || (key == SCROLL_DOWN && mouseWheelY < 0)
					|| (key >= LEFT_CLICK && mouseHandler.isButtonPressed(key - LEFT_CLICK)))
				{
					state++;
					pressed = true;
					break;
				}
				else if (keyboardHandler.isKeyPressed(key))
				{
					state++;
					pressed = true;
					break;
				}

			}
			
			state = pressed ? state : 0;
			
			keystates.put(bind, state);
		}
	}

	public static void init()
	{
		resetBinds();
	}
}
