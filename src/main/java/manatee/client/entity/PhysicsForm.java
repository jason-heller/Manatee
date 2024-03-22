package manatee.client.entity;

import org.joml.Vector3f;

import manatee.client.Time;
import manatee.client.map.MapGeometry;
import manatee.client.scene.GameMap;
import manatee.client.scene.MapScene;

/**
 * An form affected by physics, and interacts with the game environment.
 * In the context of the engine, a form is an entity with graphical representation
 * 
 * @author Jay
 *
 */
public abstract class PhysicsForm extends Form
{
	protected Vector3f velocity = new Vector3f();
	
	private boolean grounded = false;

	public PhysicsForm()
	{
	}

	@Override
	public void updateInternal(MapScene scene)
	{
		GameMap map = scene.getMap();
		MapGeometry geom = map.getGeometry();
		
		//int px = (int)position.x;
		//int py = (int)position.y;
		
		// Tile tile = geom.getTileAt(px, py);
		float height = geom.getHeightAt(position.x, position.y) + boundingBox.halfExtents.z;
		
		position.add(new Vector3f(velocity).mul(Time.deltaTime));
		
		velocity.z -= .25f;
		
		if (position.z <= height || (grounded && position.z - height < .25f))
		{
			position.z = height;
			velocity.z = 0f;
			grounded = true;
		}
		
		super.updateInternal(scene);
	}
}
