package manatee.client.gl.particle.mesh;

import manatee.client.gl.particle.ParticleSystem;
import manatee.client.gl.particle.attribs.IParticleAttrib;

public class MeshParticleSystem extends ParticleSystem
{
	private int partMeshIndex;
	
	public MeshParticleSystem(IParticleAttrib[] attribs, int partMeshIndex)
	{
		super(attribs);
		
		this.partMeshIndex = partMeshIndex;
	}
	
	public int getMeshIndex()
	{
		return partMeshIndex;
	}

	public void setMeshIndex(int partMeshIndex)
	{
		this.partMeshIndex = partMeshIndex;
	}
}
