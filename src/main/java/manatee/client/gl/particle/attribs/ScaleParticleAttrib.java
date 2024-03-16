package manatee.client.gl.particle.attribs;

import manatee.client.gl.particle.Particle;

public class ScaleParticleAttrib implements IParticleAttrib
{
	public float scale;

	public ScaleParticleAttrib(float scale)
	{
		this.scale = scale;
	}

	@Override
	public void init(Particle particle)
	{
		particle.getScale().set(scale, scale, scale);
	}

	@Override
	public void tick(Particle particle)
	{
	}
}
