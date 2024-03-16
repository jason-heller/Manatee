package manatee.client.entity.stock;

import org.joml.Vector3f;

import manatee.client.entity.SpatialEntity;
import manatee.client.map.tile.Tile;
import manatee.client.map.tile.TileFlags;
import manatee.client.scene.MapScene;

public class TileEntity extends SpatialEntity
{
	
	private Tile tile;

	public TileEntity(int x, int y, int z, int resolution, Tile tile)
	{

		float halfRes = resolution / 2f;
		
		setTile(tile, halfRes);
		
		this.position.set(x + halfRes, y + halfRes, z + boundingBox.halfExtents.z);
		
	}

	public void setTile(Tile t, float extentsLimits)
	{
		int flags = t.getFlags();
		
		Vector3f max = new Vector3f(t.getTileMeshData().getMax());
		Vector3f min = new Vector3f(t.getTileMeshData().getMin());

		if (TileFlags.FLIP_X.isSet(flags))
			max.x = -max.x;
		
		if (TileFlags.FLIP_Y.isSet(flags))
			min.y = -min.y;
		
		if (TileFlags.ROTATE_90CW.isSet(flags))
		{
			float maxY = max.y;
			float minY = min.y;
			
			max.y = max.x;
			min.y = min.x;
			max.x = maxY;
			min.x = minY;
		}
		
		Vector3f extents = new Vector3f(max).sub(min).mul(0.5f).absolute();
		
		//if (extents.z > 0.1f)
		//{
			extents.x = Math.min(extents.x, extentsLimits);
			extents.y = Math.min(extents.y, extentsLimits);
		//}
		
		this.setExtents(extents.x, extents.y, extents.z);

		this.tile = t;
	}

	@Override
	public void update(MapScene scene)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateInternal(MapScene scene)
	{
		// TODO Auto-generated method stub
		
	}

	public Tile getTile()
	{
		return tile;
	}

}
