package manatee.client.scene.editor;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joml.Matrix3f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4i;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.qos.logback.classic.Logger;
import lwjgui.LWJGUI;
import lwjgui.geometry.Insets;
import lwjgui.paint.Color;
import lwjgui.scene.Scene;
import lwjgui.scene.Window;
import lwjgui.scene.WindowHandle;
import lwjgui.scene.WindowManager;
import lwjgui.scene.WindowThread;
import lwjgui.scene.control.Button;
import lwjgui.scene.control.CodeArea;
import lwjgui.scene.control.ColorPicker;
import lwjgui.scene.control.Label;
import lwjgui.scene.control.TextField;
import lwjgui.scene.layout.BorderPane;
import lwjgui.scene.layout.HBox;
import lwjgui.scene.layout.VBox;
import manatee.cache.definitions.Model;
import manatee.cache.definitions.binfile.BinaryMapFileWriter;
import manatee.cache.definitions.lump.EntityLump;
import manatee.cache.definitions.lump.LightLump;
import manatee.cache.definitions.lump.MapDataWrapper;
import manatee.cache.definitions.lump.MapInfoLump;
import manatee.cache.definitions.lump.RegionLump;
import manatee.cache.definitions.mesh.IMesh;
import manatee.cache.definitions.texture.ITexture;
import manatee.client.Client;
import manatee.client.dev.Command;
import manatee.client.dev.Dev;
import manatee.client.entity.Form;
import manatee.client.entity.SpatialEntity;
import manatee.client.entity.stock.TileEntity;
import manatee.client.entity.stock.editor.EditorEntity;
import manatee.client.entity.stock.editor.EditorLightEntity;
import manatee.client.gl.camera.ControllableCamera;
import manatee.client.gl.camera.FloatingCamera;
import manatee.client.gl.renderer.nvg.NVGText;
import manatee.client.input.Input;
import manatee.client.input.KeybindsInternal;
import manatee.client.map.MapGeometry;
import manatee.client.map.MapRegion;
import manatee.client.map.tile.Tile;
import manatee.client.map.tile.TileFlags;
import manatee.client.scene.Assets;
import manatee.client.scene.GameMap;
import manatee.client.scene.GlobalAssets;
import manatee.client.scene.MapScene;
import manatee.client.scene.editor.history.ActionHistory;
import manatee.client.scene.editor.history.DeltaHeightAction;
import manatee.client.scene.editor.history.HeightFieldAction;
import manatee.client.scene.editor.history.IHeightAction;
import manatee.client.scene.editor.history.ModifyHeightAction;
import manatee.client.scene.editor.history.PlaceEntitiesAction;
import manatee.client.scene.editor.history.PlaceTilesAction;
import manatee.client.scene.editor.history.ReversableAction;
import manatee.client.scene.editor.ui.EditorUIBuilder;
import manatee.client.scene.editor.ui.TilePickerWindow;
import manatee.client.ui.ClientUI;
import manatee.client.ui.FileChooser;
import manatee.client.ui.UIBuilder;
import manatee.maths.Maths;
import manatee.maths.Vectors;
import manatee.maths.geom.Plane;
import manatee.primitives.Primitive;
import manatee.primitives.Primitives;

/**
 * A mess of a class. This, and its UI builder. REALLY need refactoring. But
 * this is for speeding up development, so for now it will stay a mess.
 *
 */
public class EditorScene extends MapScene
{
	private final static Logger ioLogger = (Logger) LoggerFactory.getLogger("IO");

	private String mapName = "";

	private Vector3f hoveredCentered = new Vector3f(Float.NaN), hoveredBounds = new Vector3f();
	private Matrix3f hoverMatrix = new Matrix3f();

	private NVGText hoverLabel;

	private EditorTool tool = EditorTool.SELECT;

	private EntityLump placeEntity;

	private int tileID = 1;

	private float heightDelta = 0.5f;
	private float heightRadius = 4f;
	private HeightToolMode heightMode = HeightToolMode.RAISE;
	private Vector4i heightFieldPlaceBounds = new Vector4i(0, 0, 0, 0);

	private long lastTick = System.currentTimeMillis();

	private final long UPDATE_MS = 25;

	private Primitive hover;
	private Primitive placementCursor;

	public Map<SpatialEntity, Primitive> selected = new HashMap<>();

	private ActionHistory history;
	private ReversableAction currentAction = null;
	private String seed = "";

	private MapRegion selectedRegion = null;

	private EditorAssets assets;

	private Transformer transformer;

	private ColorPicker entityColorPicker;

	private CodeArea entityTagEditor;

	protected int ioHack;

	private EditorEntity lastEntityToEditTags;

	protected String exportPath;

	private VBox[] propertiesTabs;

	public boolean placeEntOnGround = true;
	public boolean snapToGrid;
	public boolean axisLock;
	private boolean lastCamControlState, overrideCamControlState;
	private boolean heightSet = false;
	private boolean cursorIgnoresHeight = false;

	private int gridSize = 1;

	private Vector3f placeAxis = new Vector3f();

