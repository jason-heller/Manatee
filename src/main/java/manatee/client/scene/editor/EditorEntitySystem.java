package manatee.client.scene.editor;

import static manatee.client.gl.camera.CameraUtil.fov;
import static manatee.client.gl.camera.CameraUtil.near;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import lwjgui.LWJGUI;
import manatee.client.entity.EntitySystem;
import manatee.client.entity.Form;
import manatee.client.entity.SpatialEntity;
import manatee.client.entity.stock.TileEntity;
import manatee.client.map.MapGeometry;
import manatee.client.map.MapRegion;
import manatee.client.map.tile.Tile;
import manatee.maths.Maths;
import manatee.maths.MatrixMath;
import manatee.maths.geom.AlignedBox;

public class EditorEntitySystem extends EntitySystem
{
	private Map<MapRegion, List<TileEntity>> tileEntities;
	
	private MapGeometry geom;
	
	public EditorEntitySystem(MapGeometry geom, Vector3f lightColor, Vector3f lightVector)
	{
		super(lightColor, lightVector);
		this.geom = geom;
		tileEntities = new HashMap<>();
	}

	public void addTileEntity(TileEntity entity)
	{
		MapRegion r = geom.getRegionAt(entity.getPosition().x, entity.getPosition().y);
		
		if (!tileEntities.containsKey(r))
			tileEntities.put(r, new ArrayList<>());
		
		tileEntities.get(r).add(entity);
		
		entity.onSpawn();
	}
	
	public Collection<TileEntity> getTileEntities()
	{
		List<TileEntity> all = new ArrayList<>();
		
		for(List<TileEntity> list : tileEntities.values())
		{
			all.addAll(list);
		}
		
		return all;
	}

	public Collection<TileEntity> getTileEntitiesWithin(Vector3f min, Vector3f max)
	{
		return getTileEntitiesWithin(min.x, min.y, max.x, max.y);
	}
	
	public Collection<TileEntity> getTileEntitiesWithin(float minX, float minY, float maxX, float maxY)
	{
		List<TileEntity> result = new ArrayList<>();
		
		Collection<MapRegion> regions = geom.getRegionsIn(minX, minY, maxX-minX, maxY-minY);
		
		for(MapRegion region : regions)
		{
			List<TileEntity> list = tileEntities.get(region);
			
			if (list == null)
				continue;
			
			for(TileEntity e : list)
			{
				Vector3f p = e.getPosition();
				Vector3f s = e.getBoundingBox().halfExtents;
				if (minX > p.x + s.x || minY > p.y + s.y || maxX < p.x - s.x || maxY < p.y - s.y)
					continue;
				
				result.add(e);
			}
		}
		
		return result;
	}
	
	public SpatialEntity getTileEntityAt(float x, float y)
	{
		MapRegion region = geom.getRegionAt(x, y);

		if (tileEntities.get(region) == null)
			return null;
		
		for(TileEntity e : tileEntities.get(region))
		{
			Vector3f p = e.getPosition();
			Vector3f s = e.getBoundingBox().halfExtents;
			if (x > p.x + s.x || y > p.y + s.y || x < p.x - s.x || y < p.y - s.y)
				continue;
			
			return e;
		}
		
		return null;
	}

	public SpatialEntity raycastEntities(Matrix4f viewMatrix, Vector3f rayOrigin, Vector3f rayDirection, Vector3f collidePointOut)
	{
		float aspect = LWJGUI.getThreadWindow().getAspectRatio();
		
		Matrix4f projMatrix = new Matrix4f();
		MatrixMath.setProjectionMatrix(projMatrix, fov, aspect, near, RAYCAST_REACH);
		
		AlignedBox frustumAABB = MatrixMath.getFrustumAABB(projMatrix, viewMatrix);

		Collection<TileEntity> batch = getTileEntitiesWithin(frustumAABB.getMin(), frustumAABB.getMax());
		
		SpatialEntity entityCollided = null;
		float t = Float.POSITIVE_INFINITY;
		
		for(TileEntity entity : batch)
		{
			float dist = entity.getBoundingBox().raycast(rayOrigin, rayDirection);
			if (dist < t)
			{
				t = dist;
				entityCollided = entity;
			}
		}

		for (Form form : forms)
		{
			// if (!form.isVisible())
			// continue;

			float dist = form.getBoundingBox().raycast(rayOrigin, rayDirection);
			if (dist < t)
			{
				t = dist;
				entityCollided = form;
			}
		}
		
		if (collidePointOut != null && entityCollided != null)
			collidePointOut.set(rayDirection).mul(t).add(rayOrigin);
		
		return entityCollided;
	}

	public void removeTileEntity(TileEntity tile)
	{
		MapRegion r = geom.getRegionAt(tile.getPosition().x, tile.getPosition().y);
		
		tileEntities.get(r).remove(tile);
		
		tile.onDespawn();
	}

	public void clear()
	{
		super.clear();
		clearTileEntities();
	}

	public void clearTileEntities()
	{
		this.tileEntities.clear();
	}

	public void updateAdjacentTileEntities(MapGeometry geom, int x, int y)
	{
		int res = geom.getSpacing();
		
		float cx = x + (res / 2f);
		float cy = y + (res / 2f);
		
		float rad = 1f;
		
		Collection<TileEntity> entries = this.getTileEntitiesWithin(cx - rad, cy - rad, cx + rad, cy + rad);
		
		Iterator<TileEntity> iter = entries.iterator();
		
		//List<TileEntity> reAdd = new ArrayList<>();

		while(iter.hasNext())
		{
			TileEntity e = iter.next();

			int ex = Maths.floor(e.getPosition().x);
			int ey = Maths.floor(e.getPosition().y);
			
			Tile tile = geom.getTileAt(ex, ey);
			
			//reAdd.add(e);
			
			if (tile != Tile.AIR)
				e.setTile(tile, res / 2f);
		}
		
		/*for(TileEntity e : reAdd)
		{
			removeTileEntity(e);
		}
		
		for(TileEntity e : reAdd)
		{
			addTileEntity(e);
		}*/
	}
}
