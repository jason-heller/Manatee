package manatee.cache.definitions.lump;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import manatee.client.map.MapGeometry;
import manatee.client.map.MapRegion;
import manatee.client.map.light.ILight;
import manatee.client.map.light.PointLight;
import manatee.client.map.light.SpotLight;
import manatee.client.scene.editor.EditorScene;

public class LightLump implements Serializable
{
	public Vector3f position;
	public Quaternionf rotation;
	public Vector4f color;
	public float radius;

	public LightLump() {}
	
	public LightLump(ILight light)
	{
		this.position = light.getOrigin();
		this.rotation = light.getDirection();
		this.color = light.getColor();
		this.radius = light.getFalloff();
	}
	
	public void process(MapGeometry geom)
	{
		if (rotation == null)
		{
			addLight(geom, new SpotLight(position, rotation, color, radius));
		}
		else
		{
			addLight(geom, new PointLight(position, color, radius));
		}
	}

	private static void addLight(MapGeometry geom, ILight light)
	{
		float x = light.getOrigin().x;
		float y = light.getOrigin().y;
		float r = light.getFalloff();
		Collection<MapRegion> regions = geom.getRegionsIn(x-r, y-r, r*r, r*r);
		
		for(MapRegion reg : regions)
			reg.addLight(light);
	}
	
	public static LightLump[] load(EditorScene scene)
	{
		MapGeometry geom = scene.getMap().getGeometry();
		Iterator<MapRegion> iter = geom.getMapRegions().iterator();
		List<LightLump> lights = new LinkedList<>();

		while(iter.hasNext())
		{
			MapRegion region = iter.next();
			
			for(ILight light : region.getLights())
			{
				if (light == null || geom.getRegionAt(light.getOrigin().x, light.getOrigin().y) != region)
					continue;
				
				lights.add(new LightLump(light));
			}
		}
		
		return lights.toArray(new LightLump[lights.size()]);
	}
}
