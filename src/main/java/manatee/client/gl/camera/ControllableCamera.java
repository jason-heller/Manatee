package manatee.client.gl.camera;

import lwjgui.LWJGUI;
import lwjgui.scene.Node;
import lwjgui.scene.layout.OpenGLPane;
import lwjgui.scene.layout.Pane;
import manatee.client.dev.Dev;
import manatee.client.dev.DeveloperConsole;
import manatee.client.input.Input;
import manatee.client.input.Keybinds;

public abstract class ControllableCamera extends StaticCamera
{
	private boolean draggable = true;
	private boolean controllable;
	private boolean dragging;
	
	public ControllableCamera()
	{
		super();
	}

	@Override
	public void update()
	{
		Node hovered = LWJGUI.getThreadWindow().getContext().getHovered();
		//Node selected = LWJGUI.getThreadWindow().getContext().getSelected();
		controllable = hovered instanceof OpenGLPane;
		
		if (hovered instanceof Pane && ((Pane)hovered).getChildren().size() == 0)
			controllable = true;
		
		dragging = draggable && Input.isHeld(Keybinds.SELECT);
		dragging &= !DeveloperConsole.isVisible();

		if (dragging)
			dragCamera();

		if (controllable)
			controlCamera();
	}

	protected abstract void controlCamera();

	protected void dragCamera()
	{
		float dragX = Input.getMouseDX() * CameraUtil.sensitivity;
		float dragY = Input.getMouseDY() * CameraUtil.sensitivity;
		
		smoothYaw.increaseTarget(dragX);
		smoothPitch.increaseTarget(dragY);
	}

	public boolean isDraggable()
	{
		return draggable;
	}

	public void setDraggable(boolean draggable)
	{
		this.draggable = draggable;
	}
	
	public boolean isControllable()
	{
		return controllable;
	}
	
	public boolean isDragging()
	{
		return dragging;
	}
}
