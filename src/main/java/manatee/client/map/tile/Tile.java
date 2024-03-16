package manatee.client.map.tile;

import manatee.cache.definitions.texture.ITexture;
import manatee.client.gl.mesh.TileMeshData;
import manatee.client.gl.mesh.TileShaderTarget;
import manatee.client.scene.GlobalAssets;

public class Tile
{
	public static final Tile AIR = createTileGeneric(0, 0, "air", null, null, TileFlags.SKIPS_MESHING.bitmask());
	public static final Tile MISSING = createTileGeneric(-1, 0, "MISSING", TileAssets.CUBE, GlobalAssets.MISSING_TEX, 0);

	private int type;
	private int subtype;

	private String name;
	
	private TileMeshData tileMeshData = TileMeshData.EMPTY;
	
	private ITexture texture;
	
	private int flags;
	
	private TileShaderTarget shaderTarget = TileShaderTarget.GENERIC;
	
	private static Tile createTileGeneric(int id, int subtype, String name, TileMeshData mesh, ITexture tex, int flags)
	{
		Tile tile = new Tile(id, subtype, name);
		
		tile.setMeshData(mesh);
		tile.setFlags(flags);
		tile.setTexture(tex);
		
		return tile;
	}
	
	public static Tile createTileGeneric(int id, int subtype, String name, int flags)
	{
		Tile tile = new Tile(id, subtype, name);
		
		tile.setFlags(flags);
		tile.setTexture(null);
		
		return tile;
	}
	
	private Tile(int type, int subtype, String name)
	{
		this.type = type;
		this.subtype = subtype;
		this.name = name;
	}
	
	public void setFlags(int flags)
	{
		this.flags = flags;
	}
	
	public TileMeshData getTileMeshData()
	{
		return tileMeshData;
	}
	
	public void setMeshData(TileMeshData tileMeshData)
	{
		if (tileMeshData == null)
			return;
		
		this.tileMeshData = tileMeshData;
	}

	public void setTexture(ITexture texture)
	{
		if (texture == null)
			texture = GlobalAssets.MISSING_TEX;
		
		this.texture = texture;
	}

	public int getTilemapIndex()
	{
		return type | (subtype << 24);
	}
	
	
	public int getSubtype()
	{
		return subtype;
	}
	
	public boolean isFixedHeight()
	{
		return (flags & TileFlags.FIXED_HEIGHT.bitmask()) != 0;
	}
	
	public boolean isRotatesToSlope()
	{
		return (flags & TileFlags.ROTATE_TO_SLOPE.bitmask()) != 0;
	}
	
	
	public boolean isTriplanarTextureMapped()
	{
		return (flags & TileFlags.TRIPLANAR_TEXTURE_MAPPING.bitmask()) != 0;
	}

	public int getFlags()
	{
		return flags;
	}

	public String getName()
	{
		return name;
	}

	public ITexture getTexture()
	{
		return texture;
	}

	public boolean isConnectable()
	{
		return (flags & TileFlags.CONNECTABLE) != 0;
	}

	public boolean hasTrailerOnRight()
	{
		return (flags & TileFlags.HAS_TRAILER_RHS.bitmask()) != 0;
	}
	
	public boolean hasTrailerOnLeft()
	{
		return (flags & TileFlags.HAS_TRAILER_LHS.bitmask()) != 0;
	}
	
	public int getNumCorners()
	{
		if ((flags & TileFlags.GROUP_CARDINAL_CORNERS.bitmask()) != 0)
			return 1;
		
		if ((flags & TileFlags.GROUP_CARDINAL_FOURCORNERS.bitmask()) != 0)
			return 4;
		
		return 0;
	}

	public boolean isIllegal()
	{
		return (flags & TileFlags.ILLEGAL.bitmask()) != 0;
	}
	
	public TileShaderTarget getShaderTarget()
	{
		return shaderTarget;
	}

	public void setShaderTarget(TileShaderTarget shaderTarget)
	{
		this.shaderTarget = shaderTarget;
		
		if (this.shaderTarget == null)
			this.shaderTarget = TileShaderTarget.GENERIC;
	}

	public int getType()
	{
		return type;
	}

	public boolean isSolid()
	{
		return TileFlags.SOLID.isSet(flags);
	}
}
