package lwjgui.loader;

public class TagParseException extends RuntimeException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public TagParseException(String message, int line, int position)
	{
		super(message + " (line = " + line + ", pos = " + position + ")");
	}

}
