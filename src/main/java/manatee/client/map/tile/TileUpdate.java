package manatee.client.map.tile;

public class TileUpdate
{
	public final int x, y;

	public final Tile originalTile, newTile;
	
	private boolean sameTexture;
	
	public TileUpdate(int x, int y, Tile originalTile, Tile newTile)
	{
		this.x = x;
		this.y = y;
		this.originalTile = originalTile;
		this.newTile = newTile;
		
		if (originalTile.getTexture() == newTile.getTexture())
		{
			sameTexture = true;
		}
	}
	
	public boolean isSameTexture()
	{
		return sameTexture;
	}
}
