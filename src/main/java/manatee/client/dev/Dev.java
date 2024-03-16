package manatee.client.dev;

import static org.lwjgl.nanovg.NanoVG.nvgBeginFrame;
import static org.lwjgl.nanovg.NanoVG.nvgEndFrame;
import static org.lwjgl.nanovg.NanoVG.nvgRestore;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.joml.Vector3f;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.opengl.GL11;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import lwjgui.LWJGUI;
import lwjgui.paint.Color;
import lwjgui.scene.Context;
import lwjgui.scene.Scene;
import lwjgui.scene.Window;
import manatee.client.Client;
import manatee.client.gl.renderer.nvg.NVGText;
import manatee.client.scene.MapScene;

/** Class for debugging
 * 
 *
 */
public class Dev
{
	private static Set<Tracker> trackers = new HashSet<>();
	
	private static Map<Tracker, NVGText> nvgTexts = new HashMap<>();
	
	private static final Tracker DUMMY_TRACKER = new Tracker(null, null, null);
	
	private static final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
	
	public static void render()
	{
		Window window = LWJGUI.getThreadWindow();
		Scene scene = window.getScene();
		Context context = window.getContext();
		
		nvgBeginFrame(context.getNVG(), window.getWidth(), window.getHeight(), window.getPixelRatio());
		context.setScissor(scene.getX(), scene.getY(), scene.getWidth(), scene.getHeight());
		
		long ctx = context.getNVG();
		
		// Developer console
		DeveloperConsole.suggestions().render(ctx);
		
		// Trackers
		float y = window.getHeight();
		
		for(Tracker tracker : trackers)
		{
			String str = tracker.toString();
			
			boolean condMet = tracker.conditionsMet();
			
			float[] bounds = new float[4];
			NanoVG.nvgTextBounds(ctx, 0, 0, str, bounds);
			
			NanoVG.nvgBeginPath(ctx);
			NanoVG.nvgRect(ctx, 0f, y - 18, bounds[2], 18);
			NanoVG.nvgFillColor(ctx, condMet ? Color.BLUE.getNVG() : Color.RED.getNVG());
			NanoVG.nvgFill(ctx);
			
			NanoVG.nvgFillColor(ctx, Color.WHITE.getNVG());
			NanoVG.nvgText(ctx, 0f, y - 3, str);
			
			y -= 20;
		}

		nvgRestore(ctx);
		nvgEndFrame(ctx);
		
		// Labels
		for(Tracker obj : nvgTexts.keySet())
		{
			NVGText text = nvgTexts.get(obj);
			
			text.setBackgroundColor(obj.conditionsMet() ? Color.BLUE : Color.RED);
			text.setText(obj.toString());
		}
	}
	
	/**
	 * Prints the given fields in 'obj' on screen in real-time. Use untrack() to
	 * remove the tracker
	 * 
	 * @param obj        The object containing the fields
	 * @param fieldNames the fields to be listed
	 * @return The tracker object
	 */
	public static Tracker track(Object obj, String... fieldNames)
	{
		
		Tracker tracker = getTracker(obj, fieldNames);
		
		trackers.add(tracker);
		
		return tracker;
	}

	/**
	 * Prints the object 'obj' on screen in real-time. Use untrack() to remove the
	 * tracker. If there is already a tracker with the same name, the obj is
	 * updated instead
	 * 
	 * @param title What to prefix on the obj printed on stuff
	 * @param obj The object to print (calls toString())
	 * @return the tracker object
	 */
	public static Tracker track(String title, Object obj)
	{
		for(Tracker tracker : trackers)
		{
			if (tracker.getFields() == null && tracker.getNames()[0].equals(title))
			{
				tracker.setObj(obj);
				return tracker;
			}
		}
		
		Tracker tracker = new Tracker(title, obj);
		
		trackers.add(tracker);
		
		return tracker;
	}
	
	/**
	 * Prints the object 'obj' on screen in real-time. The tracker is drawn on the
	 * screen in worldspace at 'pos'. Use untrack() to remove the tracker
	 * 
	 * @param title What to prefix on the obj printed on stuff
	 * @param obj The object to print (calls toString())
	 * @return The tracker object
	 */
	public static Tracker track(Vector3f pos, String title, Object obj)
	{
		if (!(Client.scene() instanceof MapScene))
		{
			logger.error("Tracker needs worldspace to print at a 3D position");
			return null;
		}
		
		for(Tracker tracker : trackers)
		{
			if (tracker.getFields() == null && tracker.getNames()[0].equals(title))
			{
				tracker.setObj(obj);
				return tracker;
			}
		}
			
		MapScene scene = ((MapScene)Client.scene());
		
		Tracker tracker = new Tracker(title, obj);
		
		nvgTexts.put(tracker, scene.addText("", pos));
		
		return tracker;
	}

