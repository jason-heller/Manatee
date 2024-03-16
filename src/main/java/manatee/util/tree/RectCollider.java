package manatee.util.tree;

public class RectCollider implements SpatialCollider
{
	private float[] center;
	private float[] bounds;

	public RectCollider(float x, float y, float w, float h)
	{
		center = new float[] {x, y};
		bounds = new float[] {w, h};
	}
	
	@Override
	public float[] getCenter()
	{
		return center;
	}

	@Override
	public float[] getBounds()
	{
		return bounds;
	}

}
