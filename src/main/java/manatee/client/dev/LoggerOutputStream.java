package manatee.client.dev;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import lwjgui.scene.control.TextInputControl;

// TODO: This is basically a listener for the normal System.out
public class LoggerOutputStream extends PrintStream
{
	static List<TextInputControl> listeners = new ArrayList<>();
	
	public static void create()
	{
		createOut();
		createErr();
	}
	
	public static LoggerOutputStream createOut()
	{
		LoggerOutputStream stream = new LoggerOutputStream(new DoubleOutputStream(System.out));
		System.setOut(stream);
		
		return stream;
	}

	public static LoggerOutputStream createErr()
	{
		LoggerOutputStream stream = new LoggerOutputStream(new DoubleOutputStream(System.err));
		System.setErr(stream);
		
		return stream;
	}

	LoggerOutputStream(OutputStream stream)
	{
		super(stream);
	}

	public static void addListener(TextInputControl node)
	{
		if (!listeners.contains(node))
			listeners.add(node);
	}
	
	public static void clear()
	{
		listeners.clear();
	}
}

class DoubleOutputStream extends OutputStream
{

	private PrintStream orig;

	public DoubleOutputStream(PrintStream orig)
	{
		this.orig = orig;
	}

	private StringBuilder string = new StringBuilder();
	
	@Override
	public void close() throws IOException
	{
		super.close();
		orig.close();
	}

	@Override
	public void write(int b) throws IOException
	{
		char c = (char) b;
		//DeveloperConsole.print(c);
		
		for(TextInputControl listener : LoggerOutputStream.listeners)
		{
			listener.appendText(""+c);
		}
		
		orig.write(b);
	}
}