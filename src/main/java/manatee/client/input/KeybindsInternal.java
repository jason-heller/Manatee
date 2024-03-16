package manatee.client.input;

import static manatee.client.input.Input.NO_KEY;

import org.lwjgl.glfw.GLFW;

public enum KeybindsInternal implements IKeybind {
	CTRL(GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL),
	SHIFT(GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT),
	ALT(GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT),
	
	LMB(GLFW.GLFW_KEY_LAST + GLFW.GLFW_MOUSE_BUTTON_LEFT),
	RMB(GLFW.GLFW_KEY_LAST + GLFW.GLFW_MOUSE_BUTTON_RIGHT),
	DELETE(GLFW.GLFW_KEY_DELETE),
	
	T(GLFW.GLFW_KEY_T),
	R(GLFW.GLFW_KEY_R),
	E(GLFW.GLFW_KEY_E),
	
	N(GLFW.GLFW_KEY_N),
	O(GLFW.GLFW_KEY_O),
	S(GLFW.GLFW_KEY_S),
	
	Z(GLFW.GLFW_KEY_Z),
	Y(GLFW.GLFW_KEY_Y),
	X(GLFW.GLFW_KEY_X),
	
	ESCAPE(GLFW.GLFW_KEY_ESCAPE),
	
	// Developer binds
	DEVELOPER_CONSOLE(GLFW.GLFW_KEY_GRAVE_ACCENT);

	private int defaultBind, defaultAltBind;

	private KeybindsInternal(int defaultBind) {
		this(defaultBind, NO_KEY);
	}

	private KeybindsInternal(int defaultBind, int defaultAltBind) {
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
