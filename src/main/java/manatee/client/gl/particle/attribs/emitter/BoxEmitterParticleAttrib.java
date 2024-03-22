package manatee.client.gl.particle.attribs.emitter;

import org.joml.Vector3f;

import manatee.client.gl.particle.Particle;
import manatee.client.gl.particle.attribs.IParticleAttrib;

public class BoxEmitterParticleAttrib implements IParticleAttrib
{
	public Vector3f origin;
	public Vector3f bounds;
	
	public BoxEmitterParticleAttrib(Vector3f origin, Vector3f bounds)
	{
		this.origin = origin;
		this.bounds = bounds;
	}

	@Override
	public void init(Particle particle)
	{
		Vector3f pos = particle.getPosition();
		
		float x = origin.x + (((float)Math.random()) * bounds.x);
		float y = origin.y + (((float)Math.random()) * bounds.y);
		float z = origin.z + (((float)Math.random()) * bounds.z);
		
		pos.add(x, y, z);
	}

	@Override
	public void tick(Particle particle)
	{
	
	}
}
