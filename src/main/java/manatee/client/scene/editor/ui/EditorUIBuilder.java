package manatee.client.scene.editor.ui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.qos.logback.classic.Logger;
import lwjgui.event.ActionEvent;
import lwjgui.event.EventHandler;
import lwjgui.font.FontMetaData;
import lwjgui.font.FontStyle;
import lwjgui.geometry.Insets;
import lwjgui.geometry.Pos;
import lwjgui.paint.Color;
import lwjgui.scene.Node;
import lwjgui.scene.control.Button;
import lwjgui.scene.control.CodeArea;
import lwjgui.scene.control.ColorPicker;
import lwjgui.scene.control.ComboBox;
import lwjgui.scene.control.ContextMenu;
import lwjgui.scene.control.Label;
import lwjgui.scene.control.Menu;
import lwjgui.scene.control.MenuBar;
import lwjgui.scene.control.MenuItem;
import lwjgui.scene.control.ScrollPane;
import lwjgui.scene.control.Slider;
import lwjgui.scene.control.StandaloneToggleButton;
import lwjgui.scene.control.Tab;
import lwjgui.scene.control.TabPane;
import lwjgui.scene.control.TextArea;
import lwjgui.scene.control.TextField;
import lwjgui.scene.control.ToggleButton;
import lwjgui.scene.control.ToggleGroup;
import lwjgui.scene.image.Image;
import lwjgui.scene.image.ImageView;
import lwjgui.scene.layout.BorderPane;
import lwjgui.scene.layout.HBox;
import lwjgui.scene.layout.OpenGLPane;
import lwjgui.scene.layout.Pane;
import lwjgui.scene.layout.StackPane;
import lwjgui.scene.layout.VBox;
import lwjgui.scene.layout.floating.FloatingPane;
import lwjgui.style.Background;
import lwjgui.style.BackgroundSolid;
import lwjgui.style.Percentage;
import lwjgui.theme.Theme;
import lwjgui.theme.ThemeDark;
import manatee.cache.definitions.lump.EntityLump;
import manatee.client.dev.Command;
import manatee.client.dev.LoggerOutputStream;
import manatee.client.entity.SpatialEntity;
import manatee.client.entity.stock.editor.EditorEntity;
import manatee.client.gl.camera.ControllableCamera;
import manatee.client.gl.camera.FloatingCamera;
import manatee.client.gl.camera.FreeCamera;
import manatee.client.gl.camera.ICamera;
import manatee.client.map.tile.Tile;
import manatee.client.map.tile.TileFlags;
import manatee.client.map.tile.Tilemap;
import manatee.client.scene.IScene;
import manatee.client.scene.editor.EditorScene;
import manatee.client.scene.editor.EditorTool;
import manatee.client.scene.editor.HeightToolMode;
import manatee.client.ui.ClientRenderer;
import manatee.client.ui.UIBuilder;

public class EditorUIBuilder implements UIBuilder
{
	private OpenGLPane oglPane;
	
	private Image selectToolImage, cameraToolImage, itemToolImage, entityToolImage, heightToolImage, addChunkToolImage;
	private Image entBboxImage, snapImage, lockImage;
	
	private EditorScene scene;
	
	private VBox[] properties = new VBox[3];
	
	public EditorUIBuilder()
	{
		initImages();
	}
	
