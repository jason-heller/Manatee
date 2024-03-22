package manatee.client.inventory;

import manatee.client.inventory.item.ItemData;

public class InventorySlot
{
	private int stackSize;
	private ItemData item;
	
	private boolean selected, moved;
	
	public InventorySlot()
	{
		this(0, null);
	}
	
	public InventorySlot(int stackSize, ItemData item)
	{
		this.stackSize = stackSize;
		this.item = item;
	}
	
	public int getStackSize()
	{
		return stackSize;
	}
	public void setStackSize(int stackSize)
	{
		this.stackSize = stackSize;
	}
	public ItemData getItem()
	{
		return item;
	}
	public void setItem(ItemData item)
	{
		this.item = item;
	}

	public boolean isSelected()
	{
		return selected;
	}

	public boolean isMoved()
	{
		return moved;
	}

	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}
	
	public void setMoved(boolean moved)
	{
		this.moved = moved;
	}
}
