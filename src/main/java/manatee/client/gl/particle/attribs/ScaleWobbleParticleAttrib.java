package manatee.client.gl.particle.attribs;

import manatee.client.Time;
import manatee.client.gl.particle.Particle;

public class ScaleWobbleParticleAttrib implements IParticleAttrib
{
	public float speed, time;
	
	public float amplitude;
	public float startScale;
	
	public ScaleWobbleParticleAttrib(float startScale, float speed, float amplitude)
	{
		this.startScale = startScale;
		this.speed = speed;
		this.amplitude = amplitude;
	}
	
	@Override
	public void init(Particle particle)
	{
		particle.getScale().set(startScale);
	}

	@Override
	public void tick(Particle particle)
	{
		float scaleXY = startScale + ((float)Math.sin(time) * amplitude);
		float scaleZ = startScale + ((float)Math.cos(time) * amplitude);
		
		time += speed * Time.deltaTime;
		
		particle.getScale().set(scaleXY, scaleXY, scaleZ);
	}

}
