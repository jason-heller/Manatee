package manatee.client.gl.camera;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public interface ICamera {

	public void update();

	public Matrix4f getProjectionMatrix();
	public Matrix4f getViewMatrix();

	public Matrix4f getProjectionViewMatrix();

	public Vector3f getLookVector();
	public Vector3f getPosition();

	public float getYaw();
	public float getPitch();
	public float getRoll();

	public void setPosition(float x, float y, float z);
	public void setPosition(Vector3f position);

	public float getX();
	public float getY();
	public float getZ();

	public void setYaw(float yaw);
	public void setPitch(float pitch);
	public void setRoll(float roll);

	public float[] getViewport();

	public void setViewport(double x, double y, double width, double height);
}
