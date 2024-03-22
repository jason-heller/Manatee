package manatee.client.entity.stock;

import manatee.client.entity.Form;
import manatee.client.gl.mesh.EntityShaderTarget;
import manatee.client.scene.Assets;
import manatee.client.scene.MapScene;

public class EquipmentEntity extends Form
{
	
	private Form parent;
	
	public EquipmentEntity(Assets assets, Form parent)
	{
		this.parent = parent;
		
		this.setGraphic(assets.getModel("hat"), EntityShaderTarget.ANIMATED);
		
		this.position = parent.getPosition();
		this.rotation = parent.getRotation();
	}

	@Override
	public void update(MapScene scene)
	{
		this.animator.setCurrentFrame(parent.getAnimator().getCurrentFrame());
	}

}
