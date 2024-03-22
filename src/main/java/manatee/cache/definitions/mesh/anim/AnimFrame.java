package manatee.cache.definitions.mesh.anim;

import org.joml.Matrix4f;

public class AnimFrame
{
	public Matrix4f[] boneMatrices;

	public AnimFrame(Matrix4f[] boneMatrices)
	{
		this.boneMatrices = boneMatrices;
	}

}
