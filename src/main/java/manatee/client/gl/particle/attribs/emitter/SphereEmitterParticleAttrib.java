package manatee.client.gl.particle.attribs.emitter;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import manatee.client.gl.particle.Particle;
import manatee.client.gl.particle.attribs.IParticleAttrib;
import manatee.maths.Maths;

public class SphereEmitterParticleAttrib implements IParticleAttrib
{
	public Vector3f origin;
	public float radius;
	
	public SphereEmitterParticleAttrib(Vector3f origin, float radius)
	{
		this.origin = origin;
		
		this.radius = radius;
	}

	@Override
	public void init(Particle particle)
	{
		Vector3f pos = new Vector3f();
		
		float yaw = (float)Math.random() * Maths.TWOPI;
		float pitch = (float)Math.random() * Maths.TWOPI;
		
		Quaternionf q = new Quaternionf();
		q.rotateX(pitch);
		q.rotateZ(yaw);
		
		pos.set(0,1,0);
		q.transform(pos);
		pos.mul((float)Math.random() * radius);
		pos.add(origin);
		

		particle.getPosition().add(pos);
	}

	@Override
	public void tick(Particle particle)
	{

	}
}
