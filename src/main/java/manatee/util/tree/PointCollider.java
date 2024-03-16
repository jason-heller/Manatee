package manatee.util.tree;

public class PointCollider implements SpatialCollider
{
	private float[] center;
	private static final float[] bounds = new float[] {0f, 0f};

	public PointCollider(float x, float y)
	{
		center = new float[] {x, y};
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
