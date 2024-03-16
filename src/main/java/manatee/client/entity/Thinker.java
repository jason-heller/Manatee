package manatee.client.entity;

import manatee.client.scene.MapScene;

/**
 * An entity that has no physical presence, and acts as a 'thinker' object,
 * meaning it serves to handle some logic unseen to the player
 * 
 * @author Jay
 *
 */
public abstract class Thinker implements Entity
{
	public void updateInternal(MapScene scene)
	{
		update(scene);
	}
}
