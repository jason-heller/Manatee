package manatee.client.dev;

import java.util.LinkedList;
import java.util.List;

import org.lwjgl.nanovg.NanoVG;

import lwjgui.paint.Color;
import manatee.maths.Maths;

public class ConsoleSuggestions
{
	private static final int MAX_HISTORY = 20;
	private List<String> suggestions = new LinkedList<>();
	private double y;
	private int selected = 0;
	private Color bgColor;
	
	private List<String> history = new LinkedList<>();

	public ConsoleSuggestions(Color bgColor)
	{
		this.bgColor = bgColor;
	}

	public void updatePosition(double y)
	{
		this.y = y;
	}

	public void clear()
	{
		selected = 0;
		suggestions.clear();
	}

	private void update(final List<String> suggestions)
	{
		clear();
		
		for (String suggestion : suggestions)
		{
			this.suggestions.add(suggestion);
		}
		
		selected = Math.min(suggestions.size() - 1, selected);
	}

	/*
	 * public void moveUp() { int index = Math.min(list.getSelectedIndex() - 1, 0);
	 * selectIndex(index); }
	 * 
	 * public void moveDown() { int index = Math.min(list.getSelectedIndex() + 1,
	 * list.getModel().getSize() - 1); selectIndex(index); }
	 */

	public void update(String subWord)
	{
		if (subWord == null || subWord.length() == 0)
		{
			clear();
			return;
		}
		
		List<String> suggestions = Command.getSuggestion(subWord);

		update(suggestions);
	}
	
	public void render(long ctx)
	{
		if (suggestions.size() == 0)
			return;

		float textWidth = 0;

		for(int i = 0; i < suggestions.size(); i++)
		{
			String suggestion = suggestions.get(i);

			textWidth = Math.max(textWidth, NanoVG.nvgTextBounds(ctx, 0, 0, suggestion, (float[]) null));
		}
		
		NanoVG.nvgBeginPath(ctx);
		NanoVG.nvgRect(ctx, 0, (int) y, 10f + textWidth, (float) suggestions.size() * 20f);
		NanoVG.nvgFillColor(ctx, bgColor.getNVG());
		NanoVG.nvgFill(ctx);
		
		NanoVG.nvgBeginPath(ctx);
		NanoVG.nvgRect(ctx, 0, (int) y + (selected * 20), 10f + textWidth, 20f);
		NanoVG.nvgFillColor(ctx, Color.BLUE.getNVG());
		NanoVG.nvgFill(ctx);
		
		NanoVG.nvgFillColor(ctx, Color.WHITE.getNVG());
		
		float textY = (float) (y + 15f);
		
		for(int i = 0; i < suggestions.size(); i++)
		{
			String suggestion = suggestions.get(i);
			NanoVG.nvgText(ctx, 5f, textY, suggestion);
			
			textWidth = Math.max(textWidth, suggestion.length());
			textY += 20;
		}
	}

	public boolean isVisible()
	{
		return !suggestions.isEmpty();
	}

	public String getSuggestion()
	{
		if (selected >= 0)
		{
			String s = suggestions.get(selected);
			return s.indexOf(' ') == -1 ? s : s.substring(0, s.indexOf(' ') + 1);
		}
		
		return history.get((-selected) - 1);
	}
	
	public void addHistory(String s)
	{
		history.add(s);
		
		if (history.size() > MAX_HISTORY)
		{
			history.remove(0);
		}
	}

	public void select(int i)
	{
		this.selected += i;
		
		selected = Maths.clamp(selected, -history.size(), Math.max(suggestions.size()-1, 0));
	}
	
	public int getSelectedId()
	{
		return selected;
	}
}
