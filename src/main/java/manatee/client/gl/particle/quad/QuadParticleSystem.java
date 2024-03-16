package manatee.client.gl.particle.quad;

import manatee.client.gl.particle.ParticleAtlas;
import manatee.client.gl.particle.ParticleSystem;
import manatee.client.gl.particle.attribs.IParticleAttrib;

public class QuadParticleSystem extends ParticleSystem
{
	private ParticleAtlas particleAtlas;
	
	public QuadParticleSystem(IParticleAttrib[] attribs, ParticleAtlas particleAtlas)
	{
		super(attribs);
		this.particleAtlas = particleAtlas;
	}
	
	public ParticleAtlas getParticleAtlas()
	{
		return particleAtlas;
	}
}
