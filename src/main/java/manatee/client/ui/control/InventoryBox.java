package manatee.client.ui.control;

import static org.lwjgl.system.MemoryStack.stackPush;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.system.MemoryStack;

import lwjgui.geometry.Insets;
import lwjgui.glfw.input.MouseHandler;
import lwjgui.paint.Color;
import lwjgui.scene.Context;
import lwjgui.scene.control.Control;
import lwjgui.scene.image.Image;
import manatee.client.Client;
import manatee.client.Time;
import manatee.client.input.Input;
import manatee.client.input.Keybinds;
import manatee.client.inventory.InventoryPage;
import manatee.client.inventory.InventorySlot;
import manatee.client.inventory.item.ItemData;
import manatee.client.inventory.item.ItemGraphic;

public class InventoryBox extends Control
{
	private int rows, columns;
	
	private float boxSize = 64;
	
	private float cornerRadius = 12f;
	
	private int alpha = 125;
	
	private int hoveredIndex = -1;
	
	private InventoryPage page;
	
	private InventorySlot draggedSlot = null;
	
	private static final float ITEM_IMG_SIZE = 32;
	private static final float MAX_GROWTH = 4f;
	private static final float GROW_SPEED = 40f;
	
	private Color fillColor = new Color(128, 128, 128, alpha);
	private Color borderColor = new Color(255, 255, 255, alpha);
	private Color selectedColor = new Color(255, 255, 0, alpha);
	
	private float[] growths;
	
	private static Map<ItemGraphic, Image> imageCache = new HashMap<>();
	
	public InventoryBox()
	{
		rows = 1;
		columns = 1;
		this.setPadding(new Insets(5));
		resizeImages();
	}
	
	public int getRows()
	{
		return rows;
	}

	public void setRows(int rows)
	{
		this.rows = rows;
		resizeImages();
	}

	public int getColumns()
	{
		return columns;
	}

	public void setColumns(int columns)
	{
		this.columns = columns;
		resizeImages();
	}

	public void setPage(int pageNum)
	{
		this.page = Client.profile().getInventory().getPage(pageNum);
	}

	private void resizeImages()
	{
		float paddingW = (float) this.getPadding().getWidth();
		float paddingH = (float) this.getPadding().getWidth();
		
		float w = columns * (boxSize + paddingW);
		float h = rows * (boxSize + paddingH);
		
		growths = new float[rows * columns];
		
		w -= paddingW;
		h -= paddingH;
		
		this.setPrefSize(w, h);
	}

	public InventoryBox(int rows, int columns)
	{
		this.rows = rows;
		this.columns = columns;
	}
	
	@Override
	public String getElementType()
	{
		return "itembox";
	}
	
