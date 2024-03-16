package manatee.cache.definitions.loader.component;

import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.AIVector3D.Buffer;

public class EmptyAIBuffer3D extends Buffer
{
	public static final EmptyAIBuffer3D INSTANCE = new EmptyAIBuffer3D();
	
	private final AIVector3D EMPTY = new EmptyAIVector3D();

	private EmptyAIBuffer3D()
	{
		super(null);
	}

	@Override
	public AIVector3D get(int index)
	{
		return EMPTY;
	}

	class EmptyAIVector3D extends AIVector3D
	{

		public EmptyAIVector3D()
		{
			super(null);
		}

		@Override
		public float x() {
			return 0f;
		}

		@Override
		public float y() {
			return 0f;
		}
		
		@Override
		public float z() {
			return 0f;
		}
	}

	class EmptyAIColor4D extends AIColor4D
	{

		public EmptyAIColor4D()
		{
			super(null);
		}

		@Override
		public float r() {
			return 0f;
		}

		@Override
		public float g() {
			return 0f;
		}
		
		@Override
		public float b() {
			return 0f;
		}
		
		@Override
		public float a() {
			return 0f;
		}
	}

}