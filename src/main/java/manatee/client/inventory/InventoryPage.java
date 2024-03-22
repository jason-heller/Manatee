package manatee.client.inventory;

import manatee.client.inventory.item.ItemData;

public class InventoryPage
{
	
	private InventorySlot[] slots;
	
	public InventoryPage(int size)
	{
		slots = new InventorySlot[size];
		
		for(int i = 0; i < size; i++)
			slots[i] = new InventorySlot();
	}
	
	public InventorySlot getSlot(int slotIndex)
	{
		return slots[slotIndex];
	}

	public void addItem(ItemData item)
	{
		for(int i = 0; i < slots.length; i++)
		{
			if (slots[i].getItem() == null)
			{
				slots[i].setItem(item);
				return;
			}
		}
	}
}
