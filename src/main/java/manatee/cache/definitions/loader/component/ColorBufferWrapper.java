package manatee.cache.definitions.loader.component;

import org.joml.Vector3f;
import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIColor4D.Buffer;

public class ColorBufferWrapper implements IColorBufferWrapper
{
	private Buffer buffer;

	public ColorBufferWrapper(Buffer buffer)
	{
		this.buffer = buffer;
	}

	@Override
	public Vector3f get(int v)
	{
		AIColor4D color = buffer.get(v);
		return new Vector3f(color.r(), color.g(), color.b());
	}

}
