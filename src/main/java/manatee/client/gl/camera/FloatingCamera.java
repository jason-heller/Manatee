package manatee.client.gl.camera;

import org.joml.Vector3f;

import manatee.client.Time;
import manatee.client.input.Input;
import manatee.client.input.Keybinds;
import manatee.maths.Maths;
import manatee.maths.SmoothFloat;

public class FloatingCamera extends ControllableCamera
{
	protected SmoothFloat smoothZoom = new SmoothFloat(35f, 10);

	public FloatingCamera()
	{
		super();
	}

	@Override
	public void update()
	{
		super.update();
		
		smoothZoom.update(Time.deltaTime);
		
		updateYawPitch();
		
		if (smoothPitch.get() < -Maths.PI+.01f)
			smoothPitch.setValue(-Maths.PI+.01f);
		
		if (smoothPitch.get() > -.01f)
			smoothPitch.setValue(-.01f);
		
		position.z = smoothZoom.get();
		
		updateMatrices();
	}

	@Override
	protected void controlCamera()
	{
		Vector3f dir = new Vector3f(this.lookAt);
		dir.z = 0;
		dir.normalize();
		dir.mul(CameraUtil.cameraSpeed * Time.deltaTime);

		Vector3f forward = new Vector3f(lookAt);
		Vector3f strafe = new Vector3f(lookAt.x, lookAt.y, 0f).normalize().cross(0, 0, 1);

		forward.mul(CameraUtil.cameraSpeed * Time.deltaTime);
		strafe.mul(CameraUtil.cameraSpeed * Time.deltaTime);
		
		if (Input.isHeld(Keybinds.PAN_LEFT))
			position.sub(strafe);

		if (Input.isHeld(Keybinds.PAN_RIGHT))
			position.add(strafe);
		
		/*if (Input.isHeld(Keybinds.PAN_LEFT))
			smoothYaw.increaseTarget(-.025f);

		if (Input.isHeld(Keybinds.PAN_RIGHT))
			smoothYaw.increaseTarget(.025f);*/
		
		if (Input.isHeld(Keybinds.PAN_UP))
			position.add(dir);

		if (Input.isHeld(Keybinds.PAN_DOWN))
			position.sub(dir);

		if (Input.isHeld(Keybinds.ZOOM_IN))
			smoothZoom.increaseTarget(CameraUtil.cameraSpeed / 10f);

		if (Input.isHeld(Keybinds.ZOOM_OUT))
			smoothZoom.increaseTarget(-CameraUtil.cameraSpeed / 10f);
	}
}
