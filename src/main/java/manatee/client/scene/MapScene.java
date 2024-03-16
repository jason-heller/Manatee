package manatee.client.scene;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.joml.Vector4f;

import lwjgui.LWJGUI;
import lwjgui.glfw.input.MouseHandler;
import lwjgui.scene.Window;
import manatee.cache.definitions.binfile.BinaryMapFileReader;
import manatee.cache.definitions.loader.ParticleSystemLoader;
import manatee.cache.definitions.loader.exception.MapLoadException;
import manatee.client.dev.Dev;
import manatee.client.entity.EntitySystem;
import manatee.client.entity.SpatialEntity;
import manatee.client.gl.BlendMode;
import manatee.client.gl.camera.CameraUtil;
import manatee.client.gl.camera.FreeCamera;
import manatee.client.gl.camera.ICamera;
import manatee.client.gl.camera.TrackingCamera;
import manatee.client.gl.particle.ParticleManager;
import manatee.client.gl.renderer.nvg.NVGObject;
import manatee.client.gl.renderer.nvg.NVGText;
import manatee.client.input.Input;
import manatee.client.input.Keybinds;
import manatee.client.map.tile.Tile;
import manatee.client.ui.ClientUI;

public abstract class MapScene implements IScene
{

	public static final Vector3f DEFAULT_SUN_VECTOR = new Vector3f(-.5f, .5f, 0);
	protected ICamera camera;
	protected GameMap map;

	protected EntitySystem entitySystem;
	
	protected List<NVGObject> nvgObjects = new ArrayList<>();
	
	public Vector3f mouseTerrainPos = new Vector3f();
	public Vector3f mouseWorldPos = new Vector3f();
	protected Vector3f mouseRay = new Vector3f();
	
	protected SpatialEntity hoveredEntity;
	
	protected Assets assets;
	
	protected ClientUI ui;
	
	private Vector4f color = new Vector4f();
	
	protected Vector3f lightColor = new Vector3f(1,1,1);
	protected Vector3f lightVector = new Vector3f(DEFAULT_SUN_VECTOR);
	
	private BlendMode blendMode = BlendMode.MULTIPLY;
	
	protected ParticleManager particles = new ParticleManager();

	@Override
	public void init(ClientUI ui)
	{
		CameraUtil.initCommands(this);
		
		assets = createAssets();
			
		assets.loadAssets();
		
		entitySystem = new EntitySystem(lightColor, lightVector);
		
		map = new GameMap(assets, lightColor, lightVector);
		
		ParticleSystemLoader.load(particles);
		
		this.ui = ui;

	}
	
	protected abstract Assets createAssets();

	@Override
	public void tick()
	{
		camera.update();
		
		entitySystem.tick(this);
		
		map.tick();
		
		Window window = LWJGUI.getThreadWindow();
		MouseHandler mouseHandler = window.getMouseHandler();
		int mx = mouseHandler.getXI();
		int my = mouseHandler.getYI();
		
		Vector3f mouseOrig = camera.getPosition();
		mouseRay.set(WindowPicker.screenSpaceToWorldRay(camera, mx, my));
		
		Vector3f raycast = map.raycast(mouseOrig, mouseRay);

		if (raycast != null)
			mouseTerrainPos.set(raycast);
		
		mouseWorldPos.set(mouseTerrainPos);
		
		// Raycast against tile entities
		SpatialEntity entity = entitySystem.raycastEntities(camera.getViewMatrix(), mouseOrig, mouseRay, mouseWorldPos);
		
		if (hoveredEntity != entity)
		{
			hoveredEntity = entity;
			onMouseHoverChange();
		}
		
		if (Input.isPressed(Keybinds.ALT_SELECT) && mouseTerrainPos != null)
		{
			int tileAt = map.getGeometry().getTileIdAt((int)mouseTerrainPos.x, (int)mouseTerrainPos.y);
			float heightAt = map.getGeometry().getHeightAt(mouseTerrainPos.x, mouseTerrainPos.y);
			onTerrainPicked(map.getTilemap().get(tileAt), heightAt);	
		}
	}
	
	public void importMap(String path)
	{
		try
		{
			BinaryMapFileReader mapReader = new BinaryMapFileReader(path);
			mapReader.read(this);
		}
		catch(MapLoadException e)
		{
			e.printStackTrace();
		}
	}
	
	protected abstract void onTerrainPicked(Tile tileAt, float heightAt);
	
	protected abstract void onMouseHoverChange();

	public NVGText addText(String text, Vector3f pos)
	{
		NVGText st = new NVGText(pos, text);
		nvgObjects.add(st);
		
		return st;
	}
	
	public void removeText(NVGText text)
	{
		nvgObjects.remove(text);
	}

	@Override
	public void dispose()
	{
		map.dispose();
		entitySystem.dispose();
		assets.dispose();

		particles.dispose();
	}
	
	public void setCameraByName(String type)
	{
		setCamera(CameraUtil.getCameraByName(type));
	}
	
	public void toggleNoclip()
	{
		if (camera instanceof FreeCamera)
			setCamera(new TrackingCamera());
		else
			setCamera(new FreeCamera());
	}
	
	@Override
	public void setCamera(ICamera camera)
	{
		boolean copyOldCamData = (this.camera != null);
		
		Vector3f oldPos = null;
		
		if (copyOldCamData)
		{
			oldPos = this.camera.getPosition();
		}
		
		this.camera = camera;
		
		if (copyOldCamData)
		{
			//this.camera.setYaw(oldYaw);
			//this.camera.setPitch(oldPitch);
			this.camera.setPosition(oldPos);
		}
	}

	@Override
	public ICamera getCamera()
	{
		return camera;
	}

	@Override
	public void render()
	{
		entitySystem.render(this);
		
		map.render(this);

		particles.render(this);
		
	}
	
	@Override
	public List<NVGObject> getNVGObjects()
	{
		return nvgObjects;
	}

	public GameMap getMap()
	{
		return map;
	}

	public Vector3f getMouseWorldPosition()
	{
		return this.mouseWorldPos;
	}

	public Vector4f getColor()
	{
		return color;
	}

	public Vector3f getLightColor()
	{
		return lightColor;
	}

	public Vector3f getLightVector()
	{
		return lightVector;
	}

	public EntitySystem getEntitySystem()
	{
		return entitySystem;
	}

	@Override
	public Assets getAssets()
	{
		return this.assets;
	}

	public void setTileset(String tileset)
	{
		map.getGeometry().setTileset(tileset);
	}
	
	public void setBlendMode(BlendMode blendMode)
	{
		this.blendMode = blendMode;
	}
	
	public BlendMode getBlendMode()
	{
		return blendMode;
	}

	public ParticleManager getParticleManager()
	{
		return particles;
	}
}
