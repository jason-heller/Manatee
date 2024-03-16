package manatee.client.entity.stock.editor;

import static manatee.client.scene.GlobalAssets.NO_TEX;

import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import manatee.cache.definitions.Model;
import manatee.cache.definitions.mesh.IMesh;
import manatee.cache.definitions.texture.ITexture;
import manatee.client.dev.Dev;
import manatee.client.entity.BillboardForm;
import manatee.client.gl.mesh.EntityShaderTarget;
import manatee.client.scene.GlobalAssets;
import manatee.client.scene.MapScene;
import manatee.client.scene.editor.EditorScene;
import manatee.maths.Vectors;

public class EditorEntity extends BillboardForm
{
	protected Map<String, String> tags = new HashMap<>();
	
	protected String name;
	
	private String modelPath, meshPath, texturePath;
	
	protected EditorScene scene;
	
	public EditorEntity(EditorScene scene, String name, Vector3f position, Vector4f color, IMesh mesh, ITexture texture, Model model, EntityShaderTarget target)
	{
		this.position.set(position);
		setColor(color);
		
		this.scene = scene;
		
		setName(name);
		
		if (model == null)
		{
			setMesh(mesh);
			setTexture(texture);
		}
		else
		{
			this.setGraphic(model, target == null ? EntityShaderTarget.GENERIC : target);
		}
		
		this.boundingBox.setRotation(rotation);
	}
	
	public void setGraphicReferences(String meshPath, String texturePath, String modelPath, boolean lod)
	{
		this.meshPath = meshPath;
		this.modelPath = modelPath;
		this.texturePath = texturePath;
		
		this.lod = lod;
		
		if (meshPath == null && modelPath == null && texturePath != null)
		{
			this.setMesh(GlobalAssets.BILLBOARD_MESH);
			this.setFullbright(true);
			this.setVisible(true);
		}
		
	}

	@Override
	public void update(MapScene scene)
	{
	}
	
	@Override
	protected void updateModelMatrix(Matrix4f viewMatrix)
	{
		modelMatrix.identity();
		modelMatrix.translate(position.x, position.y, position.z);
		
		modelMatrix.m00(viewMatrix.m00());
		modelMatrix.m01(viewMatrix.m10());
		modelMatrix.m02(viewMatrix.m20());
		modelMatrix.m10(viewMatrix.m01());
		modelMatrix.m11(viewMatrix.m11());
		modelMatrix.m12(viewMatrix.m21());
		modelMatrix.m20(viewMatrix.m02());
		modelMatrix.m21(viewMatrix.m12());
		modelMatrix.m22(viewMatrix.m22());
		
		//modelMatrix.scale(scale.x, scale.y, scale.z);
	}
	
	@Override
	public void updateInternal(MapScene scene)
	{
		boundingBox.update();
		
		update(scene);

		if (getNumMeshes() > 0 && getMesh(0) == GlobalAssets.BILLBOARD_MESH)
			updateModelMatrix(scene.getCamera().getViewMatrix());
		else
			updateModelMatrix();
	}

	public Map<String, String> getTags()
	{
		return tags;
	}

	public void setTags(Map<String, String> tags)
	{
		this.tags = tags;
		onTagEdit();
	}
	
	private void setMesh(IMesh mesh)
	{
		if (this.meshes == null && mesh != null && this.boundingBox.halfExtents.equals(Vectors.EMPTY))
		{
			Vector3f halfExtents = new Vector3f(mesh.getMax()).sub(mesh.getMin()).mul(0.5f);

			this.boundingBox.halfExtents.set(halfExtents);
		}
		
		if (mesh == null && this.isVisible())
		{
			boundingBox.halfExtents.set(.5f, .5f, .5f);
			this.textures = new ITexture[] {NO_TEX};
			
			this.setVisible(false);
		}
		
		if (mesh != null)
			this.meshes = new IMesh[] {mesh};
	}
	
