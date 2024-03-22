package manatee.client;

import manatee.client.inventory.Inventory;
import manatee.client.inventory.item.ItemData;

public class ClientProfile
{
	private Inventory inventory = new Inventory();
	
	public ClientProfile()
	{
		inventory.addItem(ItemData.items[0]);
	}
	
	public Inventory getInventory()
	{
		return inventory;
	}
}
