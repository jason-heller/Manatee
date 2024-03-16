package manatee.client.map.light;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class PointLight implements ILight
{
	private Vector3f origin;
	private Vector4f color;
	private float falloff;

	public PointLight(Vector3f origin, Vector4f color, float falloff)
	{
		this.origin = origin;
		this.color = color;
		this.falloff = falloff;
	}

	public Quaternionf getDirection()
	{
		return null;
	}

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
		return falloff;
	}

	@Override
	public void setFalloff(float falloff)
	{
		this.falloff = falloff;
	}
}
