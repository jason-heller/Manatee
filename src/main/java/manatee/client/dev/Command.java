package manatee.client.dev;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import manatee.client.App;
import manatee.util.Trie;

public class Command
{

	private static Trie commands = new Trie();
	private static Map<String, CommandExecutor> executors = new HashMap<>();

	private final static Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
	
	public static final String BOOL_SYNTAX = "0/1";
	public static final String FLOAT_SYNTAX = "float";
	public static final String INT_SYNTAX = "int";

	public static void add(String commandName, String syntax, Object object, String methodName,
			boolean devToolsRequired)
	{
		add(commandName, syntax, object, object.getClass(), methodName, devToolsRequired);
	}
	
	public static void add(String commandName, String syntax, Class<?> methodClass, String methodName,
			boolean devToolsRequired)
	{
		add(commandName, syntax, null, methodClass, methodName, devToolsRequired);
	}

	// Convenience
	public static void add(String methodName, String syntax, Object object, boolean devToolsRequired)
	{
		add(methodName, syntax, object, object.getClass(), methodName, devToolsRequired);
	}
	
	public static void add(String methodName, String syntax, Object object)
	{
		add(methodName, syntax, object, object.getClass(), methodName, true);
	}

	private static void add(String commandName, String syntax, Object object, Class<?> methodClass, String methodName,
			boolean devToolsRequired)
	{
		Method[] methods = methodClass.getMethods();

		if (methods.length == 0)
		{
			System.err.println("No methods found under the name " + methodName);
			return;
		}

		List<Method> applicableMethods = new ArrayList<>();

		for (Method method : methods)
		{
			String name = method.getName();

			if (name.equals(methodName))
			{
				applicableMethods.add(method);
			}
		}

		int numMethods = applicableMethods.size();

		String commandNameLower = commandName.toLowerCase();

		if (numMethods == 0)
		{
			System.err.println("No methods found to add to command " + commandNameLower);
			return;
		}

		Method[] commandMethods = new Method[numMethods];

		for (int i = 0; i < numMethods; i++)
			commandMethods[i] = applicableMethods.get(i);

		CommandExecutor exec = new CommandExecutor(syntax, object, commandMethods, devToolsRequired);

		commands.insert(commandNameLower);
		executors.put(commandNameLower, exec);
	}

	public static void processCommand(String commandLineInput)
	{
		if (commandLineInput.length() == 0)
			return;

		String head = null;

		List<String> arguments = new ArrayList<>();
		String reader = "";

		boolean bracketed = false;

		for (int i = 0; i < commandLineInput.length(); i++)
		{
			char c = commandLineInput.charAt(i);

			if (!bracketed)
				c = Character.toLowerCase(c);

			if (c == ' ')
			{
				if (head == null)
					head = reader;
				else
					arguments.add(reader);

				reader = "";
				continue;
			} else if (c == '\"' || c == '\'')
				bracketed = !bracketed;
			else
				reader += c;
		}

		if (head == null)
			head = reader;
		else
			arguments.add(reader);

		CommandExecutor command = executors.get(head);

		if (command == null)
		{
			logger.info("No such command: " + head);
			return;
		}

		if (command.isDeveloperOnly() && !App.developerMode)
		{
			logger.info("Command requires developer tools to be enabled.");
			return;
		}

		logger.info("]" + commandLineInput);

		try
		{
			command.process(arguments);

		}
		catch (IllegalArgumentException e)
		{
			logger.info("Usage: " + head + " " + command.getSyntax());
		}
		catch (NotImplementedException e)
		{
			logger.info("Command has not properly been implemented: " + head);
		}
		catch (Exception e)
		{
			logger.info("Command failed: " + head);
			e.printStackTrace();
		}
	}

	public static List<String> getSuggestion(String text)
	{
		List<String> suggestedCmds = commands.getSuggestions(text.toLowerCase());

		for (int i = 0; i < suggestedCmds.size(); ++i)
		{
			String head = suggestedCmds.get(i);
			CommandExecutor cmdExec = executors.get(head);

			final String syntaxSeperator = cmdExec.getSyntax().equals("") ? "" : " ";
			suggestedCmds.set(i, head + syntaxSeperator + cmdExec.getSyntax());
		}

		return suggestedCmds;
	}
}
