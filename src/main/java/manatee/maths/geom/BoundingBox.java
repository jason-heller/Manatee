package manatee.maths.geom;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class BoundingBox
{
	public Vector3f center, halfExtents;
	
	protected Quaternionf rotation;

	protected Vector3f xAxis = new Vector3f(),
			yAxis = new Vector3f(),
			zAxis = new Vector3f();
	
	public BoundingBox(Vector3f center, Vector3f halfExtents)
	{
		this(center, halfExtents, new Quaternionf());
	}
	
	public BoundingBox(Vector3f center, Vector3f halfExtents, Quaternionf rotation)
	{
		this.center = center;
		this.halfExtents = halfExtents;
		this.rotation = rotation;
		
		update();
	}
	
	public void update()
	{
		this.xAxis.set(1f, 0f, 0f);
		this.xAxis.set(0f, 1f, 0f);
		this.xAxis.set(0f, 0f, 1f);
		
		this.xAxis.rotate(rotation);
		this.yAxis.rotate(rotation);
		this.zAxis.rotate(rotation);
	}

	/**
	 * Performs an intersection test with this and another oriented bounding
	 * box
	 * 
	 * @param box The box to collide this against
	 * @return True if there is an intersection, false otherwise
	 */
	public boolean intersects(BoundingBox box) {
		Vector3f range = new Vector3f(box.center).sub(center);
		
		return !(
				isSeparated(range, xAxis, box) ||
				isSeparated(range, yAxis, box) ||
				isSeparated(range, zAxis, box) ||
				isSeparated(range, box.xAxis, box) ||
				isSeparated(range, box.yAxis, box) ||
				isSeparated(range, box.zAxis, box) ||
				isSeparated(range, cross(xAxis, box.xAxis), box) ||
				isSeparated(range, cross(xAxis, box.yAxis), box) ||
				isSeparated(range, cross(xAxis, box.zAxis), box) ||
				isSeparated(range, cross(yAxis, box.xAxis), box) ||
				isSeparated(range, cross(yAxis, box.yAxis), box) ||
				isSeparated(range, cross(yAxis, box.zAxis), box) ||
				isSeparated(range, cross(zAxis, box.xAxis), box) ||
				isSeparated(range, cross(zAxis, box.yAxis), box) ||
				isSeparated(range, cross(zAxis, box.zAxis), box)
		);
	}
	
	private boolean isSeparated(Vector3f range, Vector3f axis, BoundingBox box) {
		float minOverlap = Math.abs(range.dot(axis));
		
		// TODO: This algorithm sucks
		Vector3f xScaled = new Vector3f(xAxis).mul(halfExtents.x);
		Vector3f yScaled = new Vector3f(yAxis).mul(halfExtents.x);
		Vector3f zScaled = new Vector3f(zAxis).mul(halfExtents.x);
		
		Vector3f xScaledOther = new Vector3f(box.xAxis).mul(halfExtents.x);
		Vector3f yScaledOther = new Vector3f(box.yAxis).mul(halfExtents.x);
		Vector3f zScaledOther = new Vector3f(box.zAxis).mul(halfExtents.x);
		
		float separation = 
				Math.abs(xScaled.dot(axis)) +
				Math.abs(yScaled.dot(axis)) +
				Math.abs(zScaled.dot(axis)) +
				Math.abs(xScaledOther.dot(axis)) +
				Math.abs(yScaledOther.dot(axis)) +
				Math.abs(zScaledOther.dot(axis));
		
		if (minOverlap > separation) 
			return true;
		
		return false;
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
	public float raycast(Vector3f rayOrigin, Vector3f rayDirection) {
		Vector3f min = new Vector3f().sub(halfExtents);
		Vector3f max = new Vector3f().add(halfExtents);
		
		Vector3f rayOrigLocal = new Vector3f(rayOrigin).sub(center);
		Vector3f rayDirLocal = new Vector3f(rayDirection);

		Quaternionf invRotation = new Quaternionf(rotation).invert();
		
		rayDirLocal.rotate(invRotation);
		rayOrigLocal.rotate(invRotation);

		return AlignedBox.raycast(min, max, rayOrigLocal, rayDirLocal);
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
	
	/*
	 * Helper Functions
	 * 
	 */
	
	private Vector3f cross(Vector3f axis1, Vector3f axis2)
	{
		return new Vector3f(axis1).cross(axis2);
	}

	public void setRotation(Quaternionf rotation)
	{
		this.rotation = rotation;
		update();
	}
}
