package manatee.client.gl.particle.attribs;

import manatee.client.Time;
import manatee.client.gl.particle.Particle;

public class GravityParticleAttrib implements IParticleAttrib
{
	public float gravity;
	
	public GravityParticleAttrib(float gravity)
	{
		this.gravity = gravity;
	}

	@Override
	public void init(Particle particle)
	{
	}

	@Override
	public void tick(Particle particle)
	{
		particle.getVelocity().z -= gravity * Time.deltaTime;
	}

}
