package lwjgui.loader;

import java.util.HashMap;
import java.util.Map;

public class StyleLoader
{
	
	public static Map<String, String> parse(String content)
	{
		Map<String, String> styles = new HashMap<>();
		
		final int len = content.length();
		
		StringBuilder read = new StringBuilder();
		String currentSelector = null;
		
		boolean inBrackets = false;
		boolean inQuotes = false;
		
		for(int pos = 0; pos < len; pos++)
		{
			char c = content.charAt(pos);
		
			if (!inQuotes && Character.isWhitespace(c))
				continue;
			
			if (inQuotes)
			{
				read.append(c);
			}
			else if (inBrackets)
			{
				if (c == '}')
				{
					styles.put(currentSelector, read.toString());

					inBrackets = false;
					read.setLength(0);
				}
				else
				{
					read.append(c);
				}
			}
			else {
				if (c == '{')
				{
					inBrackets = true;
					currentSelector = read.toString();
					
					read.setLength(0);
				}
				else
				{
					read.append(c);
				}
			}
		}
		
		return styles;
	}

}
