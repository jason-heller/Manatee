package lwjgui.loader;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import lwjgui.LWJGUI;
import lwjgui.event.EventHandler;
import lwjgui.event.KeyEvent;
import lwjgui.event.MouseEvent;
import lwjgui.event.ScrollEvent;
import lwjgui.event.TypeEvent;
import lwjgui.geometry.Insets;
import lwjgui.geometry.Pos;
import lwjgui.gl.Renderer;
import lwjgui.paint.Color;
import lwjgui.scene.Node;
import lwjgui.scene.Scene;
import lwjgui.scene.control.Control;
import lwjgui.scene.image.ImageDirect;
import lwjgui.scene.image.ImageView;
import lwjgui.scene.layout.BorderPane;
import lwjgui.scene.layout.DirectionalBox;
import lwjgui.scene.layout.GridPane;
import lwjgui.scene.layout.OpenGLPane;
import lwjgui.scene.layout.Pane;
import lwjgui.scene.layout.StackPane;
import lwjgui.style.Percentage;

public class UILoader
{
	public static final String WORLDSPACE_RENDERER_NAME = "worldspace-renderer";
	
	
	private Map<String, Renderer> renderers = new HashMap<>();
	
	private Map<String, EventHandler<?>> events = new HashMap<>();
	
	private Map<String, Node> controlIdentifiers = new HashMap<>();
	
	private static boolean verbose = false;

	public void loadUI(Scene scene, String htmlPath, String cssPath)
	{
		URL htmlUrl = Thread.currentThread().getContextClassLoader().getResource(htmlPath);
		URL cssUrl = cssPath == null ? null :
			Thread.currentThread().getContextClassLoader().getResource(cssPath);

		loadUI(scene, htmlUrl, cssUrl);
	}
	
	public void loadUI(Scene scene, URL html, URL css)
	{
		Styles styles = new Styles();
		Tag rootTag = TagLoader.ROOT_TAG;
		
		controlIdentifiers.clear();
		
		try
		{
			if (css != null)
				styles.setStyles(StyleLoader.parse(readFileAsString(css)));
			else
				styles.setStyles(new HashMap<String, String>());	// Fallback

			String htmlFilename = html.getFile();
			
			rootTag = TagLoader.parse(readFileAsString(html), styles, htmlFilename);
		}
		catch (IOException | URISyntaxException e)
		{
			e.printStackTrace();
		}
		
		rootTag = getVisibleRootElement(rootTag);
		
		Node rootNode = buildNodeHierachy(null, rootTag, styles);

		if ( verbose )
		{
			System.out.println("setRoot( " + rootNode.getClass().getSimpleName() + " )");
		}

		// Putting random crap here makes it works like even System.out.println WTF AGH
		// Assuming its a context issue
		GLFW.glfwMakeContextCurrent(LWJGUI.getThreadWindow().getHandle());
		
		scene.setRoot(rootNode);
	}
	
	private Tag getVisibleRootElement(Tag rootTag)
	{
		if (rootTag.getData().getCorrospondingControl() == null)
		{
			for(Tag child : rootTag.getChildren())
			{
				Tag tag = getVisibleRootElement(child);
				if (tag != null)
					return tag;
			}
			
			return null;
		}
		
		return rootTag;
	}

	private Node buildNodeHierachy(Node parent, Tag rootTag, Styles styles)
	{
		Node node = parseNode(rootTag, styles);
		
		if (rootTag.getChildren().size() != 0)
		{
			Pane pane = ((Pane)node);
			
			for(Tag tag : rootTag.getChildren())
			{
				Node child = buildNodeHierachy(node, tag, styles);
				
				// Some pane have specific alignments, get them here
				Map<String, String> attribs = tag.getAttributes();
				
				String gridXAttrib = attribs.get("gridx");
				String gridYAttrib = attribs.get("gridy");
				String loopXAttrib = attribs.get("loopx");
				String loopYAttrib = attribs.get("loopy");
				
				PaneAlignment paneAlignment = PaneAlignment.NONE;
				
				int gridX = gridXAttrib == null ? 0 : Integer.parseInt(gridXAttrib);
				int gridY = gridYAttrib == null ? 0 : Integer.parseInt(gridYAttrib);
				int loopX = loopXAttrib == null ? 1 : Integer.parseInt(loopXAttrib);
				int loopY = loopYAttrib == null ? 1 : Integer.parseInt(loopYAttrib);
				
				if (rootTag.getName().toLowerCase().equals("gridpane"))
					paneAlignment = PaneAlignment.GRID;
		
				else 
				{
					String paneAlignmentAttrib = attribs.get("panealignment");//.replace("-", "_");
					
					paneAlignment = PaneAlignment.getAlignment(paneAlignmentAttrib);
				}
				
				AlignmentData alignmentData = new AlignmentData(paneAlignment, gridX, gridY, loopX, loopY);
				
				addChild(pane, child, alignmentData);
			}
		}
		
		return node;
	}

