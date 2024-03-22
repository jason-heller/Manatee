package manatee.client.inventory.item;

import com.google.gson.annotations.SerializedName;

public enum ItemQuality
{

	@SerializedName("STOCK")
	STOCK,

	@SerializedName("COMMON")
	COMMON,

	@SerializedName("UNCOMMON")
	UNCOMMON,

	@SerializedName("VINTAGE")
	VINTAGE,

	@SerializedName("UNUSUAL")
	UNUSUAL;
}
