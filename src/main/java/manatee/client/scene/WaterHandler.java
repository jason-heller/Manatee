package manatee.client.scene;

import org.joml.Vector2f;
import org.joml.Vector4f;

import manatee.client.Time;

public class WaterHandler
{
	Vector4f color = new Vector4f(0, 0, 1, 0.75f);
	private Vector2f flowDirection = new Vector2f(0, 1);
	private float flowSpeed = 0.2f;
	private float waveAmplitude = 1f;
	
	private Vector2f flowOffset = new Vector2f();

	public Vector4f getColor()
	{
		return color;
	}

	public Vector2f getFlowDir()
	{
		return flowDirection;
	}

	public float getFlowSpeed()
	{
		return flowSpeed;
	}

	public void setFlowSpeed(float flowSpeed)
	{
		this.flowSpeed = flowSpeed;
	}
	
	public void tick()
	{
		Vector2f delta = new Vector2f(flowDirection);
		delta.mul(flowSpeed * Time.deltaTime);
		
		flowOffset.add(delta);
	}

	public Vector2f getFlowOffset()
	{
		return flowOffset;
	}

	public float getWaveAmplitude()
	{
		return waveAmplitude;
	}

	public void setWaveAmplitude(float waveAmplitude)
	{
		this.waveAmplitude = waveAmplitude;
	}
}
