package manatee.client.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector2i;

import manatee.client.map.tile.Tile;
import manatee.client.map.tile.TileUpdate;
import manatee.client.map.tile.Tilemap;
import manatee.client.scene.Assets;
import manatee.maths.Maths;
import manatee.maths.graphTheory.PathFind;

public class MapGeometry
{
	//private RTree<HeightField, Rectangle> heightFields = RTree.dimensions(2).create();
	//private RTree<TileField, Rectangle> tileFields = RTree.dimensions(2).create();
	
	private Map<Vector2i, MapRegion> regions = new HashMap<>();
	
	private Tilemap tilemap;
	
	private int spacing;
	private int resolution;
	
	public MapGeometry(Assets assets)
	{
		reset(assets, false);
		
		tilemap = new Tilemap(assets);
	}
	
	public void reset(Assets assets, boolean withDefaultField)
	{
		dispose();
		
		tilemap = new Tilemap(assets);
		spacing = 1;
		resolution = 33;
		
		regions = new HashMap<>();
		
		if (withDefaultField)
		{
			addRegion(new Vector2i(), new float[resolution][resolution], new int[resolution-1][resolution-1], resolution, resolution, spacing);
			
			buildAllMeshes();
		}
	}
	
	public Collection<MapRegion> getMapRegions()
	{
		return this.regions.values();
	}

	public void buildAllMeshes()
	{
		for(MapRegion region : regions.values())
		{
			region.buildAllMeshes(this, null);
		}
	}

	public void dispose()
	{
		for(MapRegion region: regions.values())
		{
			region.dispose();
		}
		
		// Remove tile related assets
		if (tilemap != null)
			tilemap.dispose();
	}

	public float getHeightAt(float x, float y)
	{
		MapRegion region = this.getRegionAt(x, y);
		
		if (region == null)
			return 0f;
		
		float height = region.getHeightData().getInterpolated(x, y);
		
		return height;
	}
	
	public HeightQuery queryHeightAt(int x, int y)
	{
		MapRegion region = this.getRegionAt(x, y);
		
		if (region == null)
			return HeightQuery.NO_QUERY;
		
		float height = region.getHeightData().getInterpolated(x, y);
		
		Quaternionf quat = new Quaternionf();
		// TODO: ROTATION
		
		return new HeightQuery(height, quat);
	}
	
	public int getTileIdAt(int x, int y)
	{
		MapRegion region = this.getRegionAt(x, y);
		
		if (region == null)
			return 0;
		
		int dx = x - region.getPosition().x;
		int dy = y - region.getPosition().y;
		
		if (dx < 0 || dy < 0 || region.getXResolution() - 2 < dx || region.getYResolution() - 2 < dy)
			return 0;
			
		return region.getTileData().get(x, y);
	}
	
	public int getTileTypeAt(int x, int y)
	{
		return getTileIdAt(x, y) & 0x00FFFFFF;
	}
	
	public Tile getTileAt(int x, int y)
	{
		return tilemap.get(getTileIdAt(x, y));
	}
	
	/**
	 * Rebuilds a tilefield mesh array
	 * 
	 * @param tf     the tile field to rebuild
	 * @param update represents the updated tile. use NULL to rebuild all
	 * @return the resulting tile added, or NULL if no TileUpdate was given
	 */
	public Tile rebuild(MapRegion region, TileUpdate update)
	{
		return region.buildAllMeshes(this, update);
	}
	
	public void rebuild(MapRegion region)
	{
		region.disposeHeightMesh();
		region.buildAllMeshes(this, null);
	}
	
	private void rebuildAll()
	{
		for(MapRegion region : regions.values())
			region.buildAllMeshes(this, null);
	}

	public Tilemap getTilemap()
	{
		return tilemap;
	}

	public int getSpacing()
	{
		return spacing;
	}
	
	public void setSpacing(int spacing)
	{
		this.spacing = spacing;
	}

	public int getResolution()
	{
		return resolution;
	}

	public void setResolution(int resolution)
	{
		this.resolution = resolution;
	}

	public MapRegion getRegionAt(float x, float y)
	{
		for(Entry<Vector2i, MapRegion> entry : regions.entrySet())
		{
			Vector2i pos = entry.getKey();
			
			if (pos.x > x || pos.y > y)
				continue;
			
			MapRegion region = entry.getValue();
			
			int width = region.getWidth();
			int height = region.getHeight();
			
			if (pos.x + width <= x + 1 || pos.y + height <= y + 1)
				continue;
			
			return region;
		}
		
		return null;
	}
	
	public Collection<MapRegion> getRegionsNear(float x, float y)
	{
		return getRegionsIn(x, y, 1f, 1f);
	}
	
	public Collection<MapRegion> getRegionsIn(float x, float y, float width, float height)
	{
		List<MapRegion> regionsNear = new ArrayList<>(1);
		
		for(Entry<Vector2i, MapRegion> entry : regions.entrySet())
		{
			Vector2i pos = entry.getKey();
			
			if (pos.x > x + width || pos.y > y + height)
				continue;
			
			MapRegion region = entry.getValue();
			
			int regWidth = region.getWidth();
			int regHeight = region.getHeight();
			
			if (pos.x + regWidth < x || pos.y + regHeight < y)
				continue;
			
			regionsNear.add(region);
		}
		
		return regionsNear;
	}

	public MapRegion addRegion(Vector2i pos, float[][] heights, int[][] tiles, int xRes, int yRes, int spacing)
	{
		MapRegion region = new MapRegion(pos, xRes, yRes, spacing);
		
		region.getHeightData().set(heights);
		region.getTileData().set(tiles);
		
		addRegion(region);
		
		return region;
	}
	
	public void addRegion(MapRegion region)
	{
		this.regions.put(region.getPosition(), region);
	}
	
	public void removeRegion(MapRegion region)
	{
		this.regions.remove(region.getPosition());
	}

	public void setTileset(String tileset)
	{
		tilemap.setTileset(tileset);
		rebuildAll();
	}

	public Vector2f[] createPath(float startX, float startY, float endX, float endY)
	{
		int x1 = Maths.floor(startX);
		int y1 = Maths.floor(startY);
		int x2 = Maths.floor(endX);
		int y2 = Maths.floor(endY);
		
		return PathFind.aStar(x1, y1, x2, y2, this);
	}
}