	private void setTexture(ITexture texture)
	{
		if (meshes != null && meshes[0] == GlobalAssets.MISSING_MESH)
		{
			this.textures = new ITexture[] {GlobalAssets.NO_TEX};
			return;
		}
		
		if (texture == null)
			texture = GlobalAssets.MISSING_TEX;
		
		this.textures = new ITexture[] {texture};
	}
	
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public void setColor(Vector4f color)
	{
		this.color.set(color);

		onColorChange();
	}

	public void onColorChange()
	{
		if (name == null)
			return;

		switch(name)
		{
			case "SunControl":
			{
				scene.getLightColor().set(color.x, color.y, color.z);
				break;
			}
			
			case "WaterControl":
			{
				scene.getMap().getWater().getColor().set(color);
				break;
			}
		}
	}

	public String getModel()
	{
		return modelPath;
	}

	public void setModel(String model)
	{
		this.modelPath = model;
	}

	public String getMesh()
	{
		return meshPath;
	}

	public void setMesh(String mesh)
	{
		this.meshPath = mesh;
	}

	public String getTexture()
	{
		return texturePath;
	}

	public void setTexture(String texture)
	{
		this.texturePath = texture;
	}
	
	// TODO: This is gross - needs refactor
	@Override
	public int getNumMeshes()
	{
		return meshes == null ? 0 : meshes.length;
	}

	public void onScale()
	{
		
	}
	
	public void onTranslate()
	{
		
	}
	
	public void onRotate()
	{
		if (name == null)
			return;

		switch(name)
		{
			case "SunControl":
			{
				Vector3f v = new Vector3f(0, 1, 0);
				scene.getLightVector().set(rotation.transform(v));
				break;
			}
			
			case "WaterControl":
			{
				Vector3f v = new Vector3f(1, 0, 0);
				rotation.transform(v);
				
				scene.getMap().getWater().getFlowDir().set(v.x, v.y);
				break;
			}
			
			case "WindControl":
			{
				Vector3f v = new Vector3f(1, 0, 0);
				rotation.transform(v);
				
				scene.getMap().getWind().getWindVector().set(v.x, v.y, v.z);
				break;
			}
		}
	}
	
	
	public void onTagEdit()
	{
		if (name == null)
			return;

		switch(name)
		{
		case "WaterControl":
		{
			String flowSpeed = tags.get("flowSpeed");
			try {
				if (flowSpeed != null)
					scene.getMap().getWater().setFlowSpeed(Float.parseFloat(flowSpeed));
			}
			catch (NumberFormatException e)
			{
				Dev.log(flowSpeed + " is not a valid real number");
			}
			
			String waveAmplitude = tags.get("waveAmplitude");
			try {
				if (waveAmplitude != null)
					scene.getMap().getWater().setWaveAmplitude(Float.parseFloat(waveAmplitude));
			}
			catch (NumberFormatException e)
			{
				Dev.log(waveAmplitude + " is not a valid real number");
			}
			break;
		}
		
		case "WindControl":
		{
			String flowSpeed = tags.get("windSpeed");
			try {
				if (flowSpeed != null)
					scene.getMap().getWind().setWindSpeed(Float.parseFloat(flowSpeed));
			}
			catch (NumberFormatException e)
			{
				Dev.log(flowSpeed + " is not a valid real number");
			}
			
			String waveAmplitude = tags.get("windStrength");
			try {
				if (waveAmplitude != null)
					scene.getMap().getWind().setWindStrength(Float.parseFloat(waveAmplitude));
			}
			catch (NumberFormatException e)
			{
				Dev.log(waveAmplitude + " is not a valid real number");
			}
			break;
		}
		}
	}

	public void setLodDistanceSqr(float lodDistanceSqr)
	{
		this.lodDistanceSqr = lodDistanceSqr;
	}

	public void setTags(String[] tagKeys, String[] tagValues)
	{
		tags.clear();
		
		for(int i = 0; i < tagKeys.length; i++)
		{
			tags.put(tagKeys[i], tagValues[i]);
		}
	}
}
