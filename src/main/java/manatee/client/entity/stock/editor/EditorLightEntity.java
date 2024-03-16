package manatee.client.entity.stock.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.joml.Vector3f;
import org.joml.Vector4f;

import manatee.cache.definitions.Model;
import manatee.cache.definitions.mesh.IMesh;
import manatee.cache.definitions.texture.ITexture;
import manatee.client.dev.Dev;
import manatee.client.gl.mesh.EntityShaderTarget;
import manatee.client.map.MapRegion;
import manatee.client.map.light.AmbientLight;
import manatee.client.map.light.ILight;
import manatee.client.map.light.PointLight;
import manatee.client.map.light.SpotLight;
import manatee.client.scene.editor.EditorScene;

public class EditorLightEntity extends EditorEntity
{
	private ILight light;
	
	List<MapRegion> regions = new ArrayList<>();
	
	public EditorLightEntity(EditorScene scene, String name, Vector3f position, Vector4f color, IMesh mesh,
			ITexture texture, Model model)
	{
		super(scene, name, position, color, mesh, texture, model, EntityShaderTarget.GENERIC);
		
		switch(name)
		{
			case "PointLight":
			{
				light = new PointLight(this.position, this.color, 7f);
				//addLightToRegions();
				break;
			}
			case "SpotLight":
			{
				light = new SpotLight(this.position, this.rotation, this.color, 7f);
				//addLightToRegions();
				break;
			}
			case "AmbientLight":
			{
				light = new AmbientLight(this.position, this.rotation, this.color);
				break;
			}
		}
	}
	
	@Override
	public void onSpawn()
	{
		calcRegions();
	}
	
	@Override
	public void onTranslate()
	{
		calcRegions();
	}
	
	@Override
	public void onScale()
	{
		calcRegions();
	}
	
	private void calcRegions()
	{
		float rad = light.getFalloff();
		Collection<MapRegion> intersects = scene.getMap().getGeometry().getRegionsIn(position.x - rad, position.y - rad, rad*2, rad*2);
		Iterator<MapRegion> iter = regions.iterator();
		while(iter.hasNext())
		{
			MapRegion r = iter.next();
			
			if (!intersects.contains(r))
			{
				r.removeLight(light);
				iter.remove();
			}
		}
		
		for(MapRegion r : intersects)
		{
			if (!regions.contains(r))
			{
				r.addLight(light);
				regions.add(r);
			}
		}
	}
	
	@Override
	public void onTagEdit()
	{
		String falloff = tags.get("radius");
		try {
			if (falloff != null)
			{
				this.light.setFalloff(Float.parseFloat(falloff));
				calcRegions();
			}
		}
		catch (NumberFormatException e)
		{
			Dev.log(falloff + " is not a valid real number");
		}
	}

	@Override
	public void onDespawn()
	{
		for(MapRegion r : regions)
			r.removeLight(light);
	}
}
