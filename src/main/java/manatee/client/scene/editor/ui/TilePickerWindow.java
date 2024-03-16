package manatee.client.scene.editor.ui;

import lwjgui.LWJGUI;
import lwjgui.geometry.Pos;
import lwjgui.scene.Scene;
import lwjgui.scene.Window;
import lwjgui.scene.WindowHandle;
import lwjgui.scene.WindowManager;
import lwjgui.scene.WindowThread;
import lwjgui.scene.control.Label;
import lwjgui.scene.control.ScrollPane;
import lwjgui.scene.control.Tab;
import lwjgui.scene.control.TabPane;
import lwjgui.scene.layout.VBox;
import manatee.client.map.tile.Tile;
import manatee.client.map.tile.TileFlags;
import manatee.client.map.tile.Tilemap;
import manatee.client.scene.editor.EditorScene;

public class TilePickerWindow
{
	private static final int WIDTH = 300;
	private static final int HEIGHT = 500;

	private static EditorScene scene;
	
	private static TileVisualizer vis;
	
	private static Window window;
	private static boolean allowAllTiles = false;
	
	public static void reload(EditorScene scene)
	{

		TilePickerWindow.scene = scene;
		
		if (window == null)
			return;
	
		close();
		
		create(scene);
	}
	
	public static void setAllowAllTiles(boolean allow)
	{
		TilePickerWindow.allowAllTiles = allow;
		
		reload(scene);
	}
	
	static void create(EditorScene scene)
	{
		if (window != null)
			return;
		
		TilePickerWindow.scene = scene;
		
		WindowManager.runLater(() ->
		{
			long parentHandle = LWJGUI.getThreadWindow().getHandle();
			new WindowThread(WIDTH, HEIGHT, "Tileset Access", parentHandle, false)
			{
				
				@Override
				protected void setupHandle(WindowHandle handle)
				{
					super.setupHandle(handle);
					handle.canResize(false);
					handle.alwaysOnTop(true);
				}
				
				@Override
				protected void dispose()
				{
					super.dispose();
					vis.dispose();
					window = null;
					LWJGUI.removeThreadWindow();
				}

				@Override
				protected void init(Window window)
				{
					super.init(window);
					
					TilePickerWindow.window = window;
					
					VBox root = new VBox();
					
					TabPane tabs = new TabPane();
					tabs.setFillToParentHeight(false);
					tabs.setMaxHeight(180);
					tabs.setMouseTransparent(false);
					{
						addTab(tabs, "All", Integer.MAX_VALUE);
						addTab(tabs, "Nature", 1 << TileFlags.NATURE.ordinal());
						addTab(tabs, "Buildings", 1 << TileFlags.BUILDING.ordinal());
						addTab(tabs, "Misc.", 1 << TileFlags.MISCELLANEOUS.ordinal());
					}
					
					vis = new TileVisualizer(root, WIDTH, WIDTH);
					
					
					
					root.getChildren().add(tabs);

					// window.initNodeHierachy();
					
					window.setScene(new Scene(root, WIDTH, HEIGHT));
					window.show();
				}

				private void addTab(TabPane root, String title, int include)
				{
					Tab t = new Tab(title);
					t.setUserCanClose(false);
					
					fillSelectionTab(t, include);
					
					root.getTabs().add(t);
				}
				
			}.start();
		});
	}
	
	private static void fillSelectionTab(Tab tab, int include)
	{
		ScrollPane p = new ScrollPane();
		p.setFillToParentWidth(true);
		
		VBox box = new VBox();
		box.setAlignment(Pos.TOP_LEFT);
		box.setFillToParentWidth(true);
		
		Tilemap tilemap = scene.getMap().getTilemap();
		
		int nTiles = tilemap.getTileCount();
		
		for(int id = 1; id < nTiles; id++)
		{
			for(int subid = 0; subid < 0xFF; subid++)
			{
				Tile tile = tilemap.get(id, subid);
				
				if (subid != 0 && tile == Tile.MISSING)
					break;
				
				int flags = tile.getFlags();
				
				if (TileFlags.ILLEGAL.isSet(flags) && !allowAllTiles )
					continue;
				
				if ((flags & include) != 0)
				{
					String idStr = Integer.toHexString(id & 0xFFFFFF).toUpperCase();
					
					if (subid != 0)
					{
						idStr += ":" + subid;
					}
					
					Label l = new Label(idStr + "   " + tile.getName());
					l.setFillToParentWidth(true);
					box.getChildren().add(l);
					
					int tilemapIndex = id | (subid << 24);
					
					l.setMouseTransparent(false);
					l.setOnMouseClicked((event)-> {
						scene.setTile(tilemapIndex);
						vis.setTile(tilemap.get(tilemapIndex));
					});
				}
			}
		}
		
		p.setContent(box);
		
		tab.setContent(p);
	}

	public static void close()
	{
		if (window == null)
			return;
		
		window.close();
		window = null;
	}
}