	private void addChild(Pane pane, Node child, AlignmentData alignment)
	{
		if (alignment.getPaneAlignment() != PaneAlignment.NONE)
		{
			if (pane instanceof BorderPane)
			{
				BorderPane borderPane = ((BorderPane)pane);
				
				switch(alignment.getPaneAlignment())
				{
				case LEFT:
					borderPane.setLeft(child);
					break;
				case RIGHT:
					borderPane.setRight(child);
					break;
				case TOP:
					borderPane.setTop(child);
					break;
				case BOTTOM:
					borderPane.setBottom(child);
					break;
				default:
					borderPane.setCenter(child);
				}
			}
			else if (pane instanceof GridPane)
			{
				GridPane gridPane = ((GridPane)pane);

				for(int i = 0; i < alignment.getLoopX(); i++)
				{
					for(int j = 0; j < alignment.getLoopY(); j++)
					{
						Node copy = child.clone();
						
						if (copy != null)
							gridPane.add(copy, alignment.getX() + i, alignment.getY() + j);
						else
						{
							System.err.println("Cloning not supported for element: " + copy);
							break;
						}
					}
				}
			}
			else
			{
				System.err.println("Unknown paneAlignment for pane " + pane);
				pane.getChildren().add(child);
			}
			
			if ( verbose )
			{
				System.out.println(pane.getClass().getSimpleName() + ".addWithAlignment( " + child.getClass().getSimpleName() + ", " + alignment + " )");
			}
		}
		else 
		{
			pane.getChildren().add(child);
			
			if ( verbose )
			{
				System.out.println(pane.getClass().getSimpleName() + ".addChild( " + child.getClass().getSimpleName() + " )");
			}
		}
	}

	private Node parseNode(Tag rootTag, Styles styles)
	{
		final String name = rootTag.getName();
		final TagData tagData = rootTag.getData();

		Map<String, String> attributes = rootTag.getAttributes();
		
		String id = attributes.get("id");
		String[] classes = new String[] {};
		
		if (attributes.containsKey("class"))
				classes = attributes.get("class").split(" ");
		
		String properties = styles.get(name) + styles.get("." + id);
		
		for(String cssClass : classes)
			properties += styles.get("#" + cssClass);

		Class<?> nodeClass = tagData.getCorrospondingControl();
		Node node = null;
		
		if (nodeClass == null)
		{
			System.err.println("Bad tag: " + tagData.name() + " " + name);
			return new StackPane();
		}
		
		try
		{
			if (tagData.argument != null)
			{
				node = (Node) nodeClass.getConstructor(String.class).newInstance(tagData.argument);
			}
			else
			{
				node = (Node) nodeClass.getConstructor().newInstance();
			}
		} 
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
		{
			e.printStackTrace();
		}
		
		if ( verbose )
		{
			String classesStr = ", class=\"";
			for(String s : classes)
				classesStr += s + " ";
			System.out.println("create( " + tagData.name() + classesStr + "\", style=\"" + properties + "\" )");
		}
		
		applyAttributes(node, attributes);
		
		node.setStyle(properties);
		
		if (id != null)
			controlIdentifiers.put(id, node);
		
		return node;
	}

