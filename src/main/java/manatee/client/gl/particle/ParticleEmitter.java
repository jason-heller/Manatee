package manatee.client.gl.particle;

import org.joml.Vector3f;

import manatee.client.dev.Dev;

public class ParticleEmitter
{
	protected long msPerEmission;
	protected long emissionVariance;
	
	private long lastEmission;
	private long nextEmission;
	
	private boolean enabled;
	
	protected Vector3f origin;
	
	private int particlesPerEmission = 1;
	
	private ParticleSystem particleSystem;
	
	public ParticleEmitter(Vector3f origin, float pps, float ppsVariance, int ppe, boolean enabled)
	{
		this.particlesPerEmission = ppe;
		setParticlesPerSecond(pps, ppsVariance);
		
		this.origin = origin;
		
		setEnabled(enabled);
	}
	
	public void tick()
	{
		long time = System.currentTimeMillis() - lastEmission;
		
		if (time >= nextEmission)
		{
		
			for(int i = 0; i < particlesPerEmission; i++)
			{
				particleSystem.addParticle(new Particle(origin));
			}
			
			determineNextEmission();
		}
	}

	public Vector3f getPosition()
	{
		return origin;
	}
	
	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
		
		if (enabled)
			determineNextEmission();
	}

	private void determineNextEmission()
	{
		double rng = (Math.random() * 2.0) - 0.5;
		long variance = (long) (emissionVariance * rng);
		
		nextEmission = System.currentTimeMillis() + (msPerEmission + variance);
	}
	
	public void setParticleSystem(ParticleSystem particleSyste)
	{
		this.particleSystem = particleSyste;
	}

	public long getEmissionVariance()
	{
		return emissionVariance;
	}

	public void setEmissionVariance(float varianceFactor)
	{
		this.emissionVariance = (long) (msPerEmission * emissionVariance);
	}

	public void setParticlesPerSecond(float pps, float ppsVariance)
	{
		this.msPerEmission = (long) (1000f / pps);
		this.emissionVariance = (long) (pps * ppsVariance);
	}

	public void setParticlesPerEmission(int ppe)
	{
		this.particlesPerEmission = ppe;
	}
}
