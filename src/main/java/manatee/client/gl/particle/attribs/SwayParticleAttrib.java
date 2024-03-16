package manatee.client.gl.particle.attribs;

import manatee.client.gl.particle.Particle;

public class SwayParticleAttrib implements IParticleAttrib
{

	public float frequency, amplitude;
	
	public SwayParticleAttrib(float frequency, float amplitude)
	{
		this.frequency = frequency;
		this.amplitude = amplitude;
	}
	
	@Override
	public void init(Particle particle)
	{
	}

	@Override
	public void tick(Particle particle)
	{
		float sway = (float)Math.sin(particle.getPosition().z * frequency) * amplitude;
		
		particle.getPosition().x += sway;
	}

}
