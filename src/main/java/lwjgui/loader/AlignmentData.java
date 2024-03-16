package lwjgui.loader;

public class AlignmentData
{
	private PaneAlignment paneAlignment = PaneAlignment.NONE;
	
	private final int x, y;
	
	private int loopX, loopY;
	
	public AlignmentData(PaneAlignment paneAlignment, int x, int y, int loopX, int loopY)
	{
		this.paneAlignment = paneAlignment;
		this.x = x;
		this.y = y;
		this.loopX = loopX;
		this.loopY = loopY;
	}

	public PaneAlignment getPaneAlignment()
	{
		return paneAlignment;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public int getLoopX()
	{
		return loopX;
	}

	public int getLoopY()
	{
		return loopY;
	}
	
	
}
