package manatee.maths;

public class Maths
{
	public static final float TWOPI = (float) (Math.PI * 2.0);
	public static final float HALFPI = (float) (Math.PI / 2f);
	public static final float PI = (float)Math.PI;
	
	public static double clamp(double value, double min, double max)
	{
		return value > max ? max : (value < min ? min : value);
	}
	
	public static float clamp(float value, float min, float max)
	{
		return value > max ? max : (value < min ? min : value);
	}
	
	public static int clamp(int value, int min, int max)
	{
		return value > max ? max : (value < min ? min : value);
	}
	
	public static float cos(float value)
	{
		return (float)Math.cos(value);
	}
	
	public static float sin(float value)
	{
		return (float)Math.sin(value);
	}
	
	public static int mod(int x, int m)
	{
		return (x % m + m) % m;
	}
	
	public static int floor(float x) {
		int xi = (int) x;
		return x < xi ? xi - 1 : xi;
	}

	public static float lerp(float s, float t, float amount) {
		return s * (1f - amount) + t * amount;
	}
	
	public static float lerpAngle(float s, float t, float amount) {
		float delta = ((t - s + 9.42478f) % Maths.TWOPI) - Maths.PI;
        return (s + delta * amount + Maths.TWOPI) % Maths.TWOPI;
	}
}
