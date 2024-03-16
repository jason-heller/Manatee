package manatee.client.gl.particle.attribs;

import manatee.client.gl.particle.Particle;

public class LifeParticleAttrib implements IParticleAttrib
{
	public float length;

	public LifeParticleAttrib(float length)
	{
		this.length = length;
	}

	@Override
	public void init(Particle particle)
	{
		particle.setLifeSpan(length);
	}

	@Override
	public void tick(Particle particle)
	{
	}
}
