package manatee.client.entity.stock;

import org.joml.Vector2f;

import manatee.client.entity.PathFollower;
import manatee.client.entity.PhysicsForm;
import manatee.client.gl.mesh.EntityShaderTarget;
import manatee.client.input.Input;
import manatee.client.input.Keybinds;
import manatee.client.scene.Assets;
import manatee.client.scene.MapScene;

public class Player extends PhysicsForm
{

	private float halfWidth = 1;
	private float halfHeight = 4;
	
	private PathFollower pathFollower = new PathFollower(position, rotation);
	
	public Player(Assets assets)
	{
		position.set(16, 16, 64);
		this.setExtents(halfWidth, halfWidth, halfHeight);
		this.setGraphic(assets.getModel("player"), EntityShaderTarget.ANIMATED);
		
		this.animator.setAnimation("player|Idle");
	}
	
	@Override
	public void update(MapScene scene)
	{
		if (Input.isPressed(Keybinds.ZOOM_IN))
		{
			//velocity.z = 10f;
		}
		
		boolean pathFinished = pathFollower.update(scene);
		
		if (pathFinished)
			animator.setAnimation("player|Idle");
	}

	public void setPath(Vector2f[] path)
	{
		pathFollower.setPath(path);
		
		if (pathFollower.getPath() != null)
			animator.setAnimation("player|Walk");
	}

}