	@Override
	public void render(Context context)
	{
		if ( !isVisible() || page == null)
			return;
		
		if ( context == null )
			return;
		
		float paddingW = (float) this.getPadding().getWidth();
		float paddingH = (float) this.getPadding().getWidth();
		
		MouseHandler mh = Client.ui().getWindow().getMouseHandler();
		float xMouse = mh.getX();
		float yMouse = mh.getY();
		
		long vg = context.getNVG();

	
		int index = -1;
		
		boolean selectPressed = Input.isPressed(Keybinds.SELECT);
		boolean selectHeld = Input.isHeld(Keybinds.SELECT);
		
		// Tick
		hoveredIndex = -1;
		
		for(int x = 0; x < columns; x++)
		{
			for(int y = 0; y < rows; y++)
			{
				index++;
				InventorySlot slot = page.getSlot(index);
				
				float xBox = (float) (this.getX() + (x * (boxSize + paddingW)));
				float yBox = (float) (this.getY() + (y * (boxSize + paddingH)));
				
				if (xMouse >= xBox && yMouse >= yBox && xMouse < xBox + boxSize && yMouse < yBox + boxSize)
				{
					growths[index] = Math.min(growths[index] + (Time.deltaTime * GROW_SPEED), MAX_GROWTH);
					hoveredIndex = index;
					
					if (selectPressed && slot.getItem() != null)
					{
						slot.setSelected(true);
					}
					
					if (!selectHeld && draggedSlot != null)
					{
						// Swap slots
						ItemData slotItem = slot.getItem();
						int slotStackSize = slot.getStackSize();
						
						slot.setItem(draggedSlot.getItem());
						slot.setStackSize(draggedSlot.getStackSize());
						slot.setSelected(true);
						
						draggedSlot.setItem(slotItem);
						draggedSlot.setStackSize(slotStackSize);
						draggedSlot.setSelected(false);
						
						draggedSlot = null;
					}
				}
				else
				{
					growths[index] = Math.max(growths[index] - (Time.deltaTime * GROW_SPEED), 0f);
					
					if (selectPressed)
					{
						slot.setSelected(false);
					}
					
					if (slot.isSelected() && !slot.isMoved() && selectHeld)
					{
						draggedSlot = slot;
						slot.setMoved(true);
					}
					
					if (slot.isMoved() && !selectHeld)
					{
						slot.setMoved(false);
					}
				}
			}
		}
		
		// Render
		index = -1;
		for(int x = 0; x < columns; x++)
		{
			for(int y = 0; y < rows; y++)
			{
				index++;
				InventorySlot slot = page.getSlot(index);
				
				float g = growths[index];
				float g2 = growths[index] * 2f;
				
				float xBox = (float) (this.getX() + (x * (boxSize + paddingW)));
				float yBox = (float) (this.getY() + (y * (boxSize + paddingH)));
				
				// Draw Box
				NanoVG.nvgBeginPath(vg);
				NanoVG.nvgRoundedRect(vg, xBox - g, yBox - g, boxSize + g2, boxSize + g2, cornerRadius);
				NanoVG.nvgFillColor(vg, fillColor.getNVG());
				NanoVG.nvgFill(vg);
				NanoVG.nvgClosePath(vg);
				
				NanoVG.nvgBeginPath(vg);
				NanoVG.nvgRoundedRect(vg, xBox - g, yBox - g, boxSize + g2, boxSize + g2, cornerRadius);
				NanoVG.nvgStrokeColor(vg, (slot.isSelected() ? selectedColor : borderColor).getNVG());
				NanoVG.nvgStrokeWidth(vg, 2f);
				NanoVG.nvgStroke(vg);
				NanoVG.nvgClosePath(vg);

				ItemData item = slot.getItem();
				
				// Draw Item
				if (item == null)
					continue;
				
				Image image = null;
				ItemGraphic itemGraphic = ItemGraphic.PAGE0;//item.getItemGraphic();
				
				if (imageCache.containsKey(itemGraphic))
				{
					image = imageCache.get(itemGraphic);
				}
				else
				{
					image = new Image(itemGraphic.getPath());
					imageCache.put(item.getItemGraphic(), image);
				}
				
				float xItem = xBox + 8f - g;
				float yItem = yBox + 8f - g;
				float itemSize = (boxSize - 16f) + g2;
				float patternStretch = 1f + (g2 / (boxSize - 16f));
				
				float itemAlpha = slot.isMoved() ? 0f : 1f;
				
				try (MemoryStack stack = stackPush())
				{
					@SuppressWarnings("deprecation")
					NVGPaint imagePaint = NanoVG.nvgImagePattern(vg,
							-(item.getGraphicX() * ITEM_IMG_SIZE) + xItem, -(item.getGraphicY() * ITEM_IMG_SIZE) + yItem,
							image.getWidth() * patternStretch, image.getHeight() * patternStretch,
							0, image.getImage(), itemAlpha, NVGPaint.callocStack(stack));
					NanoVG.nvgBeginPath(vg);
					NanoVG.nvgRect(vg, xItem, yItem, itemSize, itemSize);
					NanoVG.nvgFillPaint(vg, imagePaint);
					NanoVG.nvgFill(vg);
					NanoVG.nvgClosePath(vg);
				}
			}
		}
		
		if (hoveredIndex != -1)
		{
			ItemData item = page.getSlot(hoveredIndex).getItem();
			
			if (Input.isHeld(Keybinds.SELECT))
			{
				
			}
			
			if (item != null)
			{
				String itemName = item.getName();
				
				NanoVG.nvgFillColor(vg, Color.BLACK.getNVG());
				NanoVG.nvgText(vg, xMouse + 1, yMouse - 15, itemName);
				NanoVG.nvgFillColor(vg, Color.WHITE.getNVG());
				NanoVG.nvgText(vg, xMouse, yMouse - 16, itemName);
			}
		}
	}
}
