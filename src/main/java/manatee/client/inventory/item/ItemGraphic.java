package manatee.client.inventory.item;

import com.google.gson.annotations.SerializedName;

public enum ItemGraphic
{
	@SerializedName("PAGE0")
	PAGE0("texture/item/page0.png");

	private final String path;
	
	ItemGraphic(String path)
	{
		this.path = path;
	}

	public String getPath()
	{
		return path;
	}
}
