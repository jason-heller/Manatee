package manatee.client.scene.editor.history;

import java.util.HashSet;
import java.util.Set;

import org.joml.Quaternionf;

import manatee.client.entity.SpatialEntity;
import manatee.client.entity.stock.editor.EditorEntity;
import manatee.client.scene.editor.Transformation;

public class TransformAction implements ReversableAction
{
	private Set<EditorEntity> entities;
	
	private Transformation transform;
	
	private float x, y, z, w;
	
	public TransformAction(Transformation transform, float x, float y, float z, float w, Set<EditorEntity> entities)
	{
		this.transform = transform;
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		
		this.entities = new HashSet<EditorEntity>(entities);
	}

	@Override
	public void act()
	{
		for(SpatialEntity e : entities)
		{
			switch(transform)
			{
			case TRANSLATION:
				e.getPosition().add(x, y, z);
				break;
			case ROTATION:
				e.getRotation().mul(x, y, z, w);
				break;
			case SCALE:
				e.getScale().mul(x, y, z);
				break;
				default:
			}
		}
	}

	@Override
	public void reverse()
	{
		for(EditorEntity e : entities)
		{
			switch(transform)
			{
			case TRANSLATION:
				e.getPosition().sub(x,y,z);
				break;
			case ROTATION:
				Quaternionf q = new Quaternionf(x,y,z,w).invert();
				e.getRotation().mul(q.x, q.y, q.z, q.w);
				break;
			case SCALE:
				e.getScale().div(x, y, z);
				break;
				default:
			}
		}
	}
}