	private boolean canPlaceChunk;

	
	private static final String KEY_PATTERN = ".*(?=\\s*:)";
	private static final String STRING_PATTERN = "\\\"(.*?)\\\"";
	private static final String VALUE_PATTERN = "[^\\s]+";//"(?<=:).*(?!<\")\\b\\w+\\b(?!\")";
	
	public static final Pattern PATTERN = Pattern.compile(
			"(?<KEY>" + KEY_PATTERN + ")"
			+ "|(?<STRING>" + STRING_PATTERN + ")"
			+ "|(?<VALUE>" + VALUE_PATTERN + ")"
	);

	public static final Color DEFAULT_SKY_COLOR = new Color(59 / 255f, 137 / 255f, 196 / 255f);

	@Override
	public void init(ClientUI ui)
	{
		super.init(ui);
		
		Command.add("editor_set_tile", Command.INT_SYNTAX, this, "setTile", true);

		setCamera(new FloatingCamera());

		hover = Primitives.addBox(hoveredCentered, hoveredBounds);
		placementCursor = Primitives.addBox(new Vector3f(), new Vector3f());

		history = new ActionHistory(50);

		ui.getRenderer().setDebugSceneInfo(true);

		transformer = new Transformer(mouseRay);
		
		this.entitySystem = new EditorEntitySystem(map.getGeometry(), lightColor, lightVector);
		
		GameMap.renderSkipped = true;
	}

