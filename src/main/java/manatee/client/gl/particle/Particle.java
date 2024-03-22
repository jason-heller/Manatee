package manatee.client.gl.particle;

import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import manatee.client.Time;

public class Particle
{
	private Vector3f position = new Vector3f();
	private Vector3f velocity = new Vector3f();
	
	private AxisAngle4f rotation = new AxisAngle4f();
	
	// private int order;
	
	private Vector4f color = new Vector4f(1,1,1,1);
	
	private Vector3f scale = new Vector3f(1,1,1);
	
	private float life;
	private float lifeSpan = 1f;

	public Particle(Vector3f position)
	{
		this.position.set(position);
	}
	
	public void tick()
	{
		float delta = Time.deltaTime;
		life += delta;
		
		position.add(velocity.x * delta, velocity.y * delta, velocity.z * delta);
	}

	/*public int getOrder()
	{
		return order;
	}

	public void setOrder(int order)
	{
		this.order = order;
	}*/

	public AxisAngle4f getRotation()
	{
		return rotation;
	}

	public Vector3f getScale()
	{
		return scale;
	}

	public float getLife()
	{
		return life;
	}

	public void setLife(float life)
	{
		this.life = life;
	}

	public float getLifeSpan()
	{
		return lifeSpan;
	}

	public void setLifeSpan(float lifeSpan)
	{
		this.lifeSpan = lifeSpan;
	}

	public Vector3f getPosition()
	{
		return position;
	}

	public Vector3f getVelocity()
	{
		return velocity;
	}

	public Vector4f getColor()
	{
		return color;
	}
	
	public boolean isExpired()
	{
		return life >= lifeSpan;
	}

	public void setColor(Vector4f color)
	{
		this.color = color;
	}
}
