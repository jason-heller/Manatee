package manatee.cache.definitions.loader;

import org.joml.Matrix4f;
import org.lwjgl.assimp.AIMatrix4x4;

public class LoaderUtil
{
	public static Matrix4f getAIMatrix(AIMatrix4x4 aiMatrix)
	{
		Matrix4f outMatrix = new Matrix4f();

		outMatrix.m00(aiMatrix.a1());
		outMatrix.m01(aiMatrix.a2());
		outMatrix.m02(aiMatrix.a3());
		outMatrix.m03(aiMatrix.a4());

		outMatrix.m10(aiMatrix.b1());
		outMatrix.m11(aiMatrix.b2());
		outMatrix.m12(aiMatrix.b3());
		outMatrix.m13(aiMatrix.b4());

		outMatrix.m20(aiMatrix.c1());
		outMatrix.m21(aiMatrix.c2());
		outMatrix.m22(aiMatrix.c3());
		outMatrix.m23(aiMatrix.c4());

		outMatrix.m30(aiMatrix.d1());
		outMatrix.m31(aiMatrix.d2());
		outMatrix.m32(aiMatrix.d3());
		outMatrix.m33(aiMatrix.d4());

		return outMatrix;
	}
}
