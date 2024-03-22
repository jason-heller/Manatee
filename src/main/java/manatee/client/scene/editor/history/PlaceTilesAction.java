package manatee.client.scene.editor.history;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2i;

import manatee.client.entity.SpatialEntity;
import manatee.client.entity.stock.TileEntity;
import manatee.client.map.MapGeometry;
import manatee.client.map.MapRegion;
import manatee.client.map.tile.Tile;
import manatee.client.map.tile.TileFlags;
import manatee.client.map.tile.TileUpdate;
import manatee.client.scene.editor.EditorEntitySystem;
import manatee.maths.Maths;

public class PlaceTilesAction implements ReversableAction
{
	private EditorEntitySystem entitySystem;
	private MapGeometry geom;

	private List<PlaceTileAction> batch = new ArrayList<>();

	public PlaceTilesAction(EditorEntitySystem entitySystem, SpatialEntity hoveredEntity, MapGeometry geom, MapRegion region, int newTileId, float x, float y)
	{
		this.entitySystem = entitySystem;
		this.geom = geom;
		
		add(region, hoveredEntity, newTileId, x, y);
		
	}

	public void add(MapRegion region, SpatialEntity entity, int newTileId, float x, float y)
	{
		Vector2i pos = region.getPosition();

		int txLocal = Math.floorDiv(Maths.floor(x - pos.x), region.getSpacing());
		int tyLocal = Math.floorDiv(Maths.floor(y - pos.y), region.getSpacing());

		txLocal = Math.min(txLocal, region.getXResolution() - 2);
		tyLocal = Math.min(tyLocal, region.getYResolution() - 2);

		int tx = txLocal * region.getSpacing();
		int ty = tyLocal * region.getSpacing();
		
		tx += pos.x;
		ty += pos.y;
		
		if (batch.size() > 0)
		{
			for(PlaceTileAction tile : batch)
			{
				if (tile.x == tx && tile.y == ty && tile.newTileId == newTileId)
					return;
			}
		}

		// Tile newTile = geom.getTileMap().get(newTileId);
		
		int oldTileId = region.getTileData().getLocal(txLocal, tyLocal) & 0x00FFFFFF;
		newTileId = newTileId & 0x00FFFFFF;
		
		if (oldTileId == newTileId)
			return;
		
		try {
			PlaceTileAction action = new PlaceTileAction(region, entity, tx, ty, newTileId, oldTileId);
			batch.add(action);
			
			action.act();
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			System.err.println("Array index out of bounds: " + tx + ", " + ty + " reg: " + region.getPosition());
		}
	}

	@Override
	public void act()
	{
		for(PlaceTileAction action : batch)
		{
			action.act();
		}
	}

	@Override
	public void reverse()
	{
		for(PlaceTileAction action : batch)
		{
			action.reverse();
		}
	}

	private class PlaceTileAction
	{
		private TileEntity addedEntity;
		private TileEntity removedEntity;
		
		private MapRegion homeField;
		private int x, y;
		private int newTileId, oldTileId;

		private SpatialEntity entity;
		
		public PlaceTileAction(MapRegion homeField, SpatialEntity entity, int x, int y, int newTileId, int oldTileId)
		{
			this.homeField = homeField;
			this.x = x;
			this.y = y;
			this.newTileId = newTileId;
			this.oldTileId = oldTileId;
			
			this.entity = entity;
		}
		
		public void act()
		{
			oldTileId = homeField.getTileData().get(x, y);
			
			Tile oldTile = geom.getTilemap().get(oldTileId);
			Tile newTile = geom.getTilemap().get(newTileId);
			
			if (oldTile.getType() == newTile.getType())
				return;
			
			// Dev.log("ACT",oldTile.getId(),newTile.getId());
			
			// We set it twice so tiles iterated thru earlier are aware of connectors
			homeField.getTileData().set(newTile.getTilemapIndex(), x, y);
			
			Tile resultTile = geom.rebuild(homeField, new TileUpdate(x, y, oldTile, newTile));
			newTileId = resultTile.getTilemapIndex();
			
			if (addedEntity == null)
			{
				if (resultTile != Tile.AIR)
				{
					boolean ignoreHeight = TileFlags.IGNORE_HEIGHTFIELD.isSet(newTile.getFlags());
					float zOffset = ignoreHeight ? 0f : geom.getHeightAt(x, y);
					
					addedEntity = new TileEntity(x, y, 0, homeField.getSpacing(), resultTile);
					addedEntity.getPosition().z = zOffset + addedEntity.getBoundingBox().halfExtents.z;	
				}
				
				removedEntity = (entity instanceof TileEntity) ? (TileEntity) entity : null;
			}
			
			entitySystem.updateAdjacentTileEntities(geom, x, y);
			
			addEntity(addedEntity);
			removeEntity(removedEntity);
		}

		public void reverse()
		{
			Tile oldTile = geom.getTilemap().get(oldTileId);
			Tile newTile = geom.getTilemap().get(newTileId);
			
			// We set it twice so tiles iterated thru earlier are aware of connectors
			homeField.getTileData().set(oldTile.getTilemapIndex(), x, y);
			
			Tile resultTile = geom.rebuild(homeField, new TileUpdate(x, y, newTile, oldTile));
			// homeField.setTile(resultTile.getId(), x, y);

			entitySystem.updateAdjacentTileEntities(geom, x, y);
			
			removeEntity(addedEntity);
			addEntity(removedEntity);
		}

		private void addEntity(TileEntity entity)
		{
			if (entity == null)
				return;
			
			entitySystem.addTileEntity(entity);
		}
		
		private void removeEntity(TileEntity entity)
		{
			if (entity == null)
				return;
			
			entitySystem.removeTileEntity(entity);
		}
	}
}
