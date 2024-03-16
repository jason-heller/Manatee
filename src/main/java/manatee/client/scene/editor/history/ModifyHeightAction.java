package manatee.client.scene.editor.history;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joml.Vector2f;
import org.joml.Vector2i;

import manatee.client.map.MapGeometry;
import manatee.client.map.MapRegion;
import manatee.client.scene.editor.HeightToolMode;
import manatee.maths.Maths;

public class ModifyHeightAction implements IHeightAction
{
	private MapGeometry geom;
	
	private float radius;
	private HeightToolMode mode;
	
	private int spacing;
	
	private Map<Vector2i, Vector2f> changes = new HashMap<>();

	public ModifyHeightAction(MapGeometry geom, float radius, int spacing, HeightToolMode mode)
	{
		this.geom = geom;

		this.radius = radius;
		this.spacing = spacing;
		this.mode = mode;
	}
	
	@Override
	public void addChange(float x, float y, float delta)
	{
		int ix = Math.floorDiv(Maths.floor(x), spacing) * spacing;
		int iy = Math.floorDiv(Maths.floor(y), spacing) * spacing;
		
		List<MapRegion> fields = addHeight(ix, iy, delta, radius);
		
		for(MapRegion hf : fields)
			geom.rebuild(hf);
	}

	@Override
	public void act()
	{
		for(Vector2i change : changes.keySet())
		{
			Vector2f value = changes.get(change);
			Collection<MapRegion> regions = geom.getRegionsNear(change.x, change.y);
			
			for(MapRegion region : regions)
			{
				region.getHeightData().set(value.y, change.x, change.y);
				geom.rebuild(region);
			}
		}

		updateTilesets();
	}

	@Override
	public void reverse()
	{
		for(Vector2i change : changes.keySet())
		{
			Vector2f value = changes.get(change);
			Collection<MapRegion> regions = geom.getRegionsNear(change.x, change.y);
			
			for(MapRegion region : regions)
			{
				region.getHeightData().set(value.x, change.x, change.y);
				geom.rebuild(region);
			}
		}
		
		updateTilesets();
	}

	private List<MapRegion> addHeight(int centerX, int centerY, float delta, float radius)
	{
		int iRadius = ((int) radius) + 1;
		
		List<MapRegion> fields = new LinkedList<>();
		
		float originHeight = 0;
		
		if (mode == HeightToolMode.FLATTEN || mode == HeightToolMode.SMOOTH)
		{
			MapRegion f = geom.getRegionAt(centerX, centerY);
			originHeight = f.getHeightData().get(centerX, centerY);
		}

		for (int x = -iRadius; x <= iRadius; x++)
		{
			for (int y = -iRadius; y <= iRadius; y++)
			{
				if (x * x + y * y <= iRadius * iRadius)
				{

					int pixelX = centerX + (x * spacing);
					int pixelY = centerY + (y * spacing);
					
					Collection<MapRegion> regions = geom.getRegionsNear(pixelX, pixelY);
					
					if (regions == null)
						continue;
					
					for(MapRegion region : regions)
					{
						int fx1 = (int) region.getPosition().x;
						int fy1 = (int) region.getPosition().y;
						int fx2 = (fx1 + region.getWidth());
						int fy2 = (fy1 + region.getHeight());
						
						if (pixelX < fx1 || pixelY < fy1 || pixelX >= fx2 || pixelY >= fy2)
							continue;
						
						if (!fields.contains(region))
							fields.add(region);
						
						float strength = Math.max((iRadius - (float)Math.sqrt(x*x + y*y)) / iRadius, 0f);						
						
						Vector2i change = new Vector2i(pixelX, pixelY);
						Vector2f heightData;
						
						if (!changes.containsKey(change))
						{
							float originalHeight = region.getHeightData().get(pixelX, pixelY);
							heightData = new Vector2f(originalHeight, originalHeight);
							changes.put(change, heightData);
						}
						else
						{
							heightData = changes.get(change);
						}
						
						float value;
						
						switch(mode)
						{
						case RAISE:
							value = delta * strength;
							region.getHeightData().add(value, pixelX, pixelY);
							heightData.y += value;
							break;
							
						case LOWER:
							value = -delta * strength;
							region.getHeightData().add(value, pixelX, pixelY);
							heightData.y += value;
							break;
							
						case SMOOTH:
							float height = region.getHeightData().get(pixelX, pixelY);
							value = Maths.lerp(height, originHeight, .1f * strength);
							
							region.getHeightData().set(value, pixelX, pixelY);
							heightData.y = value;
							break;
							
						case FLATTEN:
							region.getHeightData().set(originHeight, pixelX, pixelY);
							heightData.y = originHeight;
							break;
							
						case NOISE:
							break;
							
						case ZERO:
							region.getHeightData().set(0f, pixelX, pixelY);
							heightData.y = 0f;
							break;
						}
					}
					}
			}
		}
		
		return fields;
    }

	@Override
	public void updateTilesets()
	{
		/*List<TileField> fields = new ArrayList<>();
		
		for(Vector2i change : changes.keySet())
		{
			TileField tf = geom.getTileFieldAt(change.x, change.y);
			
			if (tf == null)
				continue;
			
			if (!fields.contains(tf))
				fields.add(tf);
			
			geom.rebuild(tf, null);
		}*/
	}
}