	public Node buildUI(IScene gameScene, ClientRenderer clientRenderer)
	{
		FloatingPane root = new FloatingPane();
		
		scene = (EditorScene)gameScene;	// Should never be a case where scene is not an EditorScene

		Theme.setTheme(new ThemeDark());
		
		Background bg = new BackgroundSolid(Theme.current().getBackgroundAlt());

		Command.add("editor_enable_all_tiles", Command.BOOL_SYNTAX, TilePickerWindow.class, "setAllowAllTiles", true);
		
		BorderPane hud;
		VBox top;
		HBox topButtons;
		MenuBar menuBar;
		VBox consoleBox, toolBox, toolProperties;
		HBox bottomBar;
		TabPane iTabs, eTabs;
		StackPane toolPropertyPane;
		TextArea consoleDisplay;
		TextField consoleInput;
		
		oglPane = new OpenGLPane();
		
		{
			hud = new BorderPane();
			hud.setBackground(bg);
			{
				oglPane.setFillToParentHeight(true);
				oglPane.setFillToParentWidth(true);
				oglPane.setMouseTransparent(false);
				
				// Menu Bar + Buttons
				{
					top = new VBox();
					top.setFillToParentWidth(true);
					
					menuBar = new MenuBar();
					
					// Create File Menu
					Menu file = new Menu("File");
					addMenuItem(file, "New", (e) -> {EditorScene.newMapPopup(scene);});
					addMenuItem(file, "Open", (e) -> {EditorScene.queryFilePopup(scene, false, true);});
					addMenuItem(file, "Save", (e) -> {EditorScene.queryFilePopup(scene, true, false);});
					addMenuItem(file, "Save As", (e) -> {EditorScene.queryFilePopup(scene, true, true);});
					addMenuItem(file, "Export", (e) -> {EditorScene.queryExportPopup(scene);});
					
					menuBar.getItems().add(file);
					
					// Create Edit Menu
					Menu edit = new Menu("Edit");
					addMenuItem(edit, "Undo", (e) -> {scene.getHistory().undo();});
					addMenuItem(edit, "Redo", (e) -> {scene.getHistory().redo();});
					menuBar.getItems().add(edit);
					
					Menu tools = new Menu("Tools");
					
					MenuItem divider = new MenuItem("---");
					divider.setMouseTransparent(true);
					
					addMenuItem(tools, "Forest", (e) -> {scene.setTileset("forest");});
					addMenuItem(tools, "Snow", (e) -> {scene.setTileset("snow");});
					addMenuItem(tools, "Cave", (e) -> {scene.setTileset("cave");});
					addMenuItem(tools, "Castle", (e) -> {scene.setTileset("castle");});
					addMenuItem(tools, "Sky", (e) -> {scene.setTileset("sky");});
					addMenuItem(tools, "Hallows", (e) -> {scene.setTileset("hallows");});
					addMenuItem(tools, "Beach", (e) -> {scene.setTileset("beach");});
					
					tools.getItems().add(divider);

					addMenuItem(tools, "Tile Chooser", (e) -> {TilePickerWindow.create(scene);});
					
					menuBar.getItems().add(tools);
					
					topButtons = new HBox();
					{
						addImageButton(topButtons, null, entBboxImage, 16, 16, (event)-> {
							scene.placeEntOnGround = !scene.placeEntOnGround;
						}, "Toggle placing entities at center");
						
						addImageButton(topButtons, null, snapImage, 16, 16, (event)-> {
							scene.snapToGrid = !scene.snapToGrid;
						}, "Toggle snap to grid");
						
						addImageButton(topButtons, null, lockImage, 16, 16, (event)-> {
							scene.axisLock = !scene.axisLock;
						}, "Toggle straight-line placement");
					}
				}
				
				toolProperties = new VBox();
				toolProperties.setSpacing(8);
				toolProperties.setAlignment(Pos.TOP_LEFT);
				toolProperties.setPrefWidthRatio(new Percentage(20));
				toolProperties.setFillToParentHeight(true);
				{
					toolPropertyPane = new StackPane();
					toolPropertyPane.setFillToParentWidth(true);
					toolPropertyPane.setFillToParentHeight(true);
					//toolPropertyPane.setPrefWidthRatio(new Percentage(20));
					
					{
						properties[0] = new VBox();
						properties[0].setFillToParentWidth(true);
						properties[0].setFillToParentHeight(true);
						properties[0].setAlignment(Pos.TOP_LEFT);
						properties[0].setSpacing(15);
						properties[0].setBorder(new Insets(30f, 30f, 30f, 30f));
						
						
						// Strength slider
						properties[0].getChildren().add(new Label("Strength"));
						HBox hboxDelta = new HBox();
						hboxDelta.setFillToParentWidth(true);
						hboxDelta.setSpacing(2f);
						{
							Slider deltaSlider = new Slider(0.05f, 2f, scene.getHeightDelta());
							Label deltaLabel = new Label("" + scene.getHeightDelta());
							deltaSlider.setFillToParentWidth(true);
							deltaSlider.setOnValueChangedEvent((event) ->
							{
								scene.setHeightDelta((float) deltaSlider.getValue());
								deltaLabel.setText(String.format("%.2f", deltaSlider.getValue()));
							});
							hboxDelta.getChildren().add(deltaSlider);
							hboxDelta.getChildren().add(deltaLabel);
						}
						properties[0].getChildren().add(hboxDelta);

						// Radius slider
						properties[0].getChildren().add(new Label("Radius"));
						HBox hboxRadius = new HBox();
						hboxRadius.setFillToParentWidth(true);
						hboxRadius.setSpacing(2f);
						{
							Slider radiusSlider = new Slider(0.1f, 50f, scene.getHeightRadius());
							Label radiusLabel = new Label("" + scene.getHeightRadius());
							radiusSlider.setFillToParentWidth(true);
							radiusSlider.setOnValueChangedEvent((event) ->
							{
								scene.setHeightRadius((float) radiusSlider.getValue());
								radiusLabel.setText(String.format("%.2f", radiusSlider.getValue()));
							});
							hboxRadius.getChildren().add(radiusSlider);
							hboxRadius.getChildren().add(radiusLabel);
						}
						properties[0].getChildren().add(hboxRadius);
						
						// Mode
						properties[0].getChildren().add(new Label("Mode"));
						HBox t = new HBox();
						t.setSpacing(8);
						t.setAlignment(Pos.CENTER);
						properties[0].getChildren().add(t);

						{
							ComboBox<String> combo = new ComboBox<String>("");
							combo.setPrefWidth(120);
							combo.getItems().add("Raise");
							combo.getItems().add("Lower");
							combo.getItems().add("Smooth");
							combo.getItems().add("Flatten");
							combo.getItems().add("Zero");
							combo.getItems().add("Paint_Texture");
							combo.getItems().add("Erase_Texture");
							
							combo.setValue("Raise");
							
							combo.setOnAction((e) -> {
								scene.setHeightMode(HeightToolMode.valueOf(combo.getValue().toUpperCase()));
							});
							
							t.getChildren().add(combo);
						}

						toolPropertyPane.getChildren().add(properties[0]);
						properties[0].setVisible(false);
					}
					
					{
						properties[1] = new VBox();
						properties[1].setFillToParentWidth(true);
						properties[1].setFillToParentHeight(true);
						properties[1].setAlignment(Pos.TOP_LEFT);
						properties[1].setSpacing(15);
						properties[1].setBorder(new Insets(30f, 30f, 30f, 30f));
						
						{
							colorPicker(properties[1], "Sky Color", EditorScene.DEFAULT_SKY_COLOR, scene.getColor());
							//colorPicker(properties[1], "Water Color", new Color(59 / 255f, 137 / 255f, 196 / 255f), scene.getWaterColor());
						}
						toolPropertyPane.getChildren().add(properties[1]);
						
						properties[2] = new VBox();
						properties[2].setFillToParentWidth(true);
						properties[2].setFillToParentHeight(true);
						properties[2].setAlignment(Pos.TOP_LEFT);
						properties[2].setSpacing(15);
						properties[2].setBorder(new Insets(30f, 30f, 30f, 30f));
						{
							colorPicker(properties[2], "Entity Color", Color.WHITE, scene.getSelected());
							
							label(properties[2], "Entity Tags");
							CodeArea ca = new CodeArea();
							ca.setPrefWidthRatio(new Percentage(17));
							ca.setFillToParentHeight(true);
							
							// Pass data onto selected
							ca.setOnTextChange((event)->{
								if (scene.getLastEntityToEditTags() != null && scene.getSelected().contains(scene.getLastEntityToEditTags()))
								{
									
									String text = ca.getText();
									Matcher matcher = EditorScene.PATTERN.matcher(text);
									
									Map<String, String> tags = new HashMap<>();
									String key = null;
									ca.resetHighlighting();
									
									while ( matcher.find() ) {
										int start = matcher.start();
										int end = matcher.end()-1;
										
										if ( matcher.group("KEY") != null && !matcher.group("KEY").equals("") ) {
											ca.setHighlighting(start, end, new FontMetaData().color(Color.LIGHT_BLUE).style(FontStyle.BOLD));
											key = matcher.group();
										} else if ( matcher.group("STRING") != null ) {
											ca.setHighlighting(start, end, new FontMetaData().color(Color.SALMON).style(FontStyle.BOLD));
											tags.put(key, matcher.group());
											key = null;
										} else if ( matcher.group("VALUE") != null ) {
											ca.setHighlighting(start, end, new FontMetaData().color(Color.LIGHT_GREEN).style(FontStyle.BOLD));
											tags.put(key, matcher.group());
											key = null;
										}
									}
									
									scene.getLastEntityToEditTags().setTags(tags);
								}
							});

							scene.setEntityTagEditor(ca);
							
							properties[2].getChildren().add(ca);
						}
						properties[2].setVisible(false);
						
						scene.setPropertiesTabs(properties);
						
						toolPropertyPane.getChildren().add(properties[2]);
						
					}
					
					
					toolProperties.getChildren().add(toolPropertyPane);
				}

				bottomBar = new HBox();
				bottomBar.setSpacing(24);
				bottomBar.setAlignment(Pos.TOP_LEFT);
				bottomBar.setFillToParentWidth(true);
				bottomBar.setPadding(new Insets(12));
				bottomBar.setPrefHeightRatio(Percentage.TEN);
				{
					StackPane toolElementPicker = new StackPane();
					toolElementPicker.setFillToParentWidth(false);
					toolElementPicker.setPrefWidth(300);
					//toolElementPicker.setPrefWidthRatio(new Percentage(9));
					
					// Item / Entity picker
					/*iTabs = new TabPane();
					iTabs.setFillToParentHeight(false);
					iTabs.setMaxHeight(180);
					iTabs.setMouseTransparent(false);
					{
						addTab(iTabs, "All", Integer.MAX_VALUE);
						addTab(iTabs, "Nature", 1 << TileFlags.NATURE.ordinal());
						addTab(iTabs, "Buildings", 1 << TileFlags.BUILDING.ordinal());
						addTab(iTabs, "Misc.", 1 << TileFlags.MISCELLANEOUS.ordinal());
					}*/
					
					eTabs = new TabPane();
					eTabs.setFillToParentHeight(false);
					eTabs.setMaxHeight(180);
					eTabs.setMouseTransparent(false);
					{
						Tab tab1 = new Tab("General");
						tab1.setUserCanClose(false);
						eTabs.getTabs().add(tab1);
						Tab tab2 = new Tab("Tool/Clip");
						tab2.setUserCanClose(false);
						eTabs.getTabs().add(tab2);
						
						try
						{
							String json;
						
							json = Files.readString(Paths.get("src/main/resources/data/editor/edict.json"), StandardCharsets.UTF_8);
							
							Gson gson = new GsonBuilder().create();
							
							EntityLump[] eData = gson.fromJson(json, EntityLump[].class);
							
							fillSelectionTab(tab1, tab2, eData);
						}
						catch (IOException e1)
						{
							e1.printStackTrace();
						}
					}
					
					eTabs.setVisible(false);
					//iTabs.setVisible(false);
					//toolElementPicker.getChildren().add(iTabs);
					toolElementPicker.getChildren().add(eTabs);
					bottomBar.getChildren().add(toolElementPicker);
					
					consoleBox = new VBox();
					consoleBox.setMinWidth(600);
					consoleBox.setBackgroundLegacy(new Color("#3b1b10"));
					{
						consoleDisplay = new TextArea();
						consoleDisplay.setFillToParentWidth(true);
						consoleDisplay.setEditable(false);

						consoleInput = new TextField();
						consoleInput.setFillToParentWidth(true);
					}
				}

			}
			
			// Toolbar
			toolBox = new VBox();
			toolBox.setFillToParentHeight(true);
			toolBox.setSpacing(2.0);
			{
				
				final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
				
				ToggleGroup g = new ToggleGroup();
				
				addImageButton(toolBox, g, selectToolImage, 40, 32, (event)-> {
					//iTabs.setVisible(false);
					eTabs.setVisible(false);

					setPropertyBox(1);
					
					scene.setTool(EditorTool.SELECT);
				});
				
				addImageButton(toolBox, g, cameraToolImage, 40, 32, (event)-> {
					if (scene.getCamera() instanceof FreeCamera)
					{
						scene.setCamera(new FloatingCamera());
						logger.info("Set camera mode to: Floating Camera");
					}
					else
					{
						scene.setCamera(new FreeCamera());
						logger.info("Set camera mode to: Free Camera");
					}
				});
				
				addImageButton(toolBox, g, entityToolImage, 40, 32, (event)-> {
					//iTabs.setVisible(false);
					eTabs.setVisible(true);
					
					setPropertyBox(2);
					
					scene.setTool(EditorTool.ENTITY);
				});
				
				addImageButton(toolBox, g, itemToolImage, 40, 32, (event)-> {
					//iTabs.setVisible(true);
					eTabs.setVisible(false);
					
					setPropertyBox(1);
					
					scene.setTool(EditorTool.ITEM);
					
					TilePickerWindow.create(scene);
				});
				
				addImageButton(toolBox, g, heightToolImage, 40, 32, (event)-> {
					//iTabs.setVisible(false);
					eTabs.setVisible(false);
					
					setPropertyBox(0);
					
					scene.setTool(EditorTool.HEIGHT);
				});
				
				addImageButton(toolBox, g, addChunkToolImage, 40, 32, (event)-> {
					//iTabs.setVisible(false);
					eTabs.setVisible(false);
					
					setPropertyBox(1);
					
					
					scene.setTool(EditorTool.CHUNK);
				});
			}

			top.getChildren().add(menuBar);
			top.getChildren().add(topButtons);
			
			root.getChildren().add(hud);
			hud.setTop(top);
			hud.setCenter(oglPane);
			hud.setBottom(bottomBar);
			hud.setLeft(toolBox);
			hud.setRight(toolProperties);
			bottomBar.getChildren().add(consoleBox);
			consoleBox.getChildren().add(consoleDisplay);
			consoleBox.getChildren().add(consoleInput);

			oglPane.setOnMouseEntered(e ->
			{
				ICamera camera = gameScene.getCamera();

				if (camera instanceof ControllableCamera)
					((ControllableCamera) camera).setDraggable(true);
			});

			oglPane.setOnMouseExited(e ->
			{
				;
				ICamera camera = gameScene.getCamera();

				if (camera instanceof ControllableCamera)
					((ControllableCamera) camera).setDraggable(false);
			});

			oglPane.setRendererCallback(clientRenderer);
		}

		consoleInput.setOnKeyPressed((keyEvent) ->
		{
			if (keyEvent.getKey() == GLFW.GLFW_KEY_ENTER)
			{
				String input = consoleInput.getText();

				Command.processCommand(input);

				consoleInput.clear();
			}
		});
		
		LoggerOutputStream.addListener(consoleDisplay);

		consoleDisplay.setDisabled(true);
		
		return root;
	}
	
