package manatee.cache.definitions.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import manatee.cache.definitions.loader.exception.ShaderException;
import manatee.client.dev.Dev;

public class ShaderLoader
{
private static final Logger logger = (Logger) LoggerFactory.getLogger("IO");
	
	public static String loadShader(String path)
	{
		return loadShaderInternal(path).get("");
	}

	private static Map<String, String> loadShaderInternal(String path)
	{
		// logger.info("Loading shader: " + path);
		StringBuilder result = new StringBuilder();
		Map<String, String> blocks = new HashMap<>();
		blocks.put("", "");

		try (BufferedReader reader = ResourceLoader.getReader(path))
		{
			String line = "";
			while ((line = reader.readLine()) != null)
			{
				// Sassy GLSL..? 
				int ampPos = line.indexOf('@');
				if (ampPos != -1)
				{
					int spacePos = line.indexOf(' ', ampPos+1);
					String cmd = line.substring(ampPos+1, spacePos);
					
					Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
			        Matcher matcher = regex.matcher(line.substring(spacePos));
					
					String arg;
					
					if (!matcher.find())
					{
						throw new ShaderException("No args found for " + cmd);
					}

					if (matcher.group(1) != null)
					{
						// double-quote
						arg = matcher.group(1);
					}
					else if (matcher.group(2) != null)
					{
						// single-quote
						arg = matcher.group(2);
					}
					else
					{
						// unquoted
						arg = matcher.group();
					}

					switch(cmd)
					{
					case "import":
						Map<String, String> importBlocks = loadShaderInternal(arg);
						String core = importBlocks.remove("");
						result.append(core);
						
						for(Entry<String, String> entry : importBlocks.entrySet())
						{
							String key = entry.getKey();
							String src = "";
							
							if (blocks.get(key) != null)
								src = blocks.get(key);
							
							blocks.put(key, src + entry.getValue());
						}
						
						break;
					case "include":
						result.append(blocks.get(arg));
						break;
						
					case "mixin":
						blocks.put(arg, readBlock(reader, line, true));

						break;
					case "require":
						blocks.put(path, readBlock(reader, line, false));
						
						String src = "";
						
						if (blocks.get("require") != null)
							src = blocks.get("require") + ";";
						
						blocks.put("require", src + path);
						break;
					}
				}
				else {
					result.append(line).append("\n");
				}
			}
		}
		catch (IOException e)
		{
			logger.error("Couldn't find the file at " + path);
		}
		catch (NullPointerException e)
		{
			logger.error("Null pointer exception " + path);
			logger.error("Path: " + path);
		}

		blocks.put("", result.toString());
		//System.out.println("Final output of " + path);
		//System.err.println(blocks.get(""));
		
		String requirements = blocks.get("require");
		
		if(requirements != null)
		{
			String[] reqFiles = requirements.split(";");
			
			for(int i = 0; i < reqFiles.length; i ++)
			{
				if (reqFiles[i].equals(path))
					continue;
					
				String[] reqArray = blocks.get(reqFiles[i]).replaceAll("\t", "").split(";");
				for(String req : reqArray)
				{
					if (!blocks.get("").contains(req))
					{
						
						throw new ShaderException("Import requested \'" + req + "\' but was missing in parent file: " + path);
					}
				}
			}
		}
		
		return blocks;
	}

	private static String readBlock(BufferedReader reader, String line, boolean multiLine) throws IOException
	{
		StringBuilder block = new StringBuilder();
		int bracketNest = processMixinLine(block, line, 0, multiLine);
		while (bracketNest != 0)
		{
			line = reader.readLine();
			bracketNest = processMixinLine(block, line, bracketNest, multiLine);
		}
		
		return block.toString();
	}

	private static int processMixinLine(StringBuilder block, String line, int count, boolean multiLine)
	{
        for (char c : line.toCharArray()) {
        	if (c == '{')
        	{
                if (count != 0)
                	block.append(c+"");
                count++;
        	}
        	else if (c == '}')
        	{
                count--;
                if (count != 0)
                	block.append(c+"");
        	}
        	else if (count != 0)
        	{
        		block.append(c+"");
        	}
        }
        
        if (multiLine)
        	block.append("\n");
		return count;
	}
}