	@Override
	public void tick()
	{
		super.tick();
		
		boolean ctrlHeld = Input.isHeld(KeybindsInternal.CTRL);
		boolean shiftHeld = Input.isHeld(KeybindsInternal.SHIFT);
		
		MapGeometry geom = map.getGeometry();

		boolean mouseOverWorldView = false;
		
		int tileFlags = geom.getTilemap().get(tileID).getFlags();
		cursorIgnoresHeight = false;

		if (TileFlags.IGNORE_HEIGHTFIELD.isSet(tileFlags))
		{
			float intersect = new Plane(Vectors.EMPTY, Vectors.Z_AXIS).raycast(camera.getPosition(), mouseRay);
			mouseTerrainPos.set(mouseRay).mul(intersect).add(camera.getPosition());
			cursorIgnoresHeight = true;
			
			// 8x8 hack to make placing water less painless
			boolean isEightByEight = geom.getTilemap().get(tileID).getName().contains("8x8");
			
			if (tool == EditorTool.ITEM && isEightByEight)
			{
				mouseTerrainPos.x = Math.floorDiv(Maths.floor(mouseTerrainPos.x), 8) * 8;
				mouseTerrainPos.y = Math.floorDiv(Maths.floor(mouseTerrainPos.y), 8) * 8;
			}
			
			mouseWorldPos.set(mouseTerrainPos);
		}

		/// TODO: Get rid of this
		switch(ioHack)
		{
		case 1:
			open(getMapName());
			ioHack = 0;
			break;
		case 2:
			save(getMapName(), false);
			ioHack = 0;
			break;
		case 3:
			reset(true);
			ioHack = 0;
			break;
		case 4:
			save(exportPath, true);
			ioHack = 0;
			break;
		}
		/////
		
		
		if (LWJGUI.getThreadWindow().getContext().getHovered() != null)
			mouseOverWorldView = LWJGUI.getThreadWindow().getContext().getHovered().getElementType().equals("openglpane");
		
		if (!mouseOverWorldView)
			return;
		
		MapRegion region = geom.getRegionAt(mouseTerrainPos.x, mouseTerrainPos.y);
		
		// Translation tool
		transformer.transform(camera);
		
		if (ctrlHeld)
		{
			if (Input.isPressed(KeybindsInternal.Z))
			{
				history.undo();
				auditSelected();
			}
			
			if (Input.isPressed(KeybindsInternal.Y))
			{
				history.redo();
			}
			
			if (Input.isPressed(KeybindsInternal.S))
				queryFilePopup(this, true, false);
			
			if (Input.isPressed(KeybindsInternal.O))
				queryFilePopup(this, false, false);
			
			if (Input.isPressed(KeybindsInternal.E))
				queryExportPopup(this);
			
			if (Input.isPressed(KeybindsInternal.N))
				newMapPopup(this);
			
			if (!overrideCamControlState && camera instanceof ControllableCamera)
			{	
				ControllableCamera cam = ((ControllableCamera) camera);
				lastCamControlState = cam.isDraggable();
				overrideCamControlState = true;
				cam.setDraggable(false);
			}
		}
		else
		{
			transformer.update(camera, selected.keySet(), history, nvgObjects);
			
			if (overrideCamControlState && camera instanceof ControllableCamera)
			{	
				ControllableCamera cam = ((ControllableCamera) camera);
				overrideCamControlState = false;
				cam.setDraggable(lastCamControlState);
			}
		}
		
		if (Input.isPressed(KeybindsInternal.LMB))
		{
			transformer.reset(history, nvgObjects);
				
			if (!selected.containsKey(this.hoveredEntity))
			{
				select(ctrlHeld || shiftHeld, hoveredEntity);
			}
			else if (hoveredEntity != null)
			{
				Primitive prim = selected.remove(hoveredEntity);
				Primitives.remove(prim);
			}
			
			if (tool == EditorTool.CHUNK)
			{
				selectedRegion = map.getGeometry().getRegionAt(mouseTerrainPos.x, mouseTerrainPos.y);
			}
		}

		if (Input.isPressed(KeybindsInternal.DELETE))
		{
			for(SpatialEntity entity : selected.keySet())
			{
				MapRegion regionAtSelection = geom.getRegionAt(entity.getPosition().x, entity.getPosition().y);
				
				
				if (regionAtSelection != null && entity instanceof TileEntity)
				{
					PlaceTilesAction action = new PlaceTilesAction((EditorEntitySystem) entitySystem, entity, geom, regionAtSelection,
							0, entity.getPosition().x, entity.getPosition().y);
					//action.act();
					
					history.commit(action);
					
				}
				else
				{
					PlaceEntitiesAction action = new PlaceEntitiesAction(entitySystem, null, entity);
					action.act();
					
					history.commit(action);
				}
			}
			
			if (selectedRegion != null && tool == EditorTool.CHUNK)
			{
				HeightFieldAction hfa = new HeightFieldAction(geom, null, selectedRegion);
				hfa.act();
				history.commit(hfa);
				selectedRegion.buildAllMeshes(geom, null);
			}
			
			for(Primitive prim : selected.values())
				Primitives.remove(prim);
		}
		
		boolean rmbPressed = Input.isPressed(KeybindsInternal.RMB);
		
		if (rmbPressed && axisLock)
		{
			placeAxis.set(Float.NaN);
			if (Math.abs(camera.getLookVector().y) > Math.abs(camera.getLookVector().x) )
				placeAxis.y = tool == EditorTool.ENTITY ? mouseWorldPos.y : mouseTerrainPos.y;
			else
				placeAxis.x = tool == EditorTool.ENTITY ? mouseWorldPos.x : mouseTerrainPos.x;
			
			Dev.log(placeAxis.x , placeAxis.y);
		}
		
		if (Input.isHeld(KeybindsInternal.RMB))
		{
			boolean turboClick = false;
			
			if (shiftHeld && System.currentTimeMillis() - lastTick >= UPDATE_MS)
			{
				turboClick = true;
				lastTick = System.currentTimeMillis();
			}

			if (rmbPressed || turboClick)
			{
				
				// Rmb press
				switch(tool)
				{
				case SELECT:
					break;
				case ITEM:
				{
					if (region == null)
						break;
					
					if (axisLock)
					{
						if (!Float.isNaN(placeAxis.x))
							mouseTerrainPos.x = placeAxis.x;
						
						if (!Float.isNaN(placeAxis.y))
							mouseTerrainPos.y = placeAxis.y;
					}

					int res = geom.getSpacing();
					float dx = Math.floorDiv(Maths.floor(mouseTerrainPos.x), res) * res;
					float dy = Math.floorDiv(Maths.floor(mouseTerrainPos.y), res) * res;	
					dx += res / 2f;
					dy += res / 2f;
					
					SpatialEntity entity = ((EditorEntitySystem) entitySystem).getTileEntityAt(dx, dy);
					
					if (currentAction == null)
					{
						// FIXME: When a new tf is made, and we undo it after holding ctrl and drawing
						// a line of entities across the border, it breaks.
						currentAction = new PlaceTilesAction((EditorEntitySystem) entitySystem, entity, geom, region,
								tileID, mouseTerrainPos.x, mouseTerrainPos.y);
						
						history.commit(currentAction);
					}
					else
					{
						((PlaceTilesAction)currentAction).add(region, entity,
								tileID, mouseTerrainPos.x, mouseTerrainPos.y);
					}

					break;
				}
				case ENTITY:
				{
					EditorEntity entity;
					
					if (axisLock)
					{
						if (!Float.isNaN(placeAxis.x))
							mouseWorldPos.x = placeAxis.x;
						
						if (!Float.isNaN(placeAxis.y))
							mouseWorldPos.y = placeAxis.y;
					}
					
					if (placeEntity.name.endsWith("Light"))
					{
						entity = new EditorLightEntity(this, placeEntity.name, mouseWorldPos, placeEntity.color, 
								getMesh(placeEntity.mesh), getTexture(placeEntity.texture), getModel(placeEntity.model));
					}
					else
					{
						entity = new EditorEntity(this, placeEntity.name, mouseWorldPos, placeEntity.color, 
									getMesh(placeEntity.mesh), getTexture(placeEntity.texture), getModel(placeEntity.model), placeEntity.shader);
					}
					
					entity.setGraphicReferences(placeEntity.mesh, placeEntity.texture, placeEntity.model, placeEntity.lod);
					entity.setTags(placeEntity.tags);
					entity.setLodDistanceSqr(placeEntity.lodDistanceSqr);
					
					if (placeEntOnGround)
						entity.getPosition().z += entity.getBoundingBox().halfExtents.z;
					
					if (snapToGrid)
					{
						Vector3f pos = entity.getPosition();
						pos.x = Math.floorDiv(Maths.floor(pos.x), this.gridSize) * gridSize;
						pos.y = Math.floorDiv(Maths.floor(pos.y), this.gridSize ) * gridSize;
						
						pos.x += gridSize / 2f;
						pos.y += gridSize / 2f;
						
						// Raycast against tile entities
						SpatialEntity e = entitySystem.raycastEntities(camera.getViewMatrix(), camera.getPosition(), mouseRay, mouseWorldPos);
						
						if (hoveredEntity != e)
						{
							hoveredEntity = e;
							onMouseHoverChange();
						}
						
						Vector3f camLook = Vectors.X_AXIS;
						
						// Eugh
						if (camera.getLookVector().x <= -0.5)
							camLook = Vectors.NEG_X_AXIS;
						if (camera.getLookVector().y >= 0.5)
							camLook = Vectors.Y_AXIS;
						if (camera.getLookVector().y <= -0.5)
							camLook = Vectors.NEG_Y_AXIS;
						if (camera.getLookVector().z >= 0.5)
							camLook = Vectors.Z_AXIS;
						if (camera.getLookVector().z <= -0.5)
							camLook = Vectors.NEG_Z_AXIS;
						
						if (hoveredEntity instanceof EditorEntity && ((EditorEntity)hoveredEntity).getName().equals(placeEntity.name))
						{
							break;
						}
						
						float angle = (float) Math.atan2(camLook.y, camLook.x) - Maths.HALFPI;
						entity.getRotation().rotateZ(angle);
					}
					else
					{
						float angle = (float) Math.atan2(mouseRay.y, mouseRay.x) - Maths.HALFPI;
						entity.getRotation().rotateZ(angle);
					}
					
					if (entity instanceof Form)
						entitySystem.addForm((Form) entity);
					else
						entitySystem.addEntity(entity);
					
					select(shiftHeld, entity);
					
					if (currentAction == null)
					{
						currentAction = new PlaceEntitiesAction(this.entitySystem, entity, null);
						history.commit(currentAction);
					}
					else
					{
						((PlaceEntitiesAction)currentAction).add(entity, null);
					}
					break;
				}
				case CHUNK:
				{

					if (!shiftHeld)
					{
						
						if (heightFieldPlaceBounds.equals(0, 0, 0, 0))
						{
							int size = (geom.getResolution() - 1) * geom.getSpacing();
							
							int x = Math.floorDiv(Maths.floor(mouseWorldPos.x), size) * size;
							int y = Math.floorDiv(Maths.floor(mouseWorldPos.y), size) * size;
							
							heightFieldPlaceBounds.x = x;
							heightFieldPlaceBounds.y = y;
						}
						
						
						if (canPlaceChunk) //geom.getRegionAt(mouseWorldPos.x, mouseWorldPos.y) == null
						{
							Vector2i pos = new Vector2i(heightFieldPlaceBounds.x, heightFieldPlaceBounds.y);
							int res = geom.getResolution();
							int spacing = geom.getSpacing();
							
							MapRegion r = geom.addRegion(pos, new float[res][res], new int[res-1][res-1], res, res, spacing);
							
							history.commit(new HeightFieldAction(geom, r, null));
							r.buildAllMeshes(geom, null);
						}
					}
					break;
				}
				
				default:
				}
			}
			else 
			{
				// Rmb held
				if (tool == EditorTool.HEIGHT)
				{
					if ((heightMode != HeightToolMode.SMOOTH && heightMode != HeightToolMode.FLATTEN)
							|| region != null)
					{
						
						if (currentAction == null)
						{
							// DeltaHeightAction is optimized for these types of action
							if (heightMode == HeightToolMode.RAISE || heightMode == HeightToolMode.LOWER)
							{
								currentAction = new DeltaHeightAction(geom, heightDelta, heightRadius, geom.getSpacing(), heightMode);
								history.commit(currentAction);
							}
							else
							{
								currentAction = new ModifyHeightAction(geom, heightRadius, geom.getSpacing(), heightMode);
								history.commit(currentAction);
							}
						}
						else
						{
							((IHeightAction)currentAction).addChange(mouseWorldPos.x, mouseWorldPos.y, heightDelta);
						}
					}
					
					heightSet = true;
				}
				
				/*if (tool == EditorTool.CHUNK && shiftHeld)
				{
					int spacing = geom.getSpacing();
					int x = Math.floorDiv(Maths.floor(mouseTerrainPos.x), spacing) * spacing;
					int y = Math.floorDiv(Maths.floor(mouseTerrainPos.y), spacing) * spacing;	
					
					if (heightFieldPlaceBounds.x == 0)
					{
						heightFieldPlaceBounds.x = x;
						heightFieldPlaceBounds.y = y;
					}
					else
					{
						heightFieldPlaceBounds.z = x;
						heightFieldPlaceBounds.w = y;
					}
					
					int halfW = (heightFieldPlaceBounds.z - heightFieldPlaceBounds.x) / 2;
					int halfH = (heightFieldPlaceBounds.w - heightFieldPlaceBounds.y) / 2;
					
					placementCursor.getStart().set(heightFieldPlaceBounds.x + halfW, heightFieldPlaceBounds.y + halfH, 0);	// Orig
					placementCursor.getEnd().set(halfW, halfH, 1f);			// HalfSizes
				}*/
			}
		}
		else
		{
			if (tool == EditorTool.HEIGHT && currentAction != null)
			{
				((IHeightAction)currentAction).updateTilesets();
			}
			
			currentAction = null;
			
			if (tool == EditorTool.CHUNK)
			{
				//if (!shiftHeld)
				//{
					if (selectedRegion != null)
					{
						int res = geom.getSpacing();
						float halfRes = geom.getResolution() / 2f;
						
						float dx = Math.floorDiv(Maths.floor(selectedRegion.getPosition().x), res) * res;
						float dy = Math.floorDiv(Maths.floor(selectedRegion.getPosition().y), res) * res;	
						dx += halfRes;
						dy += halfRes;
						
						float height = cursorIgnoresHeight ? 0f : geom.getHeightAt(dx, dy);
						
						placementCursor.getStart().set(dx, dy, height);
						placementCursor.getEnd().set(halfRes, halfRes, halfRes);
					}
					else if (heightFieldPlaceBounds.w != 0)
					{
						
						Vector4i finalBounds = new Vector4i(heightFieldPlaceBounds);
						
						int spacing = geom.getSpacing();
						int w = Math.abs(finalBounds.z - finalBounds.x) / spacing;
						int h = Math.abs(finalBounds.w - finalBounds.y) / spacing;
						
						if (w > 0 && h > 0)
						{
							w++;
							h++;
							
							if (finalBounds.z < finalBounds.x)
								finalBounds.x = finalBounds.z;
							
							if (finalBounds.w < finalBounds.y)
								finalBounds.y = finalBounds.w;

							Vector2i pos = new Vector2i(finalBounds.x, finalBounds.y);
							MapRegion r = geom.addRegion(pos, new float[w][h], new int[w][h], w, h, spacing);
							
							history.commit(new HeightFieldAction(geom, r, null));
							
							placementCursor.getStart().zero();
							placementCursor.getEnd().zero();
							
							heightFieldPlaceBounds.zero();
						}
					}
					
					//placementCursor.getColor().z = 0.5f + ((float)Math.sin(System.currentTimeMillis() / 200.0) / 2f);
					
					if (region == null)
					{
						canPlaceChunk = true;
						placementCursor.setColor(Vectors.XY_AXIS);
						
						int size = (geom.getResolution() - 1) * geom.getSpacing();
						
						int x = Math.floorDiv(Maths.floor(mouseWorldPos.x), size) * size;
						int y = Math.floorDiv(Maths.floor(mouseWorldPos.y), size) * size;
						
						int halfSize = size / 2;  

						placementCursor.getStart().set(x + halfSize, y + halfSize, 0f);
						placementCursor.getEnd().set(halfSize, halfSize, 1f);
					}
					else
					{
						canPlaceChunk = false;
						//placementCursor.getStart().zero();
						//placementCursor.getEnd().zero();
						placementCursor.setColor(Vectors.X_AXIS);
						
						heightFieldPlaceBounds.zero();
						
					}
				//}
			}
			else if (Float.isNaN(this.hoveredCentered.x))
			{
				int res = geom.getSpacing();
				float halfRes = res / 2f;
				
				float dx = Math.floorDiv(Maths.floor(mouseTerrainPos.x), res) * res;
				float dy = Math.floorDiv(Maths.floor(mouseTerrainPos.y), res) * res;	
				dx += halfRes;
				dy += halfRes;
				
				float height = cursorIgnoresHeight ? 0f : geom.getHeightAt(dx, dy);
				
				placementCursor.getStart().set(dx, dy, height);
				placementCursor.getEnd().set(halfRes, halfRes, halfRes);
			}
			else
			{
				placementCursor.getStart().zero();
				placementCursor.getEnd().zero();
			}
		}
	}

