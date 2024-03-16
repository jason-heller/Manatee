package lwjgui.loader;

public enum PaneAlignment
{
	NONE, TOP, BOTTOM, LEFT, RIGHT, CENTER, GRID;

	static PaneAlignment getAlignment(String paneAlignment)
	{
		if (paneAlignment == null)
			return NONE;
		
		try
		{
			return PaneAlignment.valueOf(paneAlignment.toUpperCase());
		}
		catch (IllegalArgumentException e)
		{
			return NONE;
		}
	}
}