	/**
	 * Prints the given fields in 'obj' on screen in real-time. The tracker is drawn
	 * on the screen in worldspace at 'pos'. Use untrack() to remove the tracker
	 * 
	 * @param pos        the position to draw in the tracker in world space
	 * @param obj        The object containing the fields
	 * @param fieldNames the fields to be listed
	 * @return The tracker object
	 */
	public static Tracker track(Vector3f pos, Object obj, String... fieldNames)
	{
		if (!(Client.scene() instanceof MapScene))
		{
			logger.error("Tracker needs worldspace to print at a 3D position");
			return null;
		}
			
		MapScene scene = ((MapScene)Client.scene());
		
		Tracker tracker = getTracker(obj, fieldNames);
		
		nvgTexts.put(tracker, scene.addText("", pos));
		
		return tracker;
	}
	
	private static Tracker getTracker(Object obj, String... fieldNames)
	{
		Class<?> c = obj.getClass();
		try
		{
			Field[] fields = new Field[fieldNames.length];
			
			for(int i = 0; i < fieldNames.length; ++i)
			{
				for (Field f : c.getDeclaredFields())
				{
					if (f.getName().equals(fieldNames[i]))
					{
						f.setAccessible(true);
						fields[i] = f;
						break;
					}
				}
				
				if (fields[i] == null)
				{
					for (Field f : c.getSuperclass().getDeclaredFields())
					{
						if (f.getName().equals(fieldNames[i]))
						{
							f.setAccessible(true);
							fields[i] = f;
							break;
						}
					}
				}
				
				if (fields[i] == null)
				{
					logger.warn("Tracker failed, no such element: " + obj + ", " + fieldNames[i]);
					trackers.add(DUMMY_TRACKER);
					return DUMMY_TRACKER;
				}
				
				logger.info("Tracking " + obj + ", " + fieldNames[i]);
			}
			
			return new Tracker(fieldNames, obj, fields);
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		
		return DUMMY_TRACKER;
	}

	/**
	 * Removes all trackers tracking the specified fieldName & object
	 * 
	 * @param obj       The object holding the field to be untracked
	 * @param fieldName The name of the field to be untracked
	 */
	public static void untrack(Object obj, String fieldName)
	{
		Iterator<Tracker> iter = trackers.iterator();
		
		untrack(iter, obj, fieldName);
		
		iter = nvgTexts.keySet().iterator();
		
		untrack(iter, obj, fieldName);
	}

	/**
	 * Removes all trackers tracking object
	 * 
	 * @param obj The object holding the field to be untracked
	 */
	public static void untrack(Object obj)
	{
		Iterator<Tracker> iter = trackers.iterator();
		
		iter = nvgTexts.keySet().iterator();
		
		while(iter.hasNext())
		{
			Tracker tracker = iter.next();
			
			if (tracker.getObj().equals(obj))
			{
				iter.remove();
				logger.info("Untracking " + tracker.getObj() + ", " + obj);
			}
		}
	}

	private static void untrack(Iterator<Tracker> iter, Object obj, String fieldName)
	{
		while(iter.hasNext())
		{
			Tracker tracker = iter.next();
			
			if (tracker.getObj() != obj)
				continue;
			
			if (tracker.getFields() == null)
			{
				if (tracker.getNames()[0].equals(fieldName))
				{
					iter.remove();
					logger.info("Untracking " + tracker.getObj() + ", " + fieldName);
				}
			}
			else
			{
				for(Field f : tracker.getFields())
				{
					if (f.getName().equals(fieldName))
					{
						iter.remove();
						logger.info("Untracking " + tracker.getObj() + ", " + fieldName);
					}
				}
			}
		}
	}

	public static void log(Object... objs)
	{
		StringBuilder sb = new StringBuilder();
		
		for(Object o : objs)
		{
			String oStr = o == null ? "null" : o.toString();
			
			sb.append(oStr).append(" ");
		}
		
		logger.debug(sb.toString());
	}

	public static void logTimedPerSecond(int ms, Object... objs)
	{
		if (System.currentTimeMillis() % 1000 < ms)
		{
			log(objs);
		}
	}

	public static String getCurrentArea() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        
        // The first element in the stack trace is the current method
        // The second element is the caller method
        // The third element is usually the method that invoked the caller method (and so on)
        // So, we can choose the third element to represent the current execution area
        if (stackTrace.length >= 3) {
            StackTraceElement currentElement = stackTrace[2];
            return currentElement.getClassName() + "." + currentElement.getMethodName();
        } else {
            return "Unknown";
        }
    }

	public static void glError()
	{
		int error = GL11.glGetError();
		if (error != GL11.GL_NO_ERROR) {
		    System.err.println("OpenGL error: " + error);
		}
	}
}
