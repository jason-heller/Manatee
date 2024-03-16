package manatee.client.input;

import static manatee.client.input.Input.NO_KEY;
import static manatee.client.input.Input.SCROLL_DOWN;
import static manatee.client.input.Input.SCROLL_UP;

import org.lwjgl.glfw.GLFW;

public enum Keybinds implements IKeybind {
	PAN_LEFT(GLFW.GLFW_KEY_A),
	PAN_RIGHT(GLFW.GLFW_KEY_D),
	PAN_UP(GLFW.GLFW_KEY_W),
	PAN_DOWN(GLFW.GLFW_KEY_S),
	ZOOM_OUT(SCROLL_DOWN),
	ZOOM_IN(SCROLL_UP),
	SELECT(GLFW.GLFW_KEY_LAST + GLFW.GLFW_MOUSE_BUTTON_LEFT),
	ALT_SELECT(GLFW.GLFW_KEY_LAST + GLFW.GLFW_MOUSE_BUTTON_RIGHT),
	ESCAPE(GLFW.GLFW_KEY_ESCAPE);
	
	

	private int defaultBind, defaultAltBind;

	private Keybinds(int defaultBind) {
		this(defaultBind, NO_KEY);
	}

	private Keybinds(int defaultBind, int defaultAltBind) {
		this.defaultBind = defaultBind;
		this.defaultAltBind = defaultAltBind;
	}

	public int getDefaultBind() {
		return defaultBind;
	}

	public int getDefaultAltBind() {
		return defaultAltBind;
	}
}