	private void addTab(TabPane root, String title, int include)
	{
		Tab t = new Tab(title);
		t.setUserCanClose(false);
		
		fillSelectionTab(t, include);
		
		root.getTabs().add(t);
	}
	
	private void colorPicker(Pane p, String string, Color color, Set<SpatialEntity> entities)
	{
		label(p, string);
		
		ColorPicker cp = new ColorPicker();
		cp.setAlignment(Pos.TOP_RIGHT);
		cp.setFillToParentWidth(true);
		
		cp.setOnColorUpdate((event) -> {
			Vector4f c = cp.getColor().getVector();
			for(SpatialEntity e : entities)
				if (e != null)
					((EditorEntity)e).setColor(c);
		});
		
		p.getChildren().add(cp);
		
		scene.setEntityColorPicker(cp);
	}
	
	private void colorPicker(Pane p, String string, Color color, Vector4f out)
	{
		label(p, string);
		
		ColorPicker cp = new ColorPicker(color);
		cp.setAlignment(Pos.TOP_RIGHT);
		cp.setFillToParentWidth(true);
		
		out.set(cp.getColor().getVector());
		
		cp.setOnColorUpdate((e) -> {
			out.set(cp.getColor().getVector());
		});
		
		p.getChildren().add(cp);
	}

	private void label(Pane p, Object obj)
	{
		Label l = new Label(obj.toString());
		p.getChildren().add(l);
	}

