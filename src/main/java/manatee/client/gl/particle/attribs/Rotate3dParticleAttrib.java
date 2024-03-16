package manatee.client.gl.particle.attribs;

import org.joml.Random;

import manatee.client.Time;
import manatee.client.gl.particle.Particle;

public class Rotate3dParticleAttrib implements IParticleAttrib
{
	public float rotationSpeed;

	public Rotate3dParticleAttrib(float rotationSpeed)
	{
		this.rotationSpeed = rotationSpeed;
	}

	@Override
	public void init(Particle particle)
	{
		Random r = new Random();
    
		float x = -1f + (r.nextFloat() * 2f);
		float y = -1f + (r.nextFloat() * 2f);
		float z = -1f + (r.nextFloat() * 2f);
        
        particle.getRotation().set(r.nextFloat(), x, y, z).normalize();
	}

	@Override
	public void tick(Particle particle)
	{
		particle.getRotation().angle += rotationSpeed * Time.deltaTime;
	}

}
