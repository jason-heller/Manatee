package manatee.client.gl.camera;

import static manatee.maths.Maths.HALFPI;
import static manatee.maths.Maths.TWOPI;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import lwjgui.LWJGUI;
import lwjgui.scene.Window;
import manatee.client.Time;
import manatee.maths.MatrixMath;
import manatee.maths.SmoothFloat;

public class StaticCamera implements ICamera
{
	protected Matrix4f viewMatrix = new Matrix4f();
	protected Matrix4f projMatrix = new Matrix4f();
	protected Matrix4f projViewMatrix = new Matrix4f();

	protected Vector3f position = new Vector3f();
	protected float yaw, pitch, roll;
	
	protected SmoothFloat smoothYaw = new SmoothFloat(0, 10);
	protected SmoothFloat smoothPitch = new SmoothFloat(-HALFPI / 1.5f, 10);

	protected Vector3f lookAt = new Vector3f();
	private float[] viewport = new float[4];

	public StaticCamera() {
		Window window = LWJGUI.getThreadWindow();
		float aspectRatio = window.getAspectRatio();
		
		MatrixMath.setProjectionMatrix(projMatrix, CameraUtil.fov, aspectRatio, CameraUtil.near, CameraUtil.far);
		updateMatrices();
	}

	@Override
	public void update()
	{
		updateMatrices();
	}

	protected void updateYawPitch()
	{
		smoothYaw.update(Time.deltaTime);
		smoothPitch.update(Time.deltaTime);

		this.yaw = TWOPI + smoothYaw.get();
		yaw %= TWOPI;
		pitch = smoothPitch.get();
	}

	protected void updateMatrices()
	{
		MatrixMath.setViewMatrix(viewMatrix, position, yaw, pitch, roll);
		MatrixMath.setLookVector(lookAt, viewMatrix);

		projViewMatrix.set(projMatrix).mul(viewMatrix);
	}

	@Override
	public Matrix4f getProjectionMatrix()
	{
		return projMatrix;
	}

	@Override
	public Matrix4f getViewMatrix()
	{
		return viewMatrix;
	}

	@Override
	public Matrix4f getProjectionViewMatrix()
	{
		return projViewMatrix;
	}

	@Override
	public Vector3f getLookVector()
	{
		return lookAt;
	}

	@Override
	public Vector3f getPosition()
	{
		return position;
	}

	@Override
	public float getYaw()
	{
		return yaw;
	}

	@Override
	public float getPitch()
	{
		return pitch;
	}

	@Override
	public void setPosition(float x, float y, float z)
	{
		position.set(x, y, z);
	}

	@Override
	public void setPosition(Vector3f position)
	{
		this.position.set(position);
	}

	@Override
	public float getX()
	{
		return position.x;
	}

	@Override
	public float getY()
	{
		return position.y;
	}

	@Override
	public float getZ()
	{
		return position.z;
	}

	@Override
	public void setYaw(float yaw)
	{
		this.yaw = yaw;
		this.smoothYaw.setValue(yaw);
	}

	@Override
	public void setPitch(float pitch)
	{
		this.pitch = pitch;
		this.smoothPitch.setValue(pitch);
	}

	public float getRoll()
	{
		return roll;
	}

	public void setRoll(float roll)
	{
		this.roll = roll;
	}

	@Override
	public float[] getViewport()
	{
		return viewport;
	}

	@Override
	public void setViewport(double x, double y, double width, double height)
	{
		viewport[0] = (float)x;
		viewport[1] = (float)y;
		viewport[2] = (float)width;
		viewport[3] = (float)height;
	}
}