	private void addMenuItem(Menu menu, String title, EventHandler<ActionEvent> event)
	{
		MenuItem me = new MenuItem(title);
		
		me.setOnAction(event);
		
		menu.getItems().add(me);
	}
	
	private void fillSelectionTab(Tab tab, int include)
	{
		ScrollPane p = new ScrollPane();
		p.setFillToParentWidth(true);
		
		VBox box = new VBox();
		box.setAlignment(Pos.TOP_LEFT);
		box.setFillToParentWidth(true);
		
		//
		
		Tilemap tileMap = scene.getMap().getTilemap();
		
		Set<Integer> tiles = tileMap.keys();
		Iterator<Integer> iter = tiles.iterator();
		
		iter.next();		// Skips air
		
		while(iter.hasNext())
		{
			int id = iter.next();
			Tile tile = tileMap.get(id);
			
			int flags = tile.getFlags();
			
			if (TileFlags.ILLEGAL.isSet(flags))
				continue;
			
			if ((flags & include) != 0)
			{
				Label l = new Label(id + " : " + tile.getName());
				l.setFillToParentWidth(true);
				box.getChildren().add(l);
				
				l.setMouseTransparent(false);
				l.setOnMouseClicked((event)-> {
					scene.setTile(id);
				});
			}
		}
		
		//
		
		p.setContent(box);
		
		tab.setContent(p);
	}
	
