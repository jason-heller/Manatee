package manatee.cache.definitions.lump;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.joml.Vector2i;

import manatee.client.map.MapGeometry;
import manatee.client.map.MapRegion;
import manatee.client.map.light.ILight;
import manatee.client.scene.editor.EditorScene;

public class RegionLump implements Serializable
{
	public Vector2i position;
	public float[][] heights;
	public int[][] tiles;
	public int xResolution, yResolution;
	public int spacing;

	public RegionLump() {}
	
	public RegionLump(MapRegion region)
	{
		this.position = region.getPosition();
		this.heights = region.getHeightData().get();
		this.tiles = region.getTileData().get();
		this.xResolution = region.getXResolution();
		this.yResolution = region.getYResolution();
		this.spacing = region.getSpacing();
	}

	public void process(MapGeometry geom)
	{
		geom.addRegion(position, heights, tiles, xResolution, yResolution, spacing);
	}

	public static RegionLump[] load(EditorScene scene)
	{
		MapGeometry geom = scene.getMap().getGeometry();
		RegionLump[] regLump = new RegionLump[geom.getMapRegions().size()];
		Iterator<MapRegion> iter = geom.getMapRegions().iterator();
		int i = 0;
		while(iter.hasNext())
		{
			MapRegion region = iter.next();
			regLump[i++] = new RegionLump(region);
		}
		
		return regLump;
	}
}
