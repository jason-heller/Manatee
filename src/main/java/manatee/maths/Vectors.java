package manatee.maths;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Vectors
{
	public static final Quaternionf QUAT_EMPTY = new Quaternionf();
	public static Vector3f EMPTY = new Vector3f();
	public static Vector3f ONE = new Vector3f(1, 1, 1);
	public static Vector4f ONE4f = new Vector4f(1, 1, 1, 1);

	public static Vector3f X_AXIS = new Vector3f(1, 0, 0);
	public static Vector3f Y_AXIS = new Vector3f(0, 1, 0);
	public static Vector3f Z_AXIS = new Vector3f(0, 0, 1);
	
	public static Vector3f XY_AXIS = new Vector3f(1, 1, 0);
	
	public static Vector3f NEG_X_AXIS = new Vector3f(X_AXIS).negate();
	public static Vector3f NEG_Y_AXIS = new Vector3f(Y_AXIS).negate();
	public static Vector3f NEG_Z_AXIS = new Vector3f(Z_AXIS).negate();
	
	public static Quaternionf getRotationDifference(Vector3f from, Vector3f to) {
		if (from.equals(to))
			return new Quaternionf();
		
        // Normalize the vectors to ensure they're unit vectors
        from.normalize();
        to.normalize();

        // Calculate the axis of rotation using cross product
        Vector3f axis = new Vector3f();
        axis.cross(from, to);

        // Calculate the angle of rotation using dot product
        float angle = from.dot(to);

        // Create a quaternion representing the rotation
        Quaternionf rotation = new Quaternionf();
        rotation.fromAxisAngleRad(axis.x, axis.y, axis.z, angle);

        return rotation;
    }
	
	public static Quaternionf getInverseRotationDifference(Vector3f from, Vector3f to) {
        // Calculate the rotation difference in the opposite direction
        return Vectors.getRotationDifference(to, from).invert();
    }

}
