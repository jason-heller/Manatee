package manatee.client.gl.camera;

import manatee.client.dev.Command;
import manatee.client.scene.IScene;

public class CameraUtil
{
	public static float cameraSpeed = 20f;

	public static float sensitivity = .0075f;

	public static float fov = 85f;
	public static float near = 0.1f;
	public static float far = 1000f;
	
	public static void initCommands(IScene scene)
	{
		Command.add("camera_speed", Command.FLOAT_SYNTAX, CameraUtil.class, "setCameraSpeed", false);
		Command.add("camera_mode", "static/tracking/floating/free", scene, "setCameraByName", true);
		
		Command.add("noclip", "", scene, "toggleNoclip", true);
		
		
		//Command.add("noclip", Command.FLOAT_SYNTAX, CameraUtil.class, "noclip", false);
	}
	
	public static void setCameraSpeed(float speed)
	{
		cameraSpeed = speed;
	}

	public static ICamera getCameraByName(String type)
	{
		switch(type)
		{
		case "static":
			return new StaticCamera();
		case "tracking":
			return new TrackingCamera();
		case "floating":
			return new FloatingCamera();
		case "free":
			return new FreeCamera();
		default:
			System.out.println("Unknown camera type " + type);
		}
		
		return new StaticCamera();
	}
}