	private void applyAttributes(Node node, Map<String, String> attributes)
	{
		Set<String> keys = attributes.keySet();
		
		for(String key : keys)
		{
			String attrib = attributes.get(key);
			
			switch(key)
			{
			
			case "width":
			{
				setPrefWidth(node, attrib);
				break;
			}
			case "height":
			{
				setPrefHeight(node, attrib);
				break;
			}
			case "fillwidth":
			{
				if (node instanceof Pane)
					((Pane)node).setFillToParentWidth(toBool(attrib));
				else
					((Control)node).setFillToParentWidth(toBool(attrib));
				break;
			}
			case "fillheight":
			{
				if (node instanceof Pane)
					((Pane)node).setFillToParentHeight(toBool(attrib));
				else
					((Control)node).setFillToParentHeight(toBool(attrib));
				break;
			}
			case "padding":
			{
				((Control)node).setPadding(toInsets(attrib));
				break;
			}
			case "background":
			{
				if (attrib.equals("null"))
					((Pane)node).setBackgroundLegacy(null);
				else
				{
					((Pane)node).setBackgroundLegacy(toColor(attrib));
				}
				break;
			}
			case "alignment":
			{
				Pos alignment = Pos.get(attrib);
				
				if (node instanceof DirectionalBox)
					((DirectionalBox)node).setAlignment(alignment);
				else
					((Control)node).setAlignment(alignment);
				break;
			}
				
			case "src":
			case "source":
			{
				ImageDirect image = new ImageDirect(attrib);
				((ImageView)node).setImage(image);
				break;
			}
			case "rendercallback":
			{
				Renderer renderer = renderers.get(attrib);
				if (renderer != null)
				{
					((OpenGLPane)node).setRendererCallback(renderer);
					
					// Exception to use renderer as ID
					if (attrib.equals(WORLDSPACE_RENDERER_NAME))
					{
						controlIdentifiers.put(WORLDSPACE_RENDERER_NAME, node);
					}
				}
				break;
			}
			case "onkeypressed":
			{
				setEvent(node::setOnKeyPressed, attrib);
				break;
			}
			case "onkeyreleased":
			{
				setEvent(node::setOnKeyReleased, attrib);
				break;
			}
			case "ontextinput":
			{
				setEvent(node::setOnTextInput, attrib);
				break;
			}
			case "onselected":
			{
				setEvent(node::setOnSelectedEvent, attrib);
				break;
			}
			case "ondeselected":
			{
				setEvent(node::setOnDeselectedEvent, attrib);
				break;
			}
			case "onclick":
			{
				setEvent(node::setOnMouseClicked, attrib);
				break;
			}
			case "onpressed":
			{
				setEvent(node::setOnMousePressed, attrib);
				break;
			}
			case "onreleased":
			{
				setEvent(node::setOnMouseReleased, attrib);
				break;
			}
			case "onmouseentered":
			{
				setEvent(node::setOnMouseEntered, attrib);
				break;
			}
			case "onmouseexited":
			{
				setEvent(node::setOnMouseExited, attrib);
				break;
			}
			case "onmousedragged":
			{
				setEvent(node::setOnMouseDragged, attrib);
				break;
			}
			case "onmousescrolled":
			{
				setEvent(node::setOnMouseScrolled, attrib);
				break;
			}
				
			case "panealignment":
			case "gridx":
			case "gridy":
			case "loopx":
			case "loopy":
			case "class":
			case "id":
				break;	// Handled elsewhere
				
			default:
			{
				boolean parsed = callReflexive(key, node, attributes, false);
				
				if (!parsed)
					parsed = callReflexive(key, node, attributes, true);
				
				if (!parsed)
					System.err.println("Unknown attribute applied to " + node.getClass().getSimpleName() + ": " + key);
			}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T> void setEvent(Consumer<T> method, String attrib)
	{
		EventHandler<?> eventHandler = events.get(attrib);
		
		if (eventHandler == null)
		{
			System.err.println("Could not find event: " + attrib);
			return;
		}
		
		try {
			method.accept((T)eventHandler);
		}
		catch(Exception e)
		{
			System.err.println("Failed to link event to control (" + attrib + ")");
		}
	}

	private boolean callReflexive(String key, Node node, Map<String, String> properties, boolean asSetter)
	{
		// Try to call method within node, if fail throw err
		Method[] allMethods = node.getClass().getMethods();
		
		String keyLower = (asSetter ? "set" : "") + key.toLowerCase();

		boolean parsed = false;
		for(Method method : allMethods) 
		{
			if (method.getName().toLowerCase().equals(keyLower))
			{
				// Try
				Parameter[] parameters = method.getParameters();

				Object[] outValues = new Object[parameters.length];
				
				// TODO: Account for stuff in small quotes (')
				String[] inValues = properties.get(key).split(",");
				
				// If there's too few arguments, abort. Ignore if theres too many
				if (parameters.length > inValues.length)
				{
					break;
				}
				
				if (verbose && inValues.length > parameters.length)
				{
					System.err.println("Too many arguments for invoking " + method.getName() + ", ignoring the extra ones");
				}
				
				parsed = true;
				
				for(int i = 0; i < parameters.length; i++)
				{
					Class<?> type = parameters[i].getType();
					
					if (inValues[i] == null)
					{
						parsed = false;
						break;
					}

					try {
						switch(type.getSimpleName())
						{
						case "String":
							outValues[i] = inValues[i];
							break;
						case "int":
						case "Integer":
							outValues[i] = Integer.parseInt(inValues[i]);
							break;
						case "double":
						case "Double":
							outValues[i] = Double.parseDouble(inValues[i]);
							break;
						case "float":
						case "Float":
							outValues[i] = Float.parseFloat(inValues[i]);
							break;
						case "boolean":
						case "Boolean":
							outValues[i] = toBool(inValues[i]);
							break;
						}
					}
					catch (NumberFormatException e)
					{
						e.printStackTrace();
						parsed = false;
						break;
					}
					
					if (outValues[i] == null)
					{
						parsed = false;
						break;
					}
				}

				if (parsed)
				{
					try
					{
						method.invoke(node, outValues);
						if (verbose)
						{
							System.err.println("invoked: " + method.getName() + " on " + node.getClass().getSimpleName());
							int i = 0;
							for (Object v : outValues)
							{
								System.err.println("out=" + v + " in=" + inValues[i]);
								i++;
							}
						}
						
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		
		return parsed;
	}

	private static void setPrefWidth(Node node, String attrib)
	{
		String s = asNumber(attrib);
		
		if (s.contains("%"))
		{
			node.setPrefWidthRatio(toPercent(s));
		}
		else
		{
			node.setPrefWidth(toReal(s));
		}
	}
	
	private static void setPrefHeight(Node node, String attrib)
	{
		String s = asNumber(attrib);
		
		if (s.contains("%"))
		{
			node.setPrefHeightRatio(toPercent(s));
		}
		else
		{
			node.setPrefHeight(toReal(s));
		}
	}
	
	/*
	 * 
	 * 
	 */
	
	public void addRenderer(String handle, Renderer renderer)
	{
		renderers.put(handle, renderer);
	}

	public void addEvent(String handle, EventHandler<?> event)
	{
		events.put(handle, event);
	}
	
	public void addKeyEvent(String handle, EventHandler<KeyEvent> event)
	{
		events.put(handle, event);
	}
	
	public void addMouseEvent(String handle, EventHandler<MouseEvent> event)
	{
		events.put(handle, event);
	}
	
	public void addScrollEvent(String handle, EventHandler<ScrollEvent> event)
	{
		events.put(handle, event);
	}
	
	public void addTypeEvent(String handle, EventHandler<TypeEvent> event)
	{
		events.put(handle, event);
	}

	public Node getFromID(String id)
	{
		return controlIdentifiers.get(id);
	}
	
	/*
	 * Helper classes
	 * 
	 */
	
	private static boolean toBool(String s)
	{
		if (s.equals("true") || s.equals("1"))
			return true;
		
		if (s.equals("false") || s.equals("0"))
			return false;
		
		System.err.println("Unknown boolean value: " + s);
		return false;
	}
	
	private static double toReal(String s)
	{
		return Double.parseDouble(s);
	}

	private static Percentage toPercent(String s)
	{
		return new Percentage(Double.parseDouble(s.substring(0, s.length() - 1)));
	}
	
	private static Insets toInsets(String s)
	{
		if (s.contains(","))
		{
			String[] vals = s.replaceAll(" ", "").split(",");
			
			if (vals.length < 4)
				return new Insets(0);
			
			return new Insets(toReal(vals[0]), toReal(vals[1]), toReal(vals[2]), toReal(vals[3]));
		}
		
		return new Insets(toReal(s));
	}
	
	private static Color toColor(String s)
	{
		if (s.contains(","))
		{
			String[] vals = s.replaceAll(" ", "").split(",");
			
			if (vals.length < 3)
				return Color.WHITE;
			
			return new Color(Integer.parseInt(vals[0]), Integer.parseInt(vals[1]), Integer.parseInt(vals[2]));
		}
		
		return new Color(s);
	}

	private static String asNumber(String s)
	{
		return s.replaceAll("[^\\d.-]", "");
	}

	private static String readFileAsString(URL url) throws IOException, URISyntaxException
	{
		return new String(Files.readAllBytes(Paths.get(url.toURI())), StandardCharsets.UTF_8);
	}
}
