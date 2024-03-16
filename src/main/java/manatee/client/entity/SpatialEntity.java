package manatee.client.entity;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import manatee.client.scene.MapScene;
import manatee.maths.geom.BoundingBox;

/**
 * An entity with spatial positioning, that is to say, some sort of physical
 * bounding volume. The entity need not be visible
 * 
 * @author Jay
 *
 */
public abstract class SpatialEntity implements Entity
{
	protected Vector3f position = new Vector3f();
	protected Quaternionf rotation = new Quaternionf();
	protected Vector3f scale = new Vector3f(1f, 1f, 1f);

	protected BoundingBox boundingBox = new BoundingBox(position, new Vector3f());
	
	public Vector3f getPosition()
	{
		return position;
	}

	public Quaternionf getRotation()
	{
		return rotation;
	}

	public Vector3f getScale()
	{
		return scale;
	}
	
	public BoundingBox getBoundingBox()
	{
		return boundingBox;
	}
	
	public void setExtents(float halfX, float halfY, float halfZ)
	{
		boundingBox.halfExtents.set(halfX, halfY, halfZ);
	}
	
	@Override
	public void updateInternal(MapScene scene)
	{
		boundingBox.update();
	}

	@Override
	public void onSpawn()
	{
	}
	
	@Override
	public void onDespawn()
	{
	}
}
