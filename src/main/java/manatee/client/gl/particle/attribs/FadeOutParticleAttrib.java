package manatee.client.gl.particle.attribs;

import manatee.client.Time;
import manatee.client.gl.particle.Particle;

public class FadeOutParticleAttrib implements IParticleAttrib
{
	public float lifeStart;
	public float fadeFactor;
	
	public FadeOutParticleAttrib(float lifeStart, float fadeFactor)
	{
		this.lifeStart = lifeStart;
		this.fadeFactor = fadeFactor;
	}

	@Override
	public void init(Particle particle)
	{
	}

	@Override
	public void tick(Particle particle)
	{
		if (particle.getLife() > lifeStart)
		{
			particle.getColor().w -= fadeFactor * Time.deltaTime;
		}
	}

}
