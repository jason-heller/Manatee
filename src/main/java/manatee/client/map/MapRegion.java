package manatee.client.map;

import org.joml.Vector2i;
import org.joml.Vector3f;

import manatee.cache.definitions.field.DataFieldf;
import manatee.cache.definitions.field.DataFieldi;
import manatee.cache.definitions.mesh.IMesh;
import manatee.client.gl.Shader;
import manatee.client.gl.mesh.HeightFieldMesh;
import manatee.client.gl.mesh.TileFieldMesh;
import manatee.client.map.light.ILight;
import manatee.client.map.tile.Tile;
import manatee.client.map.tile.TileUpdate;
import manatee.maths.Vectors;

public class MapRegion
{
	private DataFieldf heights;
	private DataFieldi tiles;
	
	private HeightFieldMesh heightMesh;
	private TileFieldMesh[] tileMeshes;
	
	private int xResolution, yResolution;
	private int spacing;
	
	private ILight[] lights;
	
	public static final int MAX_LIGHTS = 4;
	
	// private MapRegion[] children;
	
	private Vector2i position;
	
	public MapRegion(Vector2i position, int xResolution, int yResolution, int spacing)
	{
		this.position = position;
		this.xResolution = xResolution;
		this.yResolution = yResolution;
		this.spacing = spacing;
		
		lights = new ILight[4];
		
		heights = new DataFieldf(position, xResolution, yResolution, spacing);
		tiles = new DataFieldi(position, xResolution - 1, yResolution - 1, spacing);
	}
	
	/*public void loadMesh()
	{
		heightMesh = new HeightFieldMesh(heights.get(), xResolution, yResolution);
		tileMeshes = new TileFieldMesh[0];
	}*/
	
	public HeightFieldMesh getHeightMesh()
	{
		return heightMesh;
	}
	
	public TileFieldMesh[] getTileMeshes()
	{
		return tileMeshes;
	}

	/**
	 * Builds the mesh array, disposes the origin mesh array if exists
	 * 
	 * @param tileMap the tileMap
	 * @param heightQuery heightfield query object
	 * @param tileQuery tilefield query object
	 * @param update represents the updated tile. use NULL to rebuild all
	 * 
	 * @return the resulting tile added, or NULL if no TileUpdate was given
	 */
	public Tile buildAllMeshes(MapGeometry geom, TileUpdate update)
	{
		buildHeightMesh();
		
		Tile resultTile = buildTileMeshes(geom, update);
		
		return resultTile;
	}
	
	public void buildHeightMesh()
	{
		if (heightMesh != null)
			disposeHeightMesh();
		
		heightMesh = new HeightFieldMesh(heights.get(), xResolution, yResolution);
	}

	public Tile buildTileMeshes(MapGeometry geom, TileUpdate update)
	{
		if (tileMeshes != null)
			disposeTileMeshes();
		
		Tile resultTile = TileFieldMesh.build(geom, this, update);

		if (update != null && resultTile == null)
		{
			tileMeshes = new TileFieldMesh[0];
			return Tile.AIR;
		}
		
		return resultTile;
	}

	public void dispose()
	{
		disposeHeightMesh();
		disposeTileMeshes();
	}

	public void disposeHeightMesh()
	{
		if (heightMesh != null)
			heightMesh.dispose();
	}

	public void disposeTileMeshes()
	{
		for(IMesh mesh : tileMeshes)
		{
			if (mesh != null)
				mesh.dispose();
		}
		
		tileMeshes = null;
	}

	public DataFieldf getHeightData()
	{
		return heights;
	}
	
	public DataFieldi getTileData()
	{
		return tiles;
	}

	public Vector2i getPosition()
	{
		return position;
	}

	public int getXResolution()
	{
		return xResolution;
	}
	
	public int getYResolution()
	{
		return yResolution;
	}
	
	public int getSpacing()
	{
		return spacing;
	}

	public void setTileMeshes(TileFieldMesh[] meshes)
	{
		this.tileMeshes = meshes;
	}

	public int getWidth()
	{
		return xResolution * spacing;
	}
	
	public int getHeight()
	{
		return yResolution * spacing;
	}

	public ILight[] getLights()
	{
		return lights;
	}
	
	public void addLight(ILight light)
	{
		for(int i = 0; i < MAX_LIGHTS; i++)
		{
			if (lights[i] == null)
			{
				lights[i] = light;
				return;
			}
		}
		
		System.err.println("Too many lights in region: " + position.x + ", " + position.y);
	}

	public void passLights(Shader shader)
	{
		int i = 0;
		for (; i < MapRegion.MAX_LIGHTS; i++)
		{
			ILight light = lights[i];
			
			if (light == null)
				break;

			if (light.getDirection() != null)
			{
				Vector3f direction = new Vector3f(0, 1, 0);
				light.getDirection().transform(direction);
				shader.setUniform("v_LightVectors[" + i + "]", direction);
			}
			else
			{
				shader.setUniform("v_LightVectors[" + i + "]", Vectors.EMPTY);
			}

			shader.setUniform("v_LightOrigins[" + i + "]", light.getOrigin());
			shader.setUniform("v_LightColors[" + i + "]", light.getColor().x, light.getColor().y,
					light.getColor().z);
			shader.setUniform("v_LightRadii[" + i + "]", light.getFalloff());
		}
		
		shader.setUniform("v_LightNum", i);
	}

	public void removeLight(ILight light)
	{
		boolean found = false;
		for (int i = 0; i < MapRegion.MAX_LIGHTS; i++)
		{
			if (found)
				lights[i] = i == MapRegion.MAX_LIGHTS - 1 ? null : lights[i + 1];
			
			else if (lights[i] == light)
			{
				found = true;
				if (i == MapRegion.MAX_LIGHTS - 1)
				{
					lights[i] = null;
					return;
				}
				else
				{
					lights[i] = lights[i + 1];
				}
				
			}
		}
	}
}