	private void fillSelectionTab(Tab tabGeneral, Tab tabTool, EntityLump[] entities)
	{
		ScrollPane p1 = new ScrollPane(), p2 = new ScrollPane();
		p1.setFillToParentWidth(true);
		p2.setFillToParentWidth(true);
		
		VBox boxGeneral = new VBox();
		VBox boxTool = new VBox();
		
		boxGeneral.setAlignment(Pos.TOP_LEFT);
		boxGeneral.setFillToParentWidth(true);
		
		boxTool.setAlignment(Pos.TOP_LEFT);
		boxTool.setFillToParentWidth(true);
		
		//
		
		int i = 0;
		for(EntityLump entity : entities)
		{
			Label l = new Label(i + " : " + entity.name);
			l.setFillToParentWidth(true);
			
			if (entity.mesh == null && entity.model == null)
				boxTool.getChildren().add(l);
			else
				boxGeneral.getChildren().add(l);
			
			l.setMouseTransparent(false);
			l.setOnMouseClicked((event)-> {
				scene.setPlaceEntity(entity);
			});
			
			if (i == 0)
				scene.setPlaceEntity(entity);
			i++;
		}
		
		//
		
		p1.setContent(boxGeneral);
		tabGeneral.setContent(p1);
		
		p2.setContent(boxTool);
		tabTool.setContent(p2);
	}

