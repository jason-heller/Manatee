package manatee.client.map;

import org.joml.Quaternionf;

public class HeightQuery
{

	public static final HeightQuery NO_QUERY = new HeightQuery(0f, new Quaternionf());
	
	private float height;
	private Quaternionf rotation;
	
	public HeightQuery(float height, Quaternionf rotation)
	{
		this.height = height;
		this.rotation = rotation;
	}

	public float getHeight()
	{
		return height;
	}

	public Quaternionf getRotation()
	{
		return rotation;
	}

}
