package manatee.cache.definitions.lump;

import java.io.Serializable;

import org.joml.Vector3f;
import org.joml.Vector4f;

import manatee.client.map.MapGeometry;
import manatee.client.scene.MapScene;

public class MapInfoLump implements Serializable
{
	public String mapName;
	public String tileset;
	public int spacing, resolution;
	
	public Vector4f color, waterColor;
	public Vector3f lightColor, lightVector;

	public MapInfoLump() {}
	
	public MapInfoLump(String mapName, String tileset, int resolution, int spacing, Vector3f lightVector, Vector4f color, Vector3f lightColor, Vector4f waterColor)
	{
		this.mapName = mapName;
		this.tileset = tileset;
		this.spacing = spacing;
		this.resolution = resolution;
		
		this.color = color;
		this.lightColor = lightColor;
		this.lightVector = lightVector;
		this.waterColor = waterColor;	
	}

	public void process(MapScene scene, MapGeometry geom)
	{
		scene.getColor().set(color);
		scene.getLightColor().set(lightColor);
		scene.getLightVector().set(lightVector);
		// scene.getColor().set(waterColor);
		scene.setTileset(tileset);
		
		geom.setSpacing(spacing);
		geom.setResolution(resolution);
	}
}
