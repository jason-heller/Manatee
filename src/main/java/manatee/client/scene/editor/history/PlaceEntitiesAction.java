package manatee.client.scene.editor.history;

import java.util.ArrayList;
import java.util.List;

import manatee.client.entity.Entity;
import manatee.client.entity.EntitySystem;
import manatee.client.entity.Form;
import manatee.client.entity.stock.TileEntity;

public class PlaceEntitiesAction implements ReversableAction
{
	private EntitySystem entitySystem;
	
	private List<EntityPair> entities = new ArrayList<>();
	
	public PlaceEntitiesAction(EntitySystem entitySystem, Entity addedEntity, Entity removedEntity)
	{
		assert !(addedEntity instanceof TileEntity) : "EntityAction does not handle TileEntity objects";
		assert !(removedEntity instanceof TileEntity) : "EntityAction does not handle TileEntity objects";
		
		this.entitySystem = entitySystem;
		
		add(addedEntity, removedEntity);
	}

	public void add(Entity addedEntity, Entity removedEntity)
	{
		EntityPair pair = new EntityPair();
		pair.added = addedEntity;
		pair.removed = removedEntity;
		
		entities.add(pair);
	}

	@Override
	public void act()
	{
		for(EntityPair pair : entities)
		{
			add(pair.added);
			remove(pair.removed);
		}
	}

	@Override
	public void reverse()
	{
		for(EntityPair pair : entities)
		{
			remove(pair.added);
			add(pair.removed);
		}
	}

	private void add(Entity entity)
	{
		if (entity == null)
			return;
		
		if (entity instanceof Form)
		{
			entitySystem.addForm((Form) entity);
			return;
		}
		
		entitySystem.addEntity(entity);
	}
	
	private void remove(Entity entity)
	{
		if (entity == null)
			return;
		
		if (entity instanceof Form)
		{
			entitySystem.removeForm((Form) entity);
			return;
		}
		
		entitySystem.removeEntity(entity);
	}

	private class EntityPair
	{
		public Entity added;
		public Entity removed;
	}
}
