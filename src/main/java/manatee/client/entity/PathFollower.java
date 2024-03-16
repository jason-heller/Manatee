package manatee.client.entity;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import manatee.client.Time;
import manatee.client.scene.MapScene;

public class PathFollower
{
	private int pathIndex = 0;
	private Vector2f[] path = null;
	private Vector2f segmentOrigin = new Vector2f();
	private Vector2f direction = new Vector2f();
	private float walkSpeed = 5f;
	private float segmentDistanceSqr;
	
	private boolean closedPath = false;
	
	private Vector3f position;
	
	public PathFollower(Vector3f position)
	{
		this.position = position;
	}
	
	public boolean hasClosedPath()
	{
		return closedPath;
	}
	
	public void setClosedPath(boolean closedpath)
	{
		this.closedPath = closedpath;
	}
	
	public void update(MapScene scene)
	{
		if (path != null)
			walkPath();
	}
	
	private void walkPath()
	{
		Vector2f pos = new Vector2f(position.x, position.y);
		
		float speed = walkSpeed * Time.deltaTime;
		
		// rotation.rotate
		
		position.x += direction.x * speed;
		position.y += direction.y * speed;
		
		float distanceTraveledSqr = pos.distanceSquared(segmentOrigin.x, segmentOrigin.y);
		
		if (distanceTraveledSqr >= segmentDistanceSqr)
		{
			segmentOrigin.set(path[pathIndex]);
			pathIndex = (pathIndex + 1) % path.length;
			
			Vector2f target = path[pathIndex];
			direction.set(target.x, target.y).sub(pos);
			direction.normalize();
			
			if (pathIndex == 0 && !closedPath)
				path = null;
			else
				segmentDistanceSqr = new Vector2f(position.x, position.y).distanceSquared(path[pathIndex].x, path[pathIndex].y);
		}
	}

	public void setPath(Vector2f[] path)
	{
		if (path == null || path.length == 0)
		{
			path = null;
			return;
		}
		
		this.path = path;
		pathIndex = 0;
		
		Vector2f pos = new Vector2f(new Vector2f(position.x, position.y));
		segmentOrigin.set(position.x, position.y);
		segmentDistanceSqr = pos.distanceSquared(path[0].x, path[0].y);
		
		Vector2f target = path[pathIndex];
		direction.set(target.x, target.y).sub(pos);
		direction.normalize();
	}
	
	public Vector2f[] getPath()
	{
		return path;
	}
}
