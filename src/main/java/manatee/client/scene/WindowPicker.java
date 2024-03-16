package manatee.client.scene;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;

import manatee.client.gl.camera.ICamera;

public class WindowPicker
{
	public static Vector2i worldSpaceToViewportSpace(ICamera camera, Vector3f worldCoords)
	{
		Matrix4f projView = camera.getProjectionViewMatrix();

		Vector4f projectedVector = new Vector4f(worldCoords.x, worldCoords.y, worldCoords.z, 1.0f);
		projView.transform(projectedVector);
		
		Vector2f screenSpace = toScreenSpace(projectedVector);
		
		toViewportSpace(camera, screenSpace);
		
		return new Vector2i((int)screenSpace.x, (int)screenSpace.y);
	}

	private static void toViewportSpace(ICamera camera, Vector2f screenSpace)
	{
		float[] viewport = camera.getViewport();

		screenSpace.x *= viewport[2];
		screenSpace.y *= viewport[3];
		screenSpace.x -= viewport[0];
		screenSpace.y -= viewport[1];
	}

	public static Vector3f screenSpaceToWorldRay(ICamera camera, float x, float y)
	{
		Vector2f normalizedCoords = toNormalizedScreenCoordinates(camera ,x, y);
		Vector4f clipCoords = new Vector4f(normalizedCoords.x, normalizedCoords.y, -1.0f, 1.0f);
		Vector4f eyeCoords = toEyeCoords(camera.getProjectionMatrix(), clipCoords);
		Vector3f worldRay = toWorldCoords(camera.getViewMatrix(), eyeCoords);
		return worldRay;
	}

	/*
	 * 
	 * 
	 */

	private static Vector3f toWorldCoords(Matrix4f viewMatrix, Vector4f eyeCoords)
	{
		Matrix4f invertedView = new Matrix4f(viewMatrix).invert();

		Vector4f rayWorld = new Vector4f(eyeCoords);
		invertedView.transform(rayWorld);

		Vector3f mouseRay = new Vector3f(rayWorld.x, rayWorld.y, rayWorld.z);
		mouseRay.normalize();

		return mouseRay;
	}

	private static Vector4f toEyeCoords(Matrix4f projectionMatrix, Vector4f clipCoords)
	{
		Matrix4f invertedProjection = new Matrix4f(projectionMatrix).invert();

		Vector4f eyeCoords = new Vector4f(clipCoords);
		invertedProjection.transform(eyeCoords);

		return new Vector4f(eyeCoords.x, eyeCoords.y, -1f, 0f);
	}

	private static Vector2f toNormalizedScreenCoordinates(ICamera camera, float x, float y)
	{
		float[] viewport = camera.getViewport();
		
		float offsetX = 2f * (x - viewport[0]);
		float offsetY = 2f * (y - viewport[1]);
		
		float nx = offsetX / viewport[2] - 1f;
		float ny = -((offsetY / viewport[3]) - 1f);

		return new Vector2f(nx, ny);
	}

	private static Vector2f toScreenSpace(Vector4f clipCoords)
	{
		float w = clipCoords.w;
		float invW = 1.0f / w;
		float xScreen = clipCoords.x * invW;
		float yScreen = clipCoords.y * invW;
		
		xScreen = (xScreen + 1f) / 2f;
		yScreen = 1f - ((yScreen + 1f) / 2f);
		
		return new Vector2f(xScreen, yScreen);
	}
}
