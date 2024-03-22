package manatee.client.gl.particle;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import manatee.cache.definitions.texture.ITexture;
import manatee.client.gl.camera.ICamera;
import manatee.client.gl.particle.attribs.IParticleAttrib;

public abstract class ParticleSystem
{
	protected List<Particle> particles = new ArrayList<>();
	
	protected IParticleAttrib[] attributes;
	
	protected static List<ParticleEmitter> emitters = new ArrayList<>();
	private List<ParticleAtlas> atlases = new ArrayList<>();
	
	private float particlesPerSecond;
	private int particlesPerEmission;
	private float emissionVariance;
	
	public ParticleSystem(IParticleAttrib[] attributes)
	{
		this.attributes = attributes;
	}
	
	public void update(ICamera camera)
	{
		for(ParticleEmitter emitter : emitters)
		{
			if (emitter.isEnabled())
				emitter.tick();
		}
		
		for(IParticleAttrib attrib : attributes)
		{
			for(Particle particle : particles)
				attrib.tick(particle);
		}
	}
	
	public void addParticle(Particle particle)
	{
		particles.add(particle);
		
		for(IParticleAttrib attrib : attributes)
		{
			attrib.init(particle);
		}
	}

	public List<Particle> getParticles()
	{
		return particles;
	}
	
	public void addEmitter(Vector3f position, boolean enabled)
	{
		
		ParticleEmitter emitter = new ParticleEmitter(position, particlesPerSecond, emissionVariance, particlesPerEmission, enabled);
		emitters.add(emitter);
		
		emitter.setParticleSystem(this);
	}
	
	public void addEmitter(ParticleEmitter emitter)
	{
		emitters.add(emitter);
		
		emitter.setParticleSystem(this);
	}

	public void addAtlas(ITexture texture, int widthInFrames, boolean additiveBlending, boolean lit)
	{
		atlases.add(new ParticleAtlas(texture, widthInFrames, additiveBlending, lit));
	}

	public void clearEmitters()
	{
		emitters.clear();
	}

	public void clearParticles()
	{
		particles.clear();
	}

	public IParticleAttrib[] getAttributes()
	{
		return attributes;
	}

	public void setAttributes(IParticleAttrib[] attributes)
	{
		this.attributes = attributes;
	}

	public void setParticlesPerSecond(float particlesPerSecond)
	{
		this.particlesPerSecond = particlesPerSecond;
		
		for(ParticleEmitter emitter : emitters)
		{
			emitter.setParticlesPerSecond(particlesPerSecond, emissionVariance);
		}
		
	}

	public void setParticlesPerEmission(int particlesPerEmission)
	{
		this.particlesPerEmission = particlesPerEmission;
		
		for(ParticleEmitter emitter : emitters)
		{
			emitter.setParticlesPerEmission(particlesPerEmission);
		}
	}

	public void setEmissionVariance(float emissionVariance)
	{
		this.emissionVariance = emissionVariance;
		
		for(ParticleEmitter emitter : emitters)
		{
			emitter.setParticlesPerSecond(particlesPerSecond, emissionVariance);
		}
	}
}
