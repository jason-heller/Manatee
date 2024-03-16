package manatee.client.entity;

import org.joml.Matrix4f;

import manatee.client.scene.MapScene;

public abstract class BillboardForm extends Form
{
	public BillboardForm()
	{
		
	}

	protected void updateModelMatrix(Matrix4f viewMatrix)
	{
		modelMatrix.identity();
		modelMatrix.translate(position.x, position.y, position.z);
		
		modelMatrix.m00(viewMatrix.m00());
		modelMatrix.m01(viewMatrix.m10());
		modelMatrix.m02(viewMatrix.m20());
		modelMatrix.m10(viewMatrix.m01());
		modelMatrix.m11(viewMatrix.m11());
		modelMatrix.m12(viewMatrix.m21());
		modelMatrix.m20(viewMatrix.m02());
		modelMatrix.m21(viewMatrix.m12());
		modelMatrix.m22(viewMatrix.m22());
		
		modelMatrix.rotate(rotation);
		modelMatrix.scale(scale.x, scale.y, scale.z);
	}
	
	@Override
	public void updateInternal(MapScene scene)
	{
		boundingBox.update();
		
		update(scene);
		
		updateModelMatrix(scene.getCamera().getViewMatrix());
	}
}
