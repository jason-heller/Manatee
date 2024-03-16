package manatee.client.gl.particle.attribs.emitter;

import org.joml.Vector3f;

import manatee.client.gl.particle.Particle;
import manatee.client.gl.particle.attribs.IParticleAttrib;
import manatee.maths.Vectors;

public class RectEmitterParticleAttrib implements IParticleAttrib
{
	public Vector3f origin;
	public Vector3f axis;
	private Vector3f tangent;
	private Vector3f coTangent;
	
	public float width, height;
	
	public RectEmitterParticleAttrib(Vector3f origin, Vector3f axis, float width, float height)
	{
		this.origin = origin;
		
		if (Math.abs(axis.z) == 1.0)
			tangent = Vectors.X_AXIS;
		else
			this.tangent = new Vector3f(axis).cross(Vectors.Z_AXIS);
		
		this.coTangent = new Vector3f(tangent).cross(axis).negate();
		
		this.width = width;
		this.height = height;
	}

	@Override
	public void init(Particle particle)
	{
		Vector3f pos = particle.getPosition();
		
		pos.set(new Vector3f(tangent).mul((float)Math.random() * width));
		pos.add(new Vector3f(coTangent).mul((float)Math.random() * height));
		
		pos.add(origin);
	}

	@Override
	public void tick(Particle particle)
	{

	}
}
