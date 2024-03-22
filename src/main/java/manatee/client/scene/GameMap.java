package manatee.client.scene;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import manatee.client.dev.Command;
import manatee.client.gl.camera.ICamera;
import manatee.client.gl.renderer.HeightFieldRenderer;
import manatee.client.gl.renderer.TileRenderer;
import manatee.client.map.ISceneDrawable;
import manatee.client.map.MapGeometry;
import manatee.client.map.MapRegion;
import manatee.client.map.tile.Tilemap;
import manatee.maths.MCache;
import manatee.maths.geom.Plane;

public class GameMap implements ISceneDrawable
{
	private HeightFieldRenderer renderHeightField;
	private TileRenderer renderTileField;
	
	private WaterHandler water = new WaterHandler();
	private WindHandler wind = new WindHandler();
	
	private MapGeometry geom;

	private boolean renderWireframe;
	private boolean skipTerrain;
	public static boolean renderSkipped;

	public GameMap(Assets assets, Vector3f lightColor, Vector3f lightVector)
	{
		GameMap.renderSkipped = false;
		
		geom = new MapGeometry(assets);
		water = new WaterHandler();
		
		load(lightColor, lightVector);
		
		Command.add("render_wireframe", Command.BOOL_SYNTAX, this, "setWireframeRenderer", true);
		Command.add("render_skipped", Command.BOOL_SYNTAX, this, "setRenderSkipped", true);
	}

	public void load(Vector3f lightColor, Vector3f lightVector)
	{
		renderHeightField = new HeightFieldRenderer(lightColor, lightVector);
		renderTileField = new TileRenderer(lightColor, lightVector);
	}

	@Override
	public void render(IScene scene)
	{
		ICamera camera = scene.getCamera();
		
		Matrix4f projViewMatrix = camera.getProjectionViewMatrix();
		
		if (renderWireframe)
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

		if (!skipTerrain)
			renderHeightField.draw(projViewMatrix, getTilemap(), geom.getMapRegions());
		
		renderTileField.draw((MapScene) scene, geom.getMapRegions());

		if (renderWireframe)
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
	}

	public void tick()
	{
		water.tick();
	}
	
	public void dispose()
	{
		renderHeightField.dispose();
		renderTileField.dispose();

		geom.dispose();
	}

	public void setWireframeRenderer(boolean renderWireframe)
	{
		this.renderWireframe = renderWireframe;
	}

	public MapGeometry getGeometry()
	{
		return geom;
	}

	/*
	 * 
	 * 
	 */

	private static final int RECURSION_COUNT = 200;
	private static final float RAY_RANGE = 500f;

	public Vector3f raycast(Vector3f origin, Vector3f direction)
	{
		Vector3f rayPoint = null;
		
		// Raycast heightmap geo
		if (intersectionInRange(origin, direction, 0f, RAY_RANGE))
			rayPoint = binarySearch(origin, direction, 0, RAY_RANGE, 0);

		if (rayPoint == null)
		{
			float intersect = new Plane(MCache.EMPTY, MCache.Z_AXIS).raycast(origin, direction);
			
			if (!Float.isNaN(intersect))
				rayPoint = new Vector3f(direction).mul(intersect).add(origin);
			else
				return null;
		}

		return rayPoint;
	}

	private Vector3f binarySearch(Vector3f origin, Vector3f direction, float start, float finish, int count)
	{
		float half = start + ((finish - start) / 2f);
		if (count >= RECURSION_COUNT)
		{
			Vector3f endPoint = getPointOnRay(origin, direction, half);
			MapRegion region = geom.getRegionAt(endPoint.x, endPoint.y);
			
			if (region != null)
				return endPoint;
			
			return null;
		}
		if (intersectionInRange(origin, direction, start, half))
			return binarySearch(origin, direction, start, half, count + 1);
		else
			return binarySearch(origin, direction, half, finish, count + 1);
	}

	private Vector3f getPointOnRay(Vector3f origin, Vector3f direction, float distance)
	{
		return new Vector3f(direction).mul(distance).add(origin);
	}

	private boolean intersectionInRange(Vector3f origin, Vector3f direction, float start, float finish)
	{
		Vector3f startPoint = getPointOnRay(origin, direction, start);
		Vector3f endPoint = getPointOnRay(origin, direction, finish);
		return !isUnderGround(startPoint) && isUnderGround(endPoint);
	}

	private boolean isUnderGround(Vector3f testPoint)
	{
		float height = geom.getHeightAt(testPoint.x, testPoint.y);
		return testPoint.z < height;
	}

	public Tilemap getTilemap()
	{
		return geom.getTilemap();
	}

	@Override
	public void load()
	{
		// TODO Auto-generated method stub
		
	}

	public WaterHandler getWater()
	{
		return water;
	}

	public WindHandler getWind()
	{
		return wind;
	}

	public boolean isSkipTerrain()
	{
		return skipTerrain;
	}

	public void setSkipTerrain(boolean skipTerrain)
	{
		this.skipTerrain = skipTerrain;
	}
	

	

	
	public void setRenderSkipped(boolean skip)
	{
		renderSkipped = skip;
		this.geom.buildAllMeshes();
	}
}