	private void auditSelected()
	{
		Iterator<java.util.Map.Entry<SpatialEntity, Primitive>> iter = selected.entrySet().iterator();
		while(iter.hasNext())
		{
			java.util.Map.Entry<SpatialEntity, Primitive> e = iter.next();
			
			if (entitySystem.getForms().contains(e.getKey()))
				continue;
			
			if (entitySystem.getNonStaticEntities().contains(e.getKey()))
				continue;
			
			Primitive prim = e.getValue();
			Primitives.remove(prim);
			
			iter.remove();
		}
	}

	private void select(boolean ctrlShiftHeld, SpatialEntity entity)
	{
		if (!ctrlShiftHeld)
		{
			for(Primitive p : selected.values())
				Primitives.remove(p);
			
			selected.clear();
		}
		
		if (entity == null)
		{
			lastEntityToEditTags = null;
			entityTagEditor.setText("");
			return;
		}
		
		if (entity instanceof EditorEntity)
		{
			EditorEntity editorEntity = ((EditorEntity)entity);
			Vector4f hc = editorEntity.getColor();
			
			entityColorPicker.setColor(new Color(hc.x, hc.y, hc.z));
			
			
			setPropertyTab(2);
			
			// If we have an entity from before, populate their tags
			if (getLastEntityToEditTags() != null)
			{
				Matcher matcher = EditorScene.PATTERN.matcher(entityTagEditor.getText());
				
				Map<String, String> tags = new HashMap<>();
				//tags.clear();
				
				String key = null;
				
				while ( matcher.find() ) {
					if ( matcher.group("KEY") != null && !matcher.group("KEY").equals("") )
					{
						key = matcher.group();
					}
					else if (key != null)
					{
						if ( matcher.group("STRING") != null ) {
							tags.put(key, matcher.group());
							key = null;
						} else if ( matcher.group("VALUE") != null ) {
							tags.put(key, matcher.group());
							key = null;
						}
					}
				}

				getLastEntityToEditTags().setTags(tags);
			}
			
			// Populate UI tag editor w/ new selected entity's info
			StringBuilder sb = new StringBuilder();
			for(java.util.Map.Entry<String, String> entry : editorEntity.getTags().entrySet())
				sb.append(entry.getKey())
				.append(": ")
				.append(entry.getValue())
				.append("\n");
			
			entityTagEditor.setText(sb.toString());
			lastEntityToEditTags = editorEntity;
		}
		
		hoverMatrix.identity();
		entity.getRotation().get(hoverMatrix).invert();
		hover.getRotation().set(hoverMatrix);
		
		Primitive prim = Primitives.addBox(new Vector3f(entity.getPosition()), new Vector3f(entity.getBoundingBox().halfExtents), Vectors.X_AXIS);
		prim.getRotation().set(hoverMatrix);
	
		selected.put(entity, prim);
	}

