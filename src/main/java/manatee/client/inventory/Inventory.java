package manatee.client.inventory;

import java.util.LinkedList;

import manatee.client.inventory.item.ItemData;

public class Inventory
{
	private LinkedList<InventoryPage> pages = new LinkedList<>();
	
	public Inventory()
	{
		pages.add(new InventoryPage(18));	// Page 0 is the hotbar, equipment
	}

	public InventoryPage getPage(int pageNum)
	{
		return pages.get(pageNum);
	}

	public void addItem(ItemData item)
	{
		pages.get(0).addItem(item);
	}
}
