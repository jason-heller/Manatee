package manatee.client.gl.particle.attribs.emitter;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import manatee.client.gl.particle.Particle;
import manatee.client.gl.particle.attribs.IParticleAttrib;
import manatee.maths.Maths;
import manatee.maths.MCache;

public class CircleEmitterParticleAttrib implements IParticleAttrib
{
	public Vector3f origin;
	public Vector3f axis;
	private Vector3f tangent;
	
	public float radius;
	
	public CircleEmitterParticleAttrib(Vector3f origin, Vector3f axis, float radius)
	{
		this.origin = origin;
		this.axis = axis;
		
		if (Math.abs(axis.z) == 1.0)
			tangent = MCache.X_AXIS;
		else
			this.tangent = new Vector3f(axis).cross(MCache.Z_AXIS);
		
		this.radius = radius;
	}

	@Override
	public void init(Particle particle)
	{
		Vector3f pos = new Vector3f();
		
		pos.set(tangent);
		
		float r = (float)Math.random() * Maths.TWOPI;
		Quaternionf rot = new Quaternionf().setAngleAxis(r, axis.x, axis.y, axis.z);
		
		rot.transform(pos);
		pos.mul(((float)Math.random()) * radius);
		pos.add(origin);
		
		particle.getPosition().add(pos);
	}

	@Override
	public void tick(Particle particle)
	{

	}
}
