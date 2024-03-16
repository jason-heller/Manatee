package manatee.client.gl.particle.attribs;

import org.joml.Vector3f;

import manatee.client.Client;
import manatee.client.gl.particle.Particle;
import manatee.client.scene.MapScene;
import manatee.client.scene.WindHandler;

public class WindParticleAttrib implements IParticleAttrib
{
	private WindHandler wind;
	public float affect;
	
	public WindParticleAttrib(float affect)
	{
		this.affect = affect;
		this.wind = ((MapScene)Client.scene()).getMap().getWind();
	}

	@Override
	public void init(Particle particle)
	{
	}

	@Override
	public void tick(Particle particle)
	{
		Vector3f breeze = wind.getWindVector();
		breeze.mul(wind.getWindStrength());
		breeze.mul(affect);
		
		particle.getPosition().add(breeze);
	}

}
