package manatee.client.map.light;

import org.joml.Vector3f;

public class LightUtil
{
	public static float getAttenuRadius(Vector3f attenu)
	{
		double discriminant = (attenu.y * attenu.y) - (4 * attenu.z * attenu.x);

		if (discriminant >= 0)
		{
			double sqrtDiscriminant = Math.sqrt(discriminant);

			// Calculate the solutions
			double dist1 = (-attenu.y + sqrtDiscriminant) / (2 * attenu.z);
			double dist2 = (-attenu.y - sqrtDiscriminant) / (2 * attenu.z);

			return (float) Math.max(Math.abs(dist1), Math.abs(dist2));
		}

		return 0f;
	}
}
