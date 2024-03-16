package manatee.client.scene;

import org.joml.Vector3f;

import manatee.client.Time;

public class WindHandler
{
	private Vector3f windVector = new Vector3f(-0.5f, -0.5f, 0);
	private float windSpeed = 0.2f;
	private float windStrength = 0.25f;
	
	private float windTime;
	
	public float getWindSpeed()
	{
		return windSpeed;
	}
	
	public Vector3f getWindVector()
	{
		return windVector;
	}

	public void setWindSpeed(float windSpeed)
	{
		this.windSpeed = windSpeed;
	}

	public float getWindStrength()
	{
		return windStrength;
	}

	public void setWindStrength(float windStrength)
	{
		this.windStrength = windStrength;
	}

	public void tick()
	{
		windTime += Time.deltaTime * windSpeed;
	}
	
	public float getWindTime()
	{
		return windTime;
	}
}
