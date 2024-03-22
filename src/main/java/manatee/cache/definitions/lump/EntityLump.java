package manatee.cache.definitions.lump;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import manatee.cache.definitions.Model;
import manatee.cache.definitions.mesh.IMesh;
import manatee.cache.definitions.texture.ITexture;
import manatee.client.entity.Entity;
import manatee.client.entity.EntitySystem;
import manatee.client.entity.stock.PropEntity;
import manatee.client.entity.stock.editor.EditorEntity;
import manatee.client.entity.stock.editor.EditorLightEntity;
import manatee.client.gl.mesh.EntityShaderTarget;
import manatee.client.map.MapGeometry;
import manatee.client.map.MapRegion;
import manatee.client.scene.Assets;
import manatee.client.scene.GameMap;
import manatee.client.scene.MapScene;
import manatee.client.scene.PlayableScene;
import manatee.client.scene.WaterHandler;
import manatee.client.scene.WindHandler;
import manatee.client.scene.editor.EditorScene;

public class EntityLump implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Vector3f position;
	public Quaternionf rotation;
	public Vector3f scale;
	public String name;
	
	public String mesh, texture, model;
	
	public boolean lod;
	public float lodDistanceSqr;
	
	public Vector4f color;
	public Map<String, String> tags;
	
	public EntityShaderTarget shader;
	
	public EntityLump() {}
	
	public EntityLump(EditorEntity e)
	{
		this.position = e.getPosition();
		this.rotation = e.getRotation();
		this.scale = e.getScale();
		this.name = e.getName();
		this.color = e.getColor();
		this.tags = e.getTags();
		this.model = e.getModelPath();
		this.mesh = e.getMesh();
		this.texture = e.getTexture();
		this.lodDistanceSqr = e.getLodDistanceSqr();
		this.lod = e.hasLod();
		this.shader = e.getShaderTarget();
	}
	
	public void process(MapScene scene, EntitySystem entitySystem)
	{
		GameMap map = scene.getMap();
		//MapGeometry geom = map.getGeometry();
		WaterHandler water = map.getWater();
		WindHandler wind = map.getWind();
		
		Assets assets = scene.getAssets();
		
		switch(name)
		{
			case "Player":
			{
				if (scene instanceof PlayableScene)
				{
					((PlayableScene)scene).spawnPlayer(position, rotation);
				}
				break;
			}
			case "SunControl":
			{
				if (scene instanceof EditorScene)
				{
					Vector3f lightDir = scene.getLightVector();
					rotation.transform(lightDir);
				}
				return;
			}
			case "WaterControl":
			{
				//if (scene instanceof EditorScene)
				{
					water.getColor().set(color);
					water.setWaveAmplitude(getFloat("waveAmplitude"));
					water.setFlowSpeed(getFloat("flowSpeed"));
					
					Vector3f v = quatToVec3(rotation);
					water.getFlowDir().set(v.x, v.y);
					
					//"reflectivity": "0.0",
				}
				break;
			}
			case "WindControl":
			{
				
				wind.getWindVector().set(quatToVec3(rotation));
				wind.setWindSpeed(getFloat("windSpeed"));
				wind.setWindStrength(getFloat("windStrength"));
				break;
			}
			case "CampfireEffect":
			{
				scene.getParticleManager().addEmitter("smoke", new Vector3f(position).add(0,0,1), true);
				scene.getParticleManager().addEmitter("fire", position, true);
				break;
			}
			case "SoundscapeEntity":
				break;
				
			default:
			{
				PropEntity e = new PropEntity();
				
				if (model != null)
				{
					Model modelAsset = assets.getModel(model);
					e.setGraphic(modelAsset, shader);
				}
				else
				{
					IMesh meshAsset = assets.getMesh(mesh);
					ITexture textureAsset = assets.getTexture(texture);
					e.setGraphic(meshAsset, textureAsset, shader);
				}

				e.setColor(color);
				
				e.setLod(lod);
				e.setLodDistanceSqr(lodDistanceSqr);
				
				e.getPosition().set(position);
				e.getRotation().set(rotation);
				e.getScale().set(scale);
				
				entitySystem.addForm(e);
			}
		}
	}
		
	private MapRegion regionAt(MapGeometry geom, Vector3f position)
	{
		return geom.getRegionAt(position.x, position.y);
	}

	private Vector3f quatToVec3(Quaternionf rotation)
	{
		Vector3f v = new Vector3f(0, 1, 0);
		rotation.transform(v);
		return v;
	}

	private float getFloat(String string)
	{
		return Float.parseFloat(tags.get(string));
	}

	private float getInt(String string)
	{
		return Integer.parseInt(tags.get(string));
	}

	public static EntityLump[] load(EditorScene scene)
	{
		List<EntityLump> entities = new LinkedList<>();
		for (Entity entity : scene.getEntitySystem().getForms())
		{
			if (entity instanceof EditorEntity && !(entity instanceof EditorLightEntity))
			{
				EditorEntity editorEntity = (EditorEntity) entity;
				
				entities.add(new EntityLump(editorEntity));
			}
		}
		
		return entities.toArray(new EntityLump[entities.size()]);
	}
}
