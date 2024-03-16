package manatee.maths.geom;

import org.joml.Vector3f;

import manatee.primitives.Primitives;

public class Plane
{
	private Vector3f origin;
	private Vector3f normal;
	public float dist;
	
	public Plane(Vector3f origin, Vector3f normal)
	{
		this.origin = origin;
		this.normal = normal;
		dist = (normal.x * origin.x + normal.y * origin.y + normal.z * origin.z);
	}
	
	public Plane(float x, float y, float z, float nx, float ny, float nz)
	{
		this.origin = new Vector3f(x, y, z);
		this.normal = new Vector3f(nx, ny, nz);
		dist = (normal.x * origin.x + normal.y * origin.y + normal.z * origin.z);
	}


	public Vector3f getNormal()
	{
		return normal;
	}
	
	public Vector3f getOrigin()
	{
		return origin;
	}

	public float raycast(Vector3f org, Vector3f dir)
	{
		final float dp = normal.dot(dir);
		if (Math.abs(dp) < .00001f)
			return Float.NaN;

		float t = (dist - normal.dot(org)) / dp;
		return t >= 0 ? t : Float.NaN;
	}

	public void setOrigin(Vector3f origin)
	{
		this.origin = origin;
	}
}
