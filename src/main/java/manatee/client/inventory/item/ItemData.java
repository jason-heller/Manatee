package manatee.client.inventory.item;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ItemData
{
	private String name;
	private String description;

	private ItemGraphic itemGraphic;
	private int graphicX;
	private int graphicY;
	
	private ItemQuality quality;
	
	private List<ItemComp> components;
	
	public static ItemData[] items;
	
	public ItemData(String name, String description, ItemGraphic itemGraphic, int graphicX, int graphicY,
			ItemQuality quality)
	{
		this.name = name;
		this.description = description;
		this.itemGraphic = itemGraphic;
		this.graphicX = graphicX;
		this.graphicY = graphicY;
		this.quality = quality;
	}
	
	public static void loadItems()
	{
		String json;
		
		try
		{
			json = Files.readString(Paths.get("src/main/resources/data/items.json"), StandardCharsets.UTF_8);
			
			Gson gson = new GsonBuilder().create();
			
			items = gson.fromJson(json, ItemData[].class);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public int getGraphicX()
	{
		return graphicX;
	}

	public void setGraphicX(int graphicX)
	{
		this.graphicX = graphicX;
	}

	public int getGraphicY()
	{
		return graphicY;
	}

	public void setGraphicY(int graphicY)
	{
		this.graphicY = graphicY;
	}

	public ItemQuality getQuality()
	{
		return quality;
	}

	public void setQuality(ItemQuality quality)
	{
		this.quality = quality;
	}

	public List<ItemComp> getComponents()
	{
		return components;
	}

	public void setComponents(List<ItemComp> components)
	{
		this.components = components;
	}

	public void setItemGraphic(ItemGraphic itemGraphic)
	{
		this.itemGraphic = itemGraphic;
	}

	public ItemGraphic getItemGraphic()
	{
		return itemGraphic;
	}
}
