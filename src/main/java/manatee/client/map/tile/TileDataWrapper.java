package manatee.client.map.tile;

import manatee.client.gl.mesh.TileShaderTarget;

public class TileDataWrapper
{
	
	public String name;
	public String mesh;
	public String texture;

	public String trailer;
	public String corners;
	
	public TileShaderTarget shader;
	
	public boolean flipCorners;
	public boolean trailerOnBothEnds;

	public String[] flags;

}
