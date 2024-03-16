package manatee.client.map.tile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.gson.Gson;

import manatee.cache.definitions.texture.ITexture;
import manatee.client.gl.mesh.TileMeshData;
import manatee.client.gl.mesh.TileShaderTarget;
import manatee.client.scene.Assets;

public class Tilemap
{
	public static boolean renderSkipped;

	private Map<Integer, Tile> tiles = new TreeMap<>();
	
	private TileAssets tileAssets;

	private final String TILESET_PATH = "src/main/resources/tile/";
	
	private final int[] cardinalFlags = {
			TileFlags.CLIP_X.bitmask(), TileFlags.parse(TileFlags.CLIP_Y, TileFlags.ROTATE_90CW, TileFlags.ILLEGAL)
	};
	
	private final String cornerPostFix = " Corner";
	
	private final String[] fourCornerPostFix = {
			" NW Corner", " NE Corner", " SW Corner", " SE Corner"
	};
	
	private final int[] fourCornerFlags = {
			0, TileFlags.FLIP_X.bitmask(), TileFlags.FLIP_Y.bitmask(), TileFlags.FLIP_XY
	};
	
	private int nTiles = 0;

	private void loadTileset(String tilesetName)
	{
		final int illegal = TileFlags.ILLEGAL.bitmask();
		int nSubtypes = 0;
		
		try
		{
			Gson gson = new Gson();
			String json;
			
			json = Files.readString(Paths.get(TILESET_PATH + tilesetName + "/tileset.json"), StandardCharsets.UTF_8);
			
			TileDataWrapper[] tileDataArray = gson.fromJson(json, TileDataWrapper[].class);	
			
			for(TileDataWrapper tileData : tileDataArray)
			{
				nTiles++;
				nSubtypes = 0;
				
				int flags = TileFlags.parse(tileData.flags);
				
				boolean hasTrailerRHS = (tileData.trailer != null);
				boolean hasTrailerLHS = (tileData.trailerOnBothEnds);
				boolean hasCorners = (tileData.corners != null);
				boolean hasCardinals = hasTrailerRHS || hasCorners;
				boolean flipCorners = tileData.flipCorners;
				
				if (hasTrailerRHS)
					flags |= TileFlags.HAS_TRAILER_RHS.bitmask();
				
				if (hasTrailerLHS)
					flags |= TileFlags.HAS_TRAILER_LHS.bitmask();
			
				if (hasCorners) {
					flags |= (flipCorners ? TileFlags.GROUP_CARDINAL_FOURCORNERS
							: TileFlags.GROUP_CARDINAL_CORNERS).bitmask();
				}
				
				// If we have cardinal directions, we have groups. Handle their entry differently
				if (hasCardinals)
				{
					for(int i = 0; i < 2; i++)
					{
						final int newFlags = flags | cardinalFlags[i];
						
						put(tileData.name, tileData.mesh, tileData.shader, tileData.texture, nSubtypes++, newFlags);
						
						// Put after first tile to ensure ID number are in-sync, the engine needs this
						// for tile replacement in groups
						if (hasTrailerRHS)
						{
							int removeClip = ~TileFlags.CLIP_XY;
							put(tileData.name, tileData.trailer, tileData.shader, tileData.texture, nSubtypes++, (newFlags | illegal) & removeClip);
						}
					}
					
					if (flipCorners)
					{
						for(int i = 0; i < 4; i++)
							put(tileData.name + fourCornerPostFix[i], tileData.corners, tileData.shader, tileData.texture, nSubtypes++, flags | fourCornerFlags[i] | illegal);
					}
					else
					{
						put(tileData.name + cornerPostFix, tileData.corners, tileData.shader, tileData.texture, nSubtypes++, flags | illegal);
					}
					
					continue;
				}
				
				put(tileData.name, tileData.mesh, tileData.shader, tileData.texture, 0, flags);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public Tilemap(Assets assets)
	{	
		tileAssets = new TileAssets(assets);
		tileAssets.loadAssets();
		
		put(Tile.AIR);
		
		loadTileset("global");
		loadTileset(tileAssets.getCurrentTileset());

		nTiles++;
	}

	private Tile put(String name, String meshName, TileShaderTarget shaderTarget, String textureName, int subType, int flags)
	{
		Tile tile = Tile.createTileGeneric(nTiles, subType, name, flags);
		
		TileMeshData mesh = tileAssets.getTileMesh(meshName);
		ITexture texture = tileAssets.getTexture(textureName);
		
		// TODO: Separate mesh data from tiles
		tile.setMeshData(mesh);
		tile.setTexture(texture);
		tile.setShaderTarget(shaderTarget);
		
		put(tile);
		
		return tile;
	}
	
	private void put(Tile tile)
	{
		tiles.put(tile.getTilemapIndex(), tile);
	}
	
	public Tile get(int id)
	{
		Tile tile = tiles.get(id);
		return tile == null ? Tile.MISSING : tile;
	}
	
	public Tile get(int id, int subId)
	{
		int fullId = id | (subId << 24);
		Tile tile = tiles.get(fullId);
		
		return tile == null ? Tile.MISSING : tile;
	}

	public Collection<Tile> values()
	{
		return tiles.values();
	}

	public Set<Integer> keys()
	{
		return tiles.keySet();
	}
	
	public TileAssets getTileAssets()
	{
		return tileAssets;
	}
	
	public void dispose()
	{
		tileAssets.dispose();
	}

	public void setTileset(String tilesetName)
	{
		this.tileAssets.setCurrentTileset(tilesetName);
		
		tiles.clear();
		
		put(Tile.AIR);
		nTiles = 0;
		
		loadTileset("global");
		loadTileset(tileAssets.getCurrentTileset());
		
		nTiles++;
	}

	public String getTileset()
	{
		return this.tileAssets.getCurrentTileset();
	}

	public int getTileCount()
	{
		return nTiles;
	}
}
