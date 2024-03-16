package manatee.client;

import static org.lwjgl.glfw.GLFW.glfwInit;

import java.io.File;
import java.nio.file.Paths;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import manatee.client.input.Input;

public class App
{

	public static final File ENGINE_DIR = new File(System.getProperty("user.home"), ".manatee");
	public static final File CACHE_DIR = new File(ENGINE_DIR, "cache");
	public static final File PLUGINS_DIR = new File(ENGINE_DIR, "plugins");
	public static final File SCREENSHOT_DIR = new File(ENGINE_DIR, "screenshots");
	public static final File LOGS_DIR = new File(ENGINE_DIR, "logs");
	
	public static final String WORKING_DIRECTORY = Paths.get(".").toAbsolutePath().normalize().toString();

	public static boolean developerMode;
	
	// private GLFWErrorCallback errorCallback;

	public static void main(String[] args) throws Exception
	{
		final OptionParser parser = new OptionParser();
		parser.accepts("developer-mode", "Enable developer tools");
		parser.accepts("debug", "Show debugging output");
		parser.accepts("level-editor", "Enable level editor");
		parser.accepts("particle-editor", "Enable particle editor");
		
		parser.accepts("help", "Show this text").forHelp();
		
		OptionSet options = parser.parse(args);

		if (options.has("help"))
		{
			parser.printHelpOn(System.out);
			System.exit(0);
		}
		
		if (options.has("debug"))
		{
			final Logger loggerRoot = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
			loggerRoot.setLevel(Level.DEBUG);
			
			Logger logger = (Logger) LoggerFactory.getLogger("IO");
			logger.setLevel(Level.DEBUG);
		}

		developerMode = options.has("developer-mode");
		
		boolean editorMode = options.has("level-editor");
		boolean particleView = options.has("particle-editor");

		new App().start(editorMode, particleView);
	}

	public void start(boolean editorMode, boolean particleView) throws Exception
	{
		Input.init();
		
		//if (LWJGUIUtil.restartJVMOnFirstThread(true, args))
		//	return;
		
		// Initialize GLFW
		if ( !glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");

		new Client(editorMode, particleView);
	}
}
