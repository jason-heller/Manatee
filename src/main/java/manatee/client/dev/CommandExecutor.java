package manatee.client.dev;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CommandExecutor {
	
	private Object object;
	private Method[] methods;
	//private Runnable runnable;
	private CommandArgumentType argumentType;
	
	private final String syntax;
	
	private boolean requireCheats;

	public CommandExecutor(String syntax, Object object, Method[] consumer, boolean requireCheats) {
		this.syntax = syntax;
		this.object = object;
		this.methods = consumer;
		this.argumentType = CommandArgumentType.BOOL_OR_NONE;
		this.requireCheats = requireCheats;
	}
	
	public void process(List<String> readArgs) throws Exception {
		
		for(Method method : methods)
		{
			int numArgs = method.getParameterCount();
			
			if (numArgs != readArgs.size())
				continue;
			
			Class<?>[] types = method.getParameterTypes();
			
			Object[] args = new Object[numArgs];
			
			for(int i = 0; i < numArgs; i++)
			{
				String arg = readArgs.get(i);
				
				Class<?> type = types[i];
				
				try {
					args[i] = castArgument(type, arg);
				}
				catch(NumberFormatException | ClassCastException e)
				{
					// Fail
					args = null;
					e.printStackTrace();
					break;
				}
			}
			
			if (args == null)
				continue;
			
			try {
				method.invoke(object, args);
			}
			catch (Exception e)
			{
				System.err.println("Failed to invoke " + method.getName());
				e.printStackTrace();
			}
			
			return;
		}
		
		System.err.println("No suitable syntaxes for " + methods[0].getName());
	}
	
	private Object castArgument(Class<?> type, String arg) throws NumberFormatException
	{
		switch(type.getSimpleName())
		{
		case "boolean":
		{
			return arg.toLowerCase().equals("true") || arg.equals("1") || arg.equals("");
		}
		case "byte":
		{
			return Byte.parseByte(arg);
		}
		case "char":
		{
			if (arg.length() == 1)
				return arg.charAt(0);
			
			throw new ClassCastException("Cannot cast " + arg + " to char"); 
		}
		case "short":
		{
			return Short.parseShort(arg);
		}
		case "int":
		{
			return Integer.parseInt(arg);
		}
		case "long":
		{
			return Long.parseLong(arg);
		}
		case "float":
		{
			return Float.parseFloat(arg);
		}
		case "double":
		{
			return Double.parseDouble(arg);
		}
		case "String":
		{
			return arg;
		}
		default:
			throw new ClassCastException("Unsupported command cast for type " + type.getSimpleName()); 
		}
	}

	public boolean isDeveloperOnly() {
		return requireCheats;
	}

	public String getSyntax() {
		return syntax;
	}
}
