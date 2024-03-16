package manatee.client.scene.editor.particle;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.joml.Vector3f;
import org.joml.Vector4f;

import manatee.client.gl.camera.ICamera;
import manatee.client.gl.camera.TrackingCamera;
import manatee.client.gl.particle.ParticleEmitter;
import manatee.client.gl.particle.ParticleManager;
import manatee.client.gl.particle.ParticleSystem;
import manatee.client.gl.particle.attribs.IParticleAttrib;
import manatee.client.gl.particle.mesh.MeshParticleSystem;
import manatee.client.map.tile.Tile;
import manatee.client.scene.Assets;
import manatee.client.scene.MapScene;
import manatee.client.ui.ClientUI;
import manatee.client.ui.UIBuilder;
import manatee.maths.Vectors;
import manatee.primitives.Primitives;

public class ParticleViewScene extends MapScene
{
	public String particleType = "smoke";

	public float pps = 1f;
	public float ppsVariance = 0f;

	private List<ParticleEmitter> emitters = new ArrayList<>();

	public int ppe = 1;

	protected String newEffect = null;

	private ParticleViewUIBuilder uib;
	
	@Override
	public void tick()
	{
		super.tick();
		
		if (newEffect != null)
		{
			particleType = newEffect;
			getParticleManager().addSystem(particleType, ParticleManager.meshIndices.get("smoke"), new IParticleAttrib[] {});
			resetEmitters();
			
			uib.handleNewEffect(newEffect);
			
			newEffect = null;
		}
	}

	@Override
	public void init(ClientUI ui)
	{
		super.init(ui);

		emitters.add(new ParticleEmitter(Vectors.EMPTY, pps, ppsVariance, ppe, true));

		// Load particles

		resetEmitters();

		setCamera(new TrackingCamera());

		this.getColor().set(.5f, 0, .5f, 1f);

		Primitives.addBox(Vectors.EMPTY, new Vector3f(5, 5, 0));
	}

	public void resetEmitters()
	{
		particles.clear();

		for (ParticleEmitter pe : emitters)
		{
			pe.setParticlesPerSecond(pps, ppsVariance);
			pe.setEmissionVariance(ppsVariance);
			pe.setParticlesPerEmission((int) ppe);

			particles.addEmitter(particleType, pe);
		}
	}

	@Override
	public void dispose()
	{
		super.dispose();

		// Primitives.remove(prim);
	}

	@Override
	public void setCamera(ICamera camera)
	{
		super.setCamera(camera);

		if (camera instanceof TrackingCamera)
			((TrackingCamera) camera).setTrackingTarget(Vectors.EMPTY);
	}

	@Override
	public UIBuilder createUIBuilder()
	{
		uib = new ParticleViewUIBuilder();
		return uib;
	}

	@Override
	protected Assets createAssets()
	{
		return new EmptyAssets();
	}

	@Override
	protected void onMouseHoverChange()
	{
	}

	public Set<String> getParticleSystemNames()
	{
		return particles.getParticleSystemNames();
	}

	public List<ParticleEmitter> getEmitters()
	{
		return emitters;
	}

	private class EmptyAssets extends Assets
	{

		@Override
		public void loadAssets()
		{
		}
	}

	public void setAttributes(String psName, IParticleAttrib[] attribs)
	{
		particles.getParticleSystem(psName).setAttributes(attribs);
	}

	public void save(String filePath)
	{
		StringBuilder sb = new StringBuilder();
		
		try {
			for(String name : particles.getParticleSystemNames())
			{
				ParticleSystem ps = particles.getParticleSystem(name);
				
				sb.append(name).append("{");
				
				sb.append("\n\t").append("Emission; ").append(pps).append("; ").append(ppe).append("; ").append(ppsVariance);
				{
					int index = ((MeshParticleSystem) ps).getMeshIndex();
					sb.append("\n\t").append("Mesh; ").append(index);
				}
				
				//sb.append(ps.getAttributes().length);
				for(Object attrib : ps.getAttributes())
				{
					sb.append("\n\t").append(attrib.getClass().getSimpleName().replace("ParticleAttrib", ""));
					
					Field[] fields = attrib.getClass().getFields();
					
					for(Field field : fields)
					{
						
						if (field.getType() == Vector3f.class)
						{
							Vector3f v = (Vector3f) field.get(attrib);
							sb.append("; ").append(v.x);
							sb.append("; ").append(v.y);
							sb.append("; ").append(v.z);
						}
						else if (field.getType() == Vector4f.class)
						{
							Vector4f v = (Vector4f) field.get(attrib);
							sb.append("; ").append(v.x);
							sb.append("; ").append(v.y);
							sb.append("; ").append(v.z);
							sb.append("; ").append(v.w);
						}
						else if (field.getType() == float.class)
						{
							sb.append("; ").append(field.getFloat(attrib));
						}
					}
				}
				
				sb.append("\n}\n");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		try
		{
			Files.write(Paths.get(filePath), sb.toString().getBytes(), StandardOpenOption.CREATE);
			System.out.println("File written successfully!");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onTerrainPicked(Tile tileAt, float heightAt)
	{
	}
}
