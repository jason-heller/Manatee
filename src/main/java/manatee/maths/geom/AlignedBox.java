package manatee.maths.geom;

import org.joml.Vector3f;

public class AlignedBox
{
	public Vector3f center, halfExtents;
	
	protected Vector3f max = new Vector3f();
	protected Vector3f min = new Vector3f();
	
	public AlignedBox(Vector3f center, Vector3f halfExtents)
	{
		this.center = center;
		this.halfExtents = halfExtents;
		
		update();
	}
	
	public void update()
	{
		max.set(center).add(halfExtents);
		min.set(center).sub(halfExtents);
	}
	
	/**
	 * Performs an intersection test with this and another axis-aligned bounding
	 * box
	 * 
	 * @param box The box to collide this against
	 * @return True if there is an intersection, false otherwise
	 */
	public boolean intersects(AlignedBox box)
	{
		if (Math.abs(center.x - box.center.x) > (halfExtents.x + box.halfExtents.x))
			return false;
		if (Math.abs(center.y - box.center.y) > (halfExtents.y + box.halfExtents.y))
			return false;
		if (Math.abs(center.z - box.center.z) > (halfExtents.z + box.halfExtents.z))
			return false;

		return true;
	}

	/**
	 * Performs a raycast with the given ray origin and direction, and returns the
	 * distance from the ray origin to the intersection.
	 * 
	 * @param rayOrigin    The origin of the ray
	 * @param rayDirection The direction of the ray, normalized.
	 * @return The intersection distance, or POSITIVE_INFINITY if no intersection
	 *         occurs
	 */
	public float raycast(Vector3f rayOrigin, Vector3f rayDirection)
	{
		Vector3f min = new Vector3f(center).sub(halfExtents);
		Vector3f max = new Vector3f(center).add(halfExtents);
		
		return AlignedBox.raycast(min, max, rayOrigin, rayDirection);
	}
	
	protected static float raycast(Vector3f min, Vector3f max, Vector3f rayOrigin, Vector3f rayDirection)
	{
		Vector3f rayDirFrac = new Vector3f();
		rayDirFrac.x = 1f / rayDirection.x;
		rayDirFrac.y = 1f / rayDirection.y;
		rayDirFrac.z = 1f / rayDirection.z;

		float t1 = (min.x - rayOrigin.x) * rayDirFrac.x;
		float t2 = (max.x - rayOrigin.x) * rayDirFrac.x;
		float t3 = (min.y - rayOrigin.y) * rayDirFrac.y;
		float t4 = (max.y - rayOrigin.y) * rayDirFrac.y;
		float t5 = (min.z - rayOrigin.z) * rayDirFrac.z;
		float t6 = (max.z - rayOrigin.z) * rayDirFrac.z;

		float tMin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
		float tMax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

		// if tMax < 0, ray (line) is intersecting AABB, but the whole AABB is behind us
		if (tMax < 0)
		{
		    // t = tMax;
		    return Float.POSITIVE_INFINITY;
		}

		// if tmin > tmax, ray doesn't intersect AABB
		if (tMin > tMax)
		{
		    // t = tMax;
		    return Float.NaN;
		}

		return tMin;
	}
	
	/**
	 * Collides this against the given ray and returns the intersection point, or
	 * null if no such intersection occurs
	 * 
	 * @param rayOrigin    The origin of the ray
	 * @param rayDirection The direction of the ray, normalized.
	 * @return The intersection point, or null if no intersection occurs
	 */
	public Vector3f collide(Vector3f rayOrigin, Vector3f rayDirection)
	{
		float t = raycast(rayOrigin, rayDirection);
		
		if (Float.isNaN(t))
			return null;
		
		return new Vector3f(rayDirection).mul(t).add(rayOrigin);
	}
	
	public Vector3f getMax()
	{
		return max;
	}
	
	public Vector3f getMin()
	{
		return min;
	}
}
