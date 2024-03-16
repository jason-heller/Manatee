package manatee.cache.definitions.field;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import manatee.maths.Maths;
import manatee.maths.geom.Triangle;

public class DataFieldf implements DataField<Float>
{
	protected Vector2i position;

	protected int xResolution, yResolution;
	protected int spacing;
	
	protected float[][] data;
	
	public DataFieldf(Vector2i position, int xResolution, int yResolution, int spacing)
	{
		this.position = position;
		this.xResolution = xResolution;
		this.yResolution = yResolution;
		this.spacing = spacing;
		
		this.data = new float[xResolution][yResolution];
	}
	
	public Float get(int x, int y)
	{
		return getLocal((int)((x - position.x) / spacing), (int)((y - position.y) / spacing));
	}
	
	public Float getLocal(int x, int y)
	{
		return data[x][y];
	}

	public void set(Float value, int x, int y)
	{
		setLocal(value, (int)((x - position.x) / spacing), (int)((y - position.y) / spacing));
	}
	
	public void setLocal(Float value, int x, int y)
	{
		data[x][y] = value;
	}
	
	public void add(Float value, int x, int y)
	{
		addLocal(value, (int)((x - position.x) / spacing), (int)((y - position.y) / spacing));
	}

	public float[][] get()
	{
		return data;
	}

	public void set(float[][] data)
	{
		this.data = data;
	}
	
	public void addLocal(Float value, int x, int y)
	{
		data[x][y] += value;
	}

	/** Samples the data at a given position by interpolating adjacent data
	 * Requires that the data type can be cast to float
	 * @param x
	 * @param y
	 * @return
	 */
	public float getInterpolated(float x, float y)
	{
		int floorX = Math.floorDiv(Maths.floor(x), spacing) * spacing;
		int floorY = Math.floorDiv(Maths.floor(y), spacing) * spacing;
		
		int localX = floorX - position.x;
		int localY = floorY - position.y;
		
		int arrX = (localX / spacing);
		int arrY = (localY / spacing);
		
		arrX = (arrX >= data.length - 2) ? data.length - 2 : arrX;
		arrY = (arrY >= data.length - 2) ? data.length - 2 : arrY;

		float triX = x - floorX;
		float triY = y - floorY;
		
		if (arrX < 0 || arrY < 0)
			return 0f;
		
		Vector2f pos = new Vector2f(triX, triY);
		float w = data[arrX][arrY + 1];

		if (triX <= (1f - triY))
		{
			float u = data[arrX][arrY];
			float v = data[arrX + 1][arrY];
			
			return Triangle.barycentric(new Vector3f(0, u, 0),
					new Vector3f(1, v, 0),
					new Vector3f(0, w, 1),
					pos);
		}
		
		float u = data[arrX + 1][arrY];
		float v = data[arrX + 1][arrY + 1];
		
		return Triangle.barycentric(new Vector3f(1, u, 0),
				new Vector3f(1, v, 1),
				new Vector3f(0, w, 1),
				pos);
	}
}
