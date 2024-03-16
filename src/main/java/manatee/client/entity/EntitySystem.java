package manatee.client.entity;

import static manatee.client.gl.camera.CameraUtil.fov;
import static manatee.client.gl.camera.CameraUtil.near;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import lwjgui.LWJGUI;
import manatee.client.entity.stock.Player;
import manatee.client.gl.camera.ICamera;
import manatee.client.gl.mesh.EntityShaderTarget;
import manatee.client.gl.renderer.EntityRenderer;
import manatee.client.map.MapGeometry;
import manatee.client.scene.GameMap;
import manatee.client.scene.MapScene;
import manatee.maths.MatrixMath;

public class EntitySystem
{
	protected List<Entity> nonStaticEntities;
	
	protected List<Form> forms;
	
	private Map<EntityShaderTarget, List<Form>> formRenderBatch;
	
	private Player player;
	
	private EntityRenderer entityRenderer;
	
	public static final float RAYCAST_REACH = 40f;
	
	public EntitySystem(Vector3f lightColor, Vector3f lightVector)
	{
		nonStaticEntities = new ArrayList<>();
		forms = new ArrayList<>();
		formRenderBatch = new HashMap<>();
		
		entityRenderer = new EntityRenderer(lightColor, lightVector);
	}

	public void dispose()
	{
		entityRenderer.dispose();
	}
	
	public void addEntity(Entity entity)
	{
		nonStaticEntities.add(entity);
		entity.onSpawn();
	}
	
	public void addForm(Form form)
	{
		EntityShaderTarget shaderTarget = form.getShaderTarget();
		
		List<Form> formBatch = formRenderBatch.get(shaderTarget);
		
		if (formBatch == null)
		{
			formBatch = new ArrayList<>();
			formRenderBatch.put(shaderTarget, formBatch);
		}
		
		formBatch.add(form);
		forms.add(form);
		
		form.onSpawn();
	}

	public void render(MapScene scene)
	{
		ICamera camera = scene.getCamera();
		Vector3f camPos = camera.getPosition();
		GameMap map = scene.getMap();
		MapGeometry geom = map.getGeometry();
		
		render(EntityShaderTarget.GENERIC, camPos, geom, scene);
		render(EntityShaderTarget.FOLIAGE, camPos, geom, scene);
	}

	private void render(EntityShaderTarget shaderTarget, Vector3f camPos, MapGeometry geom, MapScene scene)
	{
		List<Form> batch = formRenderBatch.get(shaderTarget);
		
		if (batch == null)
			return;
		
		entityRenderer.begin(shaderTarget, scene);

		for (Form form : batch)
			entityRenderer.drawEntity(camPos, geom, form);
		
		entityRenderer.end();
	}

	public void tick(MapScene scene)
	{
		for(Entity entity : nonStaticEntities)
		{
			entity.updateInternal(scene);
		}
		
		for (Form form : forms)
		{
			form.updateInternal(scene);
		}
	}

	public SpatialEntity raycastEntities(Matrix4f viewMatrix, Vector3f rayOrigin, Vector3f rayDirection, Vector3f collidePointOut)
	{
		float aspect = LWJGUI.getThreadWindow().getAspectRatio();
		
		Matrix4f projMatrix = new Matrix4f();
		MatrixMath.setProjectionMatrix(projMatrix, fov, aspect, near, RAYCAST_REACH);
		
		// AlignedBox frustumAABB = MatrixMath.getFrustumAABB(projMatrix, viewMatrix);

		SpatialEntity entityCollided = null;
		float t = Float.POSITIVE_INFINITY;
		
		for(Form form : forms)
		{
			//if (!form.isVisible())
			//	continue;
			
			float dist = form.getBoundingBox().raycast(rayOrigin, rayDirection);
			if (dist < t)
			{
				t = dist;
				entityCollided = form;
			}
		}
		
		if (collidePointOut != null && entityCollided != null)
			collidePointOut.set(rayDirection).mul(t).add(rayOrigin);
		
		return entityCollided;
	}

	public Player getPlayer()
	{
		return player;
	}

	public void setPlayer(Player player)
	{
		this.removeForm(player);
		this.player = player;
		player.onSpawn();
		addForm(player);
	}

	public List<Entity> getNonStaticEntities()
	{
		return nonStaticEntities;
	}

	public List<Form> getForms()
	{
		return forms;
	}

	public void removeEntity(Entity entity)
	{
		boolean b = nonStaticEntities.remove(entity);
		
		if (b)
			return;
					
		removeForm((Form)entity);
		
		entity.onDespawn();
	}
	
	public void removeForm(Form form)
	{
		boolean contains = forms.remove(form);
		
		if (contains)
			formRenderBatch.get(form.getShaderTarget()).remove(form);
		
		form.onDespawn();
	}
	
	public void clear()
	{
		this.formRenderBatch.clear();
		this.forms.clear();
		this.nonStaticEntities.clear();
	}

}
