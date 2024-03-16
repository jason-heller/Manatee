package manatee.primitives;

import org.joml.Matrix3f;
import org.joml.Vector3f;

public class Primitive
{

	private final int id;
	private Vector3f start;
	private Vector3f end;
	private Vector3f color;

	private Matrix3f rotation;
	
	private float scale = 0;

	public Primitive(int id, Vector3f start, Vector3f end, Vector3f color, Matrix3f rotation)
	{
		this.id = id;
		this.start = start;
		this.end = end;
		this.color = color;
		this.rotation = rotation;
	}

	public Primitive(int id, Vector3f origin, Vector3f color, Matrix3f rotation)
	{
		this.id = id;
		this.start = origin;
		this.end = origin;
		this.color = color;
		this.rotation = rotation;
	}

	public int getId()
	{
		return id;
	}

	public Vector3f getStart()
	{
		return start;
	}

	public Vector3f getEnd()
	{
		return end;
	}

	public Vector3f getColor()
	{
		return color;
	}

	public Matrix3f getRotation()
	{
		return rotation;
	}

	public void setStart(Vector3f start)
	{
		this.start = start;
	}

	public void setEnd(Vector3f end)
	{
		this.end = end;
	}

	public void setColor(Vector3f color)
	{
		this.color = color;
	}

	public void setRotation(Matrix3f rotation)
	{
		this.rotation = rotation;
	}

	public float getScale()
	{
		return scale;
	}

	public void setScale(float scale)
	{
		this.scale = scale;
	}
}
