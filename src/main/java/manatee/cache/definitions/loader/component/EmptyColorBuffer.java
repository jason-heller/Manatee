package manatee.cache.definitions.loader.component;

import org.joml.Vector3f;

import manatee.maths.MCache;

public class EmptyColorBuffer implements IColorBufferWrapper
{

	@Override
	public Vector3f get(int v)
	{
		return MCache.ONE;
	}

}
