package manatee.cache.definitions;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

public class MeshUtil
{
	private static final int SIZEOF_FLOAT = 4;

	static void enableAttribs(int attribCount)
	{
		for(int i = 0; i < attribCount; i++)
			glEnableVertexAttribArray(i);
	}
	
	static void disableAttribs(int attribCount)
	{
		for(int i = 0; i < attribCount; i++)
			glDisableVertexAttribArray(i);
	}

	public static void attribInterlacedf(int... attribLengths)
	{
		int offset = 0;
		int stride = 0;
		
		for(int i = 0; i < attribLengths.length; i++)
		{
			stride += attribLengths[i] * SIZEOF_FLOAT;
		}
		
		for(int i = 0; i < attribLengths.length; i++)
		{
			int length = attribLengths[i];
			
			glVertexAttribPointer(i, length, GL_FLOAT, false, stride, offset);
			
			offset += length * SIZEOF_FLOAT;
		}
	}
}