	private Button addImageButton(Pane pane, ToggleGroup g, Image img, int w, int h, EventHandler<ActionEvent> clickEvent)
	{
		return addImageButton(pane, g, img, w, h, clickEvent, null);
	}
	
	private Button addImageButton(Pane pane, ToggleGroup g, Image img, int w, int h, EventHandler<ActionEvent> clickEvent, String tooltip)
	{
		// Create a viewable pane for that image
		ImageView gfx = new ImageView();
		gfx.setPrefSize(w, h);
		gfx.setImage(img);
		gfx.setMaintainAspectRatio(true);

		Button button = g == null ? new StandaloneToggleButton("") : new ToggleButton("", g);
		button.setGraphic(gfx);
		button.setOnAction(clickEvent);
		
		pane.getChildren().add(button);
		
		if (tooltip != null)
		{
			ContextMenu menu = new ContextMenu();
			MenuItem mi = new MenuItem(tooltip);
			mi.setMouseTransparent(true);
			menu.getItems().add(mi);
			button.setContextMenu(menu);
			
			/*button.setOnMouseEntered(e -> {
				button.getContextMenu().setVisible(true);
				button.getContextMenu().show(button.getScene(), button.getX(), button.getY() + button.getHeight());
			});
			
			button.setOnMouseExited(e -> {
				button.getContextMenu().setVisible(false);
			});*/
		}


		return button;
	}

	public static boolean isValidFilePathFormat(String filePath) {
        // Regular expression for a valid file path
        String regex = "([a-zA-Z]:)?(\\\\[a-zA-Z0-9._-]+)+\\\\?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(filePath);
        return matcher.matches();
    }
	
	@Override
	public OpenGLPane getWorldRendererPane()
	{
		return oglPane;
	}

	@Override
	public void initImages()
	{
		selectToolImage = new Image("ui/editor/select-tool.jpg");
		cameraToolImage = new Image("ui/editor/camera-tool.jpg");
		entityToolImage = new Image("ui/editor/entity-tool.jpg");
		itemToolImage = new Image("ui/editor/item-tool.jpg");
		heightToolImage = new Image("ui/editor/height-tool.jpg");
		addChunkToolImage = new Image("ui/editor/add-chunk-tool.jpg");
		
		entBboxImage = new Image("ui/editor/ent_bbox.png");
		snapImage = new Image("ui/editor/snap.png");
		lockImage = new Image("ui/editor/lock.png");
	}
	
	public void setPropertyBox(int vis)
	{
		for(int i = 0; i < properties.length; i++)
			properties[i].setVisible(i == vis ? true : false);
	}
}
