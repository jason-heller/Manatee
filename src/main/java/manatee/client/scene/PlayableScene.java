package manatee.client.scene;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import manatee.client.entity.stock.EquipmentEntity;
import manatee.client.entity.stock.Player;

public abstract class PlayableScene extends MapScene
{
	protected Player player;
	
	public void spawnPlayer(Vector3f position, Quaternionf rotation)
	{
		this.player = new Player(assets);
		player.getPosition().set(position);
		player.getRotation().set(rotation);
		entitySystem.setPlayer(player);

		
		entitySystem.addForm(new EquipmentEntity(assets, player));
	}
}
