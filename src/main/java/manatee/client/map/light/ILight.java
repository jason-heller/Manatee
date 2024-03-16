package manatee.client.map.light;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public interface ILight
{
	public Vector3f getOrigin();
	
	public Vector4f getColor();
	
	public Quaternionf getDirection();
	
	public float getFalloff();

	public void setFalloff(float falloff);
}
