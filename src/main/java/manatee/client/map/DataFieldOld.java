package manatee.client.map;

import org.joml.Vector2i;

public abstract class DataFieldOld
{
	protected Vector2i position;

	protected int strideX, strideY;
	protected int resolution;
	
	public DataFieldOld(Vector2i position, int strideX, int strideY, int resolution)
	{
		this.position = position;
		this.strideX = strideX;
		this.strideY = strideY;
		this.resolution = resolution;
	}
	
	public int getResolution()
	{
		return resolution;
	}

	public void setResolution(int resolution)
	{
		this.resolution = resolution;
	}
	
	public void setPosition(int x, int y)
	{
		position.set(x, y);
	}
	
	public void setPosition(Vector2i position)
	{
		this.position.set(position);
	}
	
	public Vector2i getPosition()
	{
		return position;
	}
	
	public int getStrideX()
	{
		return strideX;
	}

	public void setStrideX(int strideX)
	{
		this.strideX = strideX;
	}

	public int getStrideY()
	{
		return strideY;
	}

	public void setStrideY(int strideY)
	{
		this.strideY = strideY;
	}

	public int getFieldWidth()
	{
		return resolution * strideX;
	}
	
	public int getFieldHeight()
	{
		return resolution * strideY;
	}
}
