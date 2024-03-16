package manatee.cache.definitions.field;

import org.joml.Vector2i;

public class DataFieldi implements DataField<Integer>
{
	protected Vector2i position;

	protected int xResolution, yResolution;
	protected int spacing;
	
	protected int[][] data;
	
	public DataFieldi(Vector2i position, int xResolution, int yResolution, int spacing)
	{
		this.position = position;
		this.xResolution = xResolution;
		this.yResolution = yResolution;
		this.spacing = spacing;
		
		this.data = new int[xResolution][yResolution];
	}
	
	public Integer get(int x, int y)
	{
		return getLocal((int)((x - position.x) / spacing), (int)((y - position.y) / spacing));
	}
	
	public Integer getLocal(int x, int y)
	{
		return data[x][y];
	}

	public void set(Integer value, int x, int y)
	{
		setLocal(value, (int)((x - position.x) / spacing), (int)((y - position.y) / spacing));
	}
	
	public void setLocal(Integer value, int x, int y)
	{
		data[x][y] = value;
	}
	
	public void add(Integer value, int x, int y)
	{
		addLocal(value, (int)((x - position.x) / spacing), (int)((y - position.y) / spacing));
	}

	public int[][] get()
	{
		return data;
	}

	public void set(int[][] data)
	{
		//assert data.length == xResolution : "New data must retain same X resolution";
		//assert data[0].length == yResolution : "New data must retain same Y resolution";
		
		this.data = data;
	}
	
	public void addLocal(Integer value, int x, int y)
	{
		data[x][y] += value;
	}
}
