package lwjgui.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tag
{
	private Tag parent;

	private String name;
	
	private TagData data;

	private Map<String, String> attributes = new HashMap<>();

	private List<Tag> children = new ArrayList<>();
	
	public Tag(String name, TagData data)
	{
		setName(name);
		setData(data);
		setParent(null);
	}

	public Tag(Tag parent)
	{
		setParent(parent);
	}

	public void setParent(Tag parent)
	{
		this.parent = parent;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}

	public void setData(TagData data)
	{
		this.data = data;
	}

	public Tag getParent()
	{
		return parent;
	}

	public TagData getData()
	{
		return data;
	}

	public Map<String, String> getAttributes()
	{
		return attributes;
	}

	public List<Tag> getChildren()
	{
		return children;
	}
	
	public void print()
	{
		print(0);
	}

	private void print(int indentSize)
	{
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < indentSize; i++)
			sb.append("  ");
		
		sb.append(toString());
		
		System.out.println(sb.toString());
		
		for(Tag tag : children)
			tag.print(indentSize + 1);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<");
		sb.append(name);

		for (String attrib : attributes.keySet())
		{
			sb.append(" ");
			sb.append(attrib);
			sb.append("=");
			sb.append(attributes.get(attrib));
		}

		sb.append(children.size() == 0 ? " />" : ">");
		
		return sb.toString();
	}

	public String getName()
	{
		return name;
	}
}
