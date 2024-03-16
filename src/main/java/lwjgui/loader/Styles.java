package lwjgui.loader;

import java.util.Map;
import java.util.Set;

public class Styles
{
	private Map<String, String> styles;

	public Styles()
	{
		styles = null;
	}
	
	public Styles(Map<String, String> styles)
	{
		setStyles(styles);
	}

	public void setStyles(Map<String, String> styles)
	{
		this.styles = styles;
	}
	
	public void addStyles(Map<String, String> appendStyles)
	{
		if (this.styles == null)
		{
			setStyles(appendStyles);
			return;
		}
		
		for(String key : appendStyles.keySet())
		{
			this.styles.put(key, appendStyles.get(key));
		}
	}

	public Set<String> getSelectors()
	{
		return styles.keySet();
	}
	
	public String get(String selector)
	{
		String out = styles.get(selector);
		return out == null ? "" : out;
	}

	public boolean contains(String key)
	{
		return styles.containsKey(key);
	}
}