	private void setPropertyTab(int vis)
	{
		for(int i = 0; i < propertiesTabs.length; i++)
			propertiesTabs[i].setVisible(i == vis ? true : false);
	}

	@Override
	protected void onMouseHoverChange()
	{

		if (hoveredEntity == null)
		{
			hoveredCentered.set(Float.NaN);
			hoveredBounds.zero();

			if (hoverLabel != null)
				removeText(hoverLabel);
		}
		else
		{
			removeText(hoverLabel);

			hoveredCentered.set(hoveredEntity.getBoundingBox().center);
			hoveredBounds.set(hoveredEntity.getBoundingBox().halfExtents);

			String text;

			if (hoveredEntity instanceof EditorEntity)
				text = ((EditorEntity) hoveredEntity).getName();
			else
				text = ((TileEntity) hoveredEntity).getTile().getName();

			hoverLabel = addText(text, hoveredEntity.getPosition());

			hoverMatrix.identity();
			hoveredEntity.getRotation().get(hoverMatrix).invert();
			hover.getRotation().set(hoverMatrix);
		}
	}

	@Override
	public UIBuilder createUIBuilder()
	{
		return new EditorUIBuilder();
	}

	@Override
	protected Assets createAssets()
	{
		assets = new EditorAssets();
		return assets;
	}

