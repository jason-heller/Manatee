package manatee.client.entity;

import org.joml.Vector3f;

import manatee.client.Time;
import manatee.client.map.MapGeometry;
import manatee.client.map.tile.Tile;
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

	public PhysicsForm()
	{
	}

	@Override
	public void updateInternal(MapScene scene)
	{
		GameMap map = scene.getMap();
		MapGeometry geom = map.getGeometry();
		
		int px = (int)position.x;
		int py = (int)position.y;
		
		Tile tile = geom.getTileAt(px, py);
		float height = geom.getHeightAt(px, py);
		
		position.add(new Vector3f(velocity).mul(Time.deltaTime));
		
		velocity.z -= .25f;
		
		if (position.z <= height + boundingBox.halfExtents.z)
		{
			position.z = height + boundingBox.halfExtents.z;
			velocity.z = 0f;
		}
		
		super.updateInternal(scene);
	}
}
