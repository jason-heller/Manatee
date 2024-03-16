package manatee.util;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.system.MemoryUtil;

public class VecBuffer3
{
	private ByteBuffer buffer;
	
	public VecBuffer3(int numVectors)
	{
		buffer = MemoryUtil.memAlloc(4 * 3 * numVectors);
	}
	
	public void putf(float x, float y, float z)
	{
		buffer.putFloat(x);
		buffer.putFloat(y);
		buffer.putFloat(z);
	}
	
	public void putf(Vector3f vector)
	{
		buffer.putFloat(vector.x);
		buffer.putFloat(vector.y);
		buffer.putFloat(vector.z);
	}
	
	public void puti(int x, int y, int z)
	{
		buffer.putInt(x);
		buffer.putInt(y);
		buffer.putInt(z);
	}
	
	public void puti(Vector3i vector)
	{
		buffer.putInt(vector.x);
		buffer.putInt(vector.y);
		buffer.putInt(vector.z);
	}
	
	public void free()
	{
		MemoryUtil.memFree(buffer);
	}

	public FloatBuffer asFloatBuffer()
	{
		flip();
		return buffer.asFloatBuffer();
	}
	
	public void flip()
	{
		buffer.flip();
	}

	public IntBuffer asIntBuffer()
	{
		flip();
		return buffer.asIntBuffer();
	}
}
