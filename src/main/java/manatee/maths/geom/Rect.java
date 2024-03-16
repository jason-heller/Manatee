package manatee.maths.geom;

public class Rect
{
	public float minX, maxX;
	public float minY, maxY;
	
	public void set(float minX, float minY, float maxX, float maxY) {
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
	}

	public  boolean isInside(double px, double py){
		if (px >= minX && px <= maxX){
			if (py >= minY && py <= maxY){
				return true;
			}
		}

		return false;
	}
}
