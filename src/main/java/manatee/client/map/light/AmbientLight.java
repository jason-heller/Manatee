package manatee.client.map.light;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import manatee.maths.MCache;

public class AmbientLight implements ILight
{
	private Vector3f origin;
	private Vector4f color;
	private Quaternionf direction;
	
	public AmbientLight(Vector3f origin, Quaternionf direction, Vector4f color)
	{
		this.origin = origin;
		this.direction = direction;
		this.color = color;
	}

	@Override
	public Vector4f getColor()
	{
		return color;
	}

	@Override
	public Vector3f getOrigin()
	{
		return origin;
	}

	@Override
	public float getFalloff()
	{
		return 0f;
	}

	@Override
	public Quaternionf getDirection()
	{
		return direction;
	}

	@Override
	public void setFalloff(float falloff)
	{
	}
}
