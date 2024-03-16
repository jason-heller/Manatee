package manatee.client.gl.mesh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import manatee.cache.definitions.texture.ITexture;

public class TileVertexMap
{
	private Map<TileShaderTarget, Map<ITexture, ArrayList<Float>>> map = new HashMap<>();
	
	private int nKeys;

	public boolean containsKey(TileShaderTarget u, ITexture v)
	{
		if (!map.containsKey(u))
			return false;
		
		if (!map.get(u).containsKey(v))
			return false;
		
		return true;
	}

	public ArrayList<Float> get(TileShaderTarget u, ITexture v)
	{
		return map.get(u).get(v);
	}

	public void put(TileShaderTarget u, ITexture v, ArrayList<Float> value)
	{
		map.get(u).put(v, value);
	}

	public Set<TileShaderTarget> primaryKeySet()
	{
		return map.keySet();
	}
	
	public Set<ITexture> secondaryKeySet(TileShaderTarget primaryKey)
	{
		return map.get(primaryKey).keySet();
	}

	public int keyCount()
	{
		return nKeys;
	}

	public void initIfEmpty(TileShaderTarget u, ITexture v)
	{
		Map<ITexture, ArrayList<Float>> innerMap;
		
		if (!map.containsKey(u))
		{
			innerMap = new HashMap<>();
			map.put(u, innerMap);
			
			innerMap.put(v, new ArrayList<>());
			nKeys++;
		}
		else
		{
			innerMap = map.get(u);
			
			if (!innerMap.containsKey(v))
			{
				innerMap.put(v, new ArrayList<>());
				nKeys++;
			}
		}
	}
	
	
}
