package manatee.maths;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import manatee.maths.geom.AlignedBox;

public class MatrixMath
{
	/**
	 * Generates a square projection matrix based off the given parameters
	 *
	 * The matrix will look as such:
	 *
	 * | cot(fov/2)/a 0 0 0 | | 0 cot(fov/2) 0 0 | | 0 0 -(f+n)/(f-n) -2fn/(f-n) | |
	 * 0 0 -1 0 |
	 *
	 * @param fov         - field of view of camera
	 * @param aspectRatio - aspect ratio of the window (width / height)
	 * @param near        - near plane distance
	 * @param far         - far plane distance
	 * @return
	 */
	public static void setProjectionMatrix(Matrix4f matrix, float fov, float aspectRatio, float near, float far)
	{

		final float yScale = (float) (1f / Math.tan(Math.toRadians(fov / 2f)));
		final float xScale = yScale / aspectRatio;
		final float range = far - near;

		matrix.identity();

		matrix.m00(xScale);
		matrix.m11(yScale);
		matrix.m22(-((far + near) / range));
		matrix.m23(-1);
		matrix.m32(-((2 * far * near) / range));
		matrix.m33(0);
	}

	/**
	 * Returns the look (direction) vector of a matrix
	 *
	 * @param matrix The matrix to derive the look matrix from
	 * @return The look vector
	 */
	public static void setLookVector(Vector3f vec, Matrix4f matrix)
	{
		final Matrix4f inverse = new Matrix4f();
		matrix.invert(inverse);

		vec.set(inverse.m20(), inverse.m21(), inverse.m22()).negate();
	}

	public static void setViewMatrix(Matrix4f matrix, Vector3f position, float yaw, float pitch, float roll)
	{
		matrix.identity();

		matrix.rotateX(pitch);
		matrix.rotateY(roll);
		matrix.rotateZ(yaw);

		matrix.translate(-position.x, -position.y, -position.z);
	}

	public static Vector3f[] getFrustumVertices(Matrix4f projViewMatrix)
	{
		Matrix4f inverseMatrix = new Matrix4f(projViewMatrix).invert();
		Vector4f vTransform = new Vector4f();

		Vector3f[] vertices =
		{
				new Vector3f(-1, -1, -1), new Vector3f(1, -1, -1), new Vector3f(1, 1, -1), new Vector3f(-1, 1, -1),
				new Vector3f(-1, -1, 1), new Vector3f(1, -1, 1), new Vector3f(1, 1, 1),
				new Vector3f(-1, 1, 1)
		};

		for (Vector3f v : vertices)
		{
			vTransform.set(v.x, v.y, v.z, 1f);
			inverseMatrix.transform(vTransform);

			vTransform.div(vTransform.w);

			v.set(vTransform.x, vTransform.y, vTransform.z);
		}
		
		return vertices;
	}

	public static AlignedBox getFrustumAABB(Matrix4f projectionMatrix, Matrix4f viewMatrix)
	{
		Matrix4f projView = new Matrix4f(projectionMatrix);
		projView.mul(viewMatrix);
		return getFrustumAABB(projView);
	}
	
	public static AlignedBox getFrustumAABB(Matrix4f projectionViewMatrix)
	{
		Vector3f min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		Vector3f max = new Vector3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);

		Vector3f[] frustumVertices = getFrustumVertices(projectionViewMatrix);
		
		// Update min and max values based on frustum vertices
		for (Vector3f vertex : frustumVertices)
		{
			min.min(vertex);
			max.max(vertex);
		}

		// Construct and return the AABB
		Vector3f halfExtents = new Vector3f(max).sub(min).div(2f);
		Vector3f center = new Vector3f(min).add(halfExtents);
		
		return new AlignedBox(center, halfExtents);
	}
}