	public void save(String filePath, boolean asBinary)
	{
		MapGeometry geom = this.getMap().getGeometry();
		// TODO: Move this to a new Lump staging class
		try
		{
			Gson gson = new GsonBuilder().setPrettyPrinting().create();

			MapInfoLump mapInfo = new MapInfoLump(getMapName(), geom.getTilemap().getTileset(),
					geom.getResolution(), geom.getSpacing(), this.getColor(), this.getLightColor(),
					this.getColor());

			RegionLump[] regLump = RegionLump.load(this);
			EntityLump[] entitiesArr = EntityLump.load(this);
			LightLump[] lightsArr = LightLump.load(this);

			if (asBinary)
			{
				BinaryMapFileWriter bMapFilewriter = new BinaryMapFileWriter(filePath);
				
				bMapFilewriter.addLumps(mapInfo, regLump, lightsArr, entitiesArr);
				
				bMapFilewriter.write();
				
				return;
			}
			
			FileWriter fw = new FileWriter(filePath);
			
			MapDataWrapper mapData = new MapDataWrapper(mapInfo, regLump, lightsArr, entitiesArr);
			
			gson.toJson(mapData, fw);

			fw.close();

			ioLogger.info("Saved to: " + filePath);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return;
		}
	}

	public void open(String filePath)
	{
		resetSun();
		
		MapGeometry geom = this.getMap().getGeometry();

		try
		{
			Path path = Paths.get(filePath);
			
			String json = Files.readString(path, StandardCharsets.UTF_8);
			
			reset(false);

			Gson gson = new GsonBuilder().create();

			MapDataWrapper mapData = gson.fromJson(json, MapDataWrapper.class);

			MapInfoLump mapInfo = mapData.mapInfo;

			this.setMapName(mapInfo.mapName);
			this.getColor().set(mapInfo.color);
			this.getLightColor().set(mapInfo.lightColor);
			this.getColor().set(mapInfo.waterColor);

			setTileset(mapInfo.tileset);
			geom.setSpacing(mapInfo.spacing);
			geom.setResolution(mapInfo.resolution);

			// Height/tile field data
			RegionLump[] tfDataArr = mapData.regData;

			for (RegionLump regData : tfDataArr)
			{
				Vector2i pos = regData.position;

				final float[][] heights = regData.heights;
				final int[][] tiles = regData.tiles;
				final int xRes = regData.xResolution;
				final int yRes = regData.yResolution;
				final int spacing = regData.spacing;

				MapRegion region = geom.addRegion(pos, heights, tiles, xRes, yRes, spacing);
				
				// Tile entities (not saved, just regenerate)
				calcTileEntities(region);
			}

			geom.buildAllMeshes();
			
			// Entities
			EntityLump[] entities = mapData.entities;

			for (EntityLump entityData : entities)
			{
				EditorEntity entity;
				
				if(entityData.name.endsWith("Light"))
				{
					entity = new EditorLightEntity(this, entityData.name, entityData.position,
							entityData.color, getMesh(entityData.mesh), getTexture(entityData.texture),
							getModel(entityData.model));
				} else
				{
					entity = new EditorEntity(this, entityData.name, entityData.position,
								entityData.color, getMesh(entityData.mesh), getTexture(entityData.texture),
								getModel(entityData.model), entityData.shader);
				}

				entity.setGraphicReferences(entityData.mesh, entityData.texture, entityData.model, entityData.lod);
				entity.setTags(entityData.tags);
				entity.getRotation().set(entityData.rotation);
				entity.getScale().set(entityData.scale);
				entity.setLodDistanceSqr(entityData.lodDistanceSqr);

				entity.onTranslate();
				entity.onRotate();
				entity.onScale();
				entity.onColorChange();

				entitySystem.addForm(entity);
			}

			ioLogger.info("Loaded: " + filePath);
		}
		catch (NoSuchFileException e)
		{
			ioLogger.error("File not found: " + filePath);
			return;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return;
		}
	}

