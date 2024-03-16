package manatee.client.gl.camera;

import static manatee.maths.Maths.HALFPI;

import org.joml.Vector3f;

import manatee.client.Time;
import manatee.client.dev.Dev;
import manatee.client.input.Input;
import manatee.client.input.Keybinds;
import manatee.maths.SmoothFloat;

public class TrackingCamera extends ControllableCamera
{
	private Vector3f trackingTarget;

	protected SmoothFloat smoothZoom = new SmoothFloat(7f, 10f);
	
	private float heightFromTarget;
	private float distFromTarget;

	public TrackingCamera()
	{
		super();
	}

	@Override
	public void update()
	{
		if (trackingTarget == null)
			return;

		super.update();
		
		position.set(trackingTarget);
		
		smoothZoom.update(Time.deltaTime);
		
		updateYawPitch();
		
		if (smoothPitch.get() < -HALFPI)
			smoothPitch.setValue(-HALFPI);
		
		if (smoothPitch.get() > -.01f)
			smoothPitch.setValue(-.01f);
		
		distFromTarget = (float) (Math.sin(pitch)) * smoothZoom.get();
		heightFromTarget = (float) (Math.cos(pitch)) * smoothZoom.get();

		position.x += (float) Math.cos(-yaw + HALFPI) * distFromTarget;
		position.y += (float) Math.sin(-yaw + HALFPI) * distFromTarget;
		position.z += heightFromTarget;
		
		updateMatrices();
	}

	@Override
	protected void controlCamera()
	{
		
		if (Input.isHeld(Keybinds.PAN_LEFT))
			smoothYaw.increaseTarget(-.025f);

		if (Input.isHeld(Keybinds.PAN_RIGHT))
			smoothYaw.increaseTarget(.025f);

		if (Input.isHeld(Keybinds.ZOOM_IN))
			smoothZoom.scaleTarget(.8f);

		if (Input.isHeld(Keybinds.ZOOM_OUT))
			smoothZoom.scaleTarget(1.2f);
	}
	
	public void setTrackingTarget(Vector3f trackingTarget)
	{
		this.trackingTarget = trackingTarget;
	}
}
