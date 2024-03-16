package manatee.client.gl.particle.attribs;

import org.joml.Vector4f;

import manatee.client.gl.particle.Particle;

public class ColorParticleAttrib implements IParticleAttrib
{
	public Vector4f color;
	
	public ColorParticleAttrib(Vector4f color)
	{
		this.color = color;
	}

	@Override
	public void init(Particle particle)
	{
		particle.getColor().set(color);
	}

	@Override
	public void tick(Particle particle)
	{
	}

	public Vector4f getColor()
	{
		return color;
	}
}
