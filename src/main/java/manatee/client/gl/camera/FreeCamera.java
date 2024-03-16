package manatee.client.gl.camera;

import org.joml.Vector3f;

import manatee.client.Time;
import manatee.client.input.Input;
import manatee.client.input.Keybinds;
import manatee.maths.Maths;

public class FreeCamera extends ControllableCamera
{

	public FreeCamera()
	{
		super();
	}

	@Override
	public void update()
	{
		super.update();
		
		if (smoothPitch.get() < -Maths.PI+.01f)
			smoothPitch.setValue(-Maths.PI+.01f);
		
		if (smoothPitch.get() > -.01f)
			smoothPitch.setValue(-.01f);
		
		updateYawPitch();
		
		updateMatrices();
	}

	@Override
	protected void controlCamera()
	{
		Vector3f forward = new Vector3f(lookAt);
		Vector3f strafe = new Vector3f(lookAt.x, lookAt.y, 0f).normalize().cross(0, 0, 1);

		forward.mul(CameraUtil.cameraSpeed * Time.deltaTime);
		strafe.mul(CameraUtil.cameraSpeed * Time.deltaTime);
		
		if (Input.isHeld(Keybinds.PAN_LEFT))
			position.sub(strafe);

		if (Input.isHeld(Keybinds.PAN_RIGHT))
			position.add(strafe);
		
		if (Input.isHeld(Keybinds.PAN_UP))
			position.add(forward);

		if (Input.isHeld(Keybinds.PAN_DOWN))
			position.sub(forward);
	}
}
