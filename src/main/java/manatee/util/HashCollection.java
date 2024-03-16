package manatee.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HashCollection<S, T>
{
	private HashMap<S, T> map;
	private HashSet<T> set;
	
	public HashCollection()
	{
		set = new HashSet<>();
		map = new HashMap<>();
	}
	
	public void add(T t)
	{
		set.add(t);
	}
	
	public void put(S key, T value)
	{
		map.put(key, value);
	}
	
	public T get(S key)
	{
		return map.get(key);
	}
	
	public void clear()
	{
		set.clear();
		map.clear();
	}
	
	public Collection<T> values()
	{
		final List<T> values = new ArrayList<>(map.values().size() + set.size());
		values.addAll(map.values());
		values.addAll(set);
        
        return values;
	}
	
	public boolean containsKey(S key)
	{
		return map.containsKey(key);
	}

	public Set<S> keySet()
	{
		return map.keySet();
	}
}
