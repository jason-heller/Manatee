package manatee.client.gl.particle.attribs;

import org.joml.Vector3f;

import manatee.client.gl.particle.Particle;

public class ImpulseParticleAttrib implements IParticleAttrib
{
	public Vector3f axis;

	public float axisVarianceRad;

	public float force;

	public float forceVariance;

	public ImpulseParticleAttrib(Vector3f axis, float axisVarianceRad, float force, float forceVariance)
	{
		this.axis = new Vector3f(axis);
		this.axisVarianceRad = axisVarianceRad;
		this.force = force;
		this.forceVariance = forceVariance;
	}

	@Override
	public void init(Particle particle)
	{
		float variance = -1f + (float)(Math.random() * 2.0);
		float magnitude = force + (variance * forceVariance);
		
		Vector3f vel = particle.getVelocity();
		vel.set(varyUnitVector(axis, axisVarianceRad));
		
		vel.mul(magnitude);
	}

	@Override
	public void tick(Particle particle)
	{
	}
	
	private static Vector3f varyUnitVector(Vector3f coneDirection, float angle)
	{
		Vector3f result = new Vector3f(coneDirection);
		result.x = (result.x - angle) + ((float)Math.random() * angle * 2f);
		result.y = (result.y - angle) + ((float)Math.random() * angle * 2f);
		result.z = (result.z - angle) + ((float)Math.random() * angle * 2f);
		result.normalize();
		
		return result;
	}

	/*private static Vector3f varyUnitVector(Vector3f coneDirection, float angle)
	{
		float cosAngle = (float) Math.cos(angle);
		float theta = (float) (((float) Math.random()) * 2f * Math.PI);
		
		float z = cosAngle + (((float) Math.random()) * (1 - cosAngle));
		double rootOneMinusZSquared = Math.sqrt(1 - z * z);
		
		float x = (float) (rootOneMinusZSquared * Math.cos(theta));
		float y = (float) (rootOneMinusZSquared * Math.sin(theta));

		Vector4f direction = new Vector4f(x, y, z, 1f);
		if (coneDirection.x != 0 || coneDirection.y != 0 || (coneDirection.z != 1 && coneDirection.z != -1))
		{
			Vector3f rotateAxis = coneDirection.cross(Vectors.Z_AXIS);
			rotateAxis.normalize();
			float rotateAngle = (float) Math.acos(coneDirection.z);

			Quaternionf rotation = new Quaternionf();
			rotation.rotateAxis(-rotateAngle, rotateAxis);
			rotation.transform(direction);
		}
		else if (coneDirection.z == -1)
		{
			direction.z *= -1;
		}
		return new Vector3f(direction.x, direction.y, direction.z);
	}*/
}
