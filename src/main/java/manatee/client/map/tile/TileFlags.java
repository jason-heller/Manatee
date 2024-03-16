package manatee.client.map.tile;

public enum TileFlags
{
	/**
	 * Will not be added to the tile mesh if set, effectively making this tile
	 * invisible. Only meant for special tiles such as tile 0 (air)
	 */
	SKIPS_MESHING(0),

	/** Ignores all heightfields and sets the origin's Z component to 0 if set */
	IGNORE_HEIGHTFIELD(1),

	/** If set, this mesh willrotate to match the slope of the terrain */
	ROTATE_TO_SLOPE(2),

	/** Ignores all heightfields and sets the origin's Z component to 0.0 if set */
	FIXED_HEIGHT(3),

	/** If set, clips tile to fit in tile bounds on the X axis */
	CLIP_X(4),

	/** If set, clips tile to fit in tile bounds on the Y axis */
	CLIP_Y(5),

	/** If set, mirrors the mesh on the X axis */
	FLIP_X(6),

	/** If set, mirrors the mesh on the Y axis */
	FLIP_Y(7),

	/**
	 * If set, this mesh will be assigned texture coordinates via triplanar mapping.
	 * Any previous texture coordinates are discarded.
	 */
	TRIPLANAR_TEXTURE_MAPPING(8),

	PROJECT_ONTO_TERRAIN(9),

	/** If set, rotates the mesh 90 degrees clockwise */
	ROTATE_90CW(10),

	/** If set, rotates the mesh a random number between 0 and 2pi */
	VARY_ZROTATION(11),

	/** Marks this tile as a solid collidable */
	SOLID(12),

	/**
	 * This tile is part of a set of cardinal tiles (they have orientation), where
	 * both directions do not connect, but rather have trailers. Meaning, there is a
	 * special tile that acts as the 'cap' of the line of cardinal tiles.
	 * 
	 * <br>
	 * <br>
	 * =============================================================<br>
	 * The set is as such:<br>
	 * =============================================================<br>
	 * <b>X Tile</b>: one tile that acts as the horizontal run<br>
	 * <b>Y Tile</b>: one tile that acts as the vertical run<br>
	 * <b>X Trailer</b>: this tile goes at the end of the horizontal tile line<br>
	 * <b>Y Trailer</b>: Same as above for the Y tile
	 */
	HAS_TRAILER_RHS(13),
	
	/**
	 * This tile is part of a set of cardinal tiles (they have orientation), where
	 * both directions connect via a shared corner tile.
	 * 
	 * <br>
	 * <br>
	 * =============================================================<br>
	 * The set is as such:<br>
	 * =============================================================<br>
	 * <b>X Tile</b>: one tile that acts as the horizontal run<br>
	 * <b>Y Tile</b>: one tile that acts as the vertical run<br>
	 * <b>Corner Tile</b>: This tile acts as the start/end of both runs, and
	 * connects the two tiles where they meet.
	 */
	GROUP_CARDINAL_CORNERS(14),

	/**
	 * This tile is part of a set of cardinal tiles (they have orientation), where
	 * both directions connect via corner tiles. Refer to 'GROUP_CARDINAL_CORNERS'.
	 * This one is the same, except there are four corner tiles, one for NW, NE, SW,
	 * and SE
	 */
	GROUP_CARDINAL_FOURCORNERS(15),

	/**
	 * If set, tile is considered nature related. Mainly used for the editor
	 */
	NATURE(16),

	/**
	 * If set, tile is considered building related. Mainly used for the editor
	 */
	BUILDING(17),
	
	/**
	 * If set, tile is considered misc. Mainly used for the editor
	 */
	MISCELLANEOUS(18),
	
	/** Marks this tile as unobtainium. Has minor implications game-mechanics wise */
	ILLEGAL(19),
	
	HAS_TRAILER_LHS(20),
	
	UNUSED2(21),
	
	UNUSED3(22),
	
	UNUSED4(23),
	
	UNUSED6(24),
	
	UNUSED7(25),
	
	UNUSED8(26),
	
	UNUSED9(27),
	
	SHADER0(28),
	
	SHADER1(29),
	
	SHADER2(30);
	
	// Make flags into long if you need more than 30
	
	public static int CLIP_XY = parse(CLIP_X, CLIP_Y);
	public static int FLIP_XY = parse(FLIP_X, FLIP_Y);
	
	public static int WALL_X_FLAGS = parse(PROJECT_ONTO_TERRAIN, TRIPLANAR_TEXTURE_MAPPING, SOLID, BUILDING);
	public static int WALL_Y_FLAGS = parse(PROJECT_ONTO_TERRAIN, TRIPLANAR_TEXTURE_MAPPING, SOLID, BUILDING, ROTATE_90CW);
	public static int WALL_CORNER_FLAGS = parse(PROJECT_ONTO_TERRAIN, TRIPLANAR_TEXTURE_MAPPING, SOLID, BUILDING);
	public static int FLOOR_FLAGS = parse(PROJECT_ONTO_TERRAIN, TRIPLANAR_TEXTURE_MAPPING, BUILDING);
	public static int CONNECTABLE = parse(GROUP_CARDINAL_CORNERS, GROUP_CARDINAL_FOURCORNERS, HAS_TRAILER_RHS);
	
	private int bitMask;
	
	TileFlags(int i)
	{
		bitMask = (1 << i);
	}

	public boolean isSet(int flags)
	{
		return (flags & bitmask()) != 0;
	}
	
	static int parse(TileFlags...flags)
	{
		int i = 0;
		
		for(TileFlags flag : flags)
		{
			i += flag.bitmask();
		}
		
		return i;
	}

	public static int parse(String[] flagNames)
	{
		int flags = 0;
		
		for(String flagName : flagNames)
		{
			switch(flagName)
			{
			case "CLIP_XY":
				flags |= CLIP_XY;
				break;
			case "WALL_X_FLAGS":
				flags |= WALL_X_FLAGS;
				break;
			case "WALL_Y_FLAGS":
				flags |= WALL_Y_FLAGS;
				break;
			case "WALL_CORNER_FLAGS":
				flags |= WALL_CORNER_FLAGS;
				break;
			case "FLOOR_FLAGS":
				flags |= FLOOR_FLAGS;
				break;
			default:
				TileFlags flag = TileFlags.valueOf(flagName);
				
				flags |= (1 << flag.ordinal());
			}
		}
		
		return flags;
	}

	public int bitmask()
	{
		return bitMask;
	}
}