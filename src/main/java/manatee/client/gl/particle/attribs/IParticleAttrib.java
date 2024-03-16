package manatee.client.gl.particle.attribs;

import manatee.client.gl.particle.Particle;

public interface IParticleAttrib
{
	public void init(Particle particle);
	
	public void tick(Particle particle);
}