	private void calcTileEntities(MapRegion region)
	{
		int[][] tiles = region.getTileData().get();
		Vector2i pos = region.getPosition();
		int spacing = region.getSpacing();
		
		MapGeometry geom = this.map.getGeometry();
		
		for(int i = 0; i < tiles.length; i++)
		{
			for(int j = 0; j < tiles[0].length; j++)
			{
				if (tiles[i][j] == 0)
					continue;

				int tileX = pos.x + (i*spacing);
				int tileY = pos.y + (j*spacing);
				TileEntity tileEntity = new TileEntity(tileX, tileY, 0, spacing, geom.getTilemap().get(tiles[i][j]));
				tileEntity.getPosition().z = geom.getHeightAt(tileX, tileY) + tileEntity.getBoundingBox().halfExtents.z;	
				((EditorEntitySystem) entitySystem).addTileEntity(tileEntity);
				
			}
		}
	}
	
	public void recalcTileEntities()
	{
		EditorEntitySystem ees = ((EditorEntitySystem) entitySystem);
		
		ees.clearTileEntities();
		
		for(MapRegion region : this.getMap().getGeometry().getMapRegions())
		{
			calcTileEntities(region);
		}
	}

	private IMesh getMesh(String mesh)
	{
		if (mesh == null)
			return null;

		return this.assets.getMesh(mesh);
	}

	private ITexture getTexture(String texture)
	{
		if (texture == null)
			return GlobalAssets.NO_TEX;

		return this.assets.getTexture(texture);
	}

	private Model getModel(String model)
	{
		if (model == null)
			return null;
		
		return this.assets.getModel(model);
	}

	public void reset(boolean withDefaultField)
	{
		MapGeometry geom = this.getMap().getGeometry();
		
		// Reset info
		this.setMapName("");

		geom.reset(assets, withDefaultField);
		
		history.clear();

		entitySystem.clear();
		
		resetSun();
	}

	private void resetSun()
	{
		lightColor.set(1,1,1);
		lightVector.set(DEFAULT_SUN_VECTOR);
		this.getColor().set(DEFAULT_SKY_COLOR.getVector());
	}

	public void setTool(EditorTool tool)
	{
		this.tool = tool;
	}

	public EditorTool getTool()
	{
		return tool;
	}

	public void setTile(int id)
	{
		this.tileID = id;
	}

	public float getHeightDelta()
	{
		return heightDelta;
	}

	public float getHeightRadius()
	{
		return heightRadius;
	}

	public void setHeightRadius(float value)
	{
		this.heightRadius = value;
	}

	public void setHeightDelta(float value)
	{
		this.heightDelta = value;
	}

	public ActionHistory getHistory()
	{
		return history;
	}

	public void getHeightMode(HeightToolMode mode)
	{
		this.heightMode = mode;
	}

	public void getSeed(String seed)
	{
		this.seed = seed;
	}

	public String getMapName()
	{
		return mapName;
	}

	public void setMapName(String mapName)
	{
		this.mapName = mapName;
	}

	public void setPlaceEntity(EntityLump entity)
	{
		this.placeEntity = entity;
	}

	public Set<SpatialEntity> getSelected()
	{
		return this.selected.keySet();
	}


	public void setEntityColorPicker(ColorPicker entityColorPicker)
	{
		this.entityColorPicker = entityColorPicker;
	}

	public void setEntityTagEditor(CodeArea entityTagEditor)
	{
		this.entityTagEditor = entityTagEditor;
	}
	
