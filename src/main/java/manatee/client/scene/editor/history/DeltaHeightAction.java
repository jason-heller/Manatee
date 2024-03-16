package manatee.client.scene.editor.history;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.joml.Vector2i;

import manatee.client.map.MapGeometry;
import manatee.client.map.MapRegion;
import manatee.client.scene.editor.HeightToolMode;
import manatee.maths.Maths;

public class DeltaHeightAction implements IHeightAction
{
	protected MapGeometry geom;
	protected float delta, radius;
	private HeightToolMode mode;
	
	protected int spacing;
	
	protected List<Vector2i> changes = new ArrayList<>();

	public DeltaHeightAction(MapGeometry geom, float delta, float radius, int spacing, HeightToolMode mode)
	{
		this.geom = geom;

		this.delta = delta;
		this.radius = radius;
		this.spacing = spacing;
		this.mode = mode;
	}
	
	@Override
	public void addChange(float x, float y, float delta)
	{
		int ix = Math.floorDiv(Maths.floor(x), spacing) * spacing;
		int iy = Math.floorDiv(Maths.floor(y), spacing) * spacing;
		
		changes.add(new Vector2i(ix, iy));
		
		List<MapRegion> fields = addHeight(ix, iy, delta, radius);
		
		for(MapRegion hf : fields)
			geom.rebuild(hf);
	}

	@Override
	public void act()
	{
		for(Vector2i change : changes)
		{
			List<MapRegion> fields = addHeight(change.x, change.y, delta, radius);
			
			for(MapRegion hf : fields)
				geom.rebuild(hf);
		}
		
		updateTilesets();
	}

	@Override
	public void reverse()
	{
		for(Vector2i change : changes)
		{
			List<MapRegion> fields = addHeight(change.x, change.y, -delta, radius);
			
			for(MapRegion hf : fields)
				geom.rebuild(hf);
		}
		
		updateTilesets();
	}

	private List<MapRegion> addHeight(int centerX, int centerY, float delta, float radius)
	{
		int iRadius = ((int) radius) + 1;
		
		List<MapRegion> fields = new LinkedList<>();

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
						
						switch(mode)
						{
						case RAISE:
							region.getHeightData().add(delta * strength, pixelX, pixelY);
							break;
						case LOWER:
							region.getHeightData().add(-delta * strength, pixelX, pixelY);
							break;
						default:
							throw new IllegalArgumentException("AddHeightAction only takes raise or lower heightmodes");
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
	}
}