	public static void queryFilePopup(EditorScene scene, boolean isSave, boolean alwaysShowDirectory)
	{

		if (!alwaysShowDirectory && isSave && scene.getMapName() != null && !scene.getMapName().equals(""))
		{
			scene.save(scene.getMapName(), false);

			return;
		}
		
		FileChooser.create(isSave ? "Save" : "Open", "json", !isSave, "src/main/resources/maps", (e) -> {
			String str = FileChooser.fileName;

			if (!str.endsWith(".json"))
				if (str.lastIndexOf('.') != -1)
					str = str.substring(0, str.lastIndexOf('.')) + ".json";
				else
					str += ".json";

			str = "src/main/resources/maps/" + str;

			scene.setMapName(str);

			if (isSave)
				scene.ioHack = 2;
			else
				scene.ioHack = 1;
			
		});
					

		/*WindowManager.runLater(() ->
		{
			new WindowThread(500, 150, isSave ? "Save" : "Open")
			{
				@Override
				protected void setupHandle(WindowHandle handle)
				{
					super.setupHandle(handle);
					handle.canResize(false);
				}

				@Override
				protected void init(Window window)
				{
					super.init(window);

					BorderPane root = new BorderPane();
					root.setPadding(new Insets(32, 32, 32, 32));

					HBox box = new HBox();
					box.setSpacing(8f);
					box.setFillToParentWidth(true);
					Label l = new Label("Filename:");
					TextField ta = new TextField("map.json");
					ta.setFillToParentWidth(true);

					box.getChildren().add(l);
					box.getChildren().add(ta);

					root.setCenter(box);

					Button btn = new Button(isSave ? "Save" : "Open");
					btn.setOnAction((event) ->
					{

						String str = ta.getText();

						if (!str.endsWith(".json"))
							if (str.lastIndexOf('.') != -1)
								str = str.substring(0, str.lastIndexOf('.')) + ".json";
							else
								str += ".json";

						str = "src/main/resources/maps/" + str;

						scene.setMapName(str);

						if (isSave)
							scene.ioHack = 2;
						else // if (Files.exists(new File(str).toPath()))
							scene.ioHack = 1;

						window.close();
					});

					root.setBottom(btn);

					window.setScene(new Scene(root, 500, 150));
					window.show();
				}
			}.start();
		});*/
	}

	public static void newMapPopup(EditorScene scene) {

		WindowManager.runLater(() ->
		{
			new WindowThread(500, 150, "New")
			{
				@Override
				protected void setupHandle(WindowHandle handle)
				{
					super.setupHandle(handle);
					handle.canResize(false);
				}

				@Override
				protected void init(Window window)
				{
					super.init(window);

					BorderPane root = new BorderPane();
					root.setPadding(new Insets(32, 32, 32, 32));

					Label l = new Label("Open new map?");
					root.setCenter(l);

					HBox box = new HBox();
					box.setSpacing(12f);
					root.setBottom(box);

					Button yes = new Button("Yes");
					yes.setOnAction((event) ->
					{

						scene.ioHack = 3;
						window.close();
					});

					Button no = new Button("Cancel");
					no.setOnAction((event) ->
					{

						window.close();
					});

					box.getChildren().add(yes);
					box.getChildren().add(no);

					// Display window
					window.setScene(new Scene(root, 500, 150));
					window.show();
				}
			}.start(); // Start the ManagedThread
		});
	}
	
	public static void queryExportPopup(EditorScene scene) {
		
		WindowManager.runLater(() -> {
			new WindowThread(500, 150, "Export") {
				@Override
				protected void setupHandle(WindowHandle handle) {
					super.setupHandle(handle);
					handle.canResize(false);
				}
				@Override
				protected void init(Window window) {
					super.init(window);
					
					BorderPane root = new BorderPane();
					root.setPadding(new Insets(32,32,32,32));
					
	
					HBox box = new HBox();
					box.setSpacing(8f);
					box.setFillToParentWidth(true);
					Label l = new Label("Export to:");
					TextField ta = new TextField("level.map");
					ta.setFillToParentWidth(true);
					
					box.getChildren().add(l);
					box.getChildren().add(ta);
					
					root.setCenter(box);
					
					Button btn = new Button("Export");
					btn.setOnAction((event)-> {
						
						String str = ta.getText();
						
						if (!str.endsWith(".json"))
							if (str.lastIndexOf('.') != -1)
								str = str.substring(0, str.lastIndexOf('.')) + ".map";
							else
								str += ".map";
						
						str = "src/main/resources/maps/" + str;
						
						scene.exportPath = str;
						
						scene.ioHack = 4;
						
						window.close();
					});
					
					root.setBottom(btn);
					
					window.setScene(new Scene(root, 500, 150));
					window.show();
				}
			}.start();
		});
	}
	
	@Override
	public void dispose()
	{
		super.dispose();
		
		TilePickerWindow.close();
	}

	@Override
	public void setTileset(String string)
	{
		super.setTileset(string);
		this.recalcTileEntities();
		TilePickerWindow.reload((EditorScene) Client.scene());
	}

	public EditorEntity getLastEntityToEditTags()
	{
		return lastEntityToEditTags;
	}

	public void setPropertiesTabs(VBox[] properties)
	{
		this.propertiesTabs = properties;
	}
	
	@Override
	protected void onTerrainPicked(Tile tileAt, float heightAt)
	{
	}
}
