package manatee.cache.definitions.loader;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joml.Vector3f;
import org.joml.Vector4f;

import manatee.client.Client;
import manatee.client.dev.Dev;
import manatee.client.gl.particle.ParticleManager;
import manatee.client.gl.particle.ParticleSystem;
import manatee.client.gl.particle.attribs.IParticleAttrib;
import manatee.client.gl.particle.attribs.WindParticleAttrib;
import manatee.client.gl.particle.mesh.MeshParticleSystem;
import manatee.client.scene.MapScene;
import manatee.client.scene.editor.particle.ParticleViewUIBuilder;

public class ParticleSystemLoader
{
	public static void load(ParticleManager particles)
	{
		try
		{
			List<String> lines = Files.readAllLines(Paths.get("src/main/resources/scene/particle/particles.txt"));
			Iterator<String> iter = lines.iterator();
			while(iter.hasNext())
			{
				String line = iter.next();
				if (line.contains("{"))
				{
					String psName = line.split("\\{")[0];
					int psMeshId = -1;
					float pps = 10f;
					float ppsVariance = 0f;
					int ppe = 1;
					
					List<IParticleAttrib> attribs = new ArrayList<>();

					while (!((line = iter.next()).contains("}")))
					{
						String[] data = line.replaceAll("\\s+", "").split(";");
						
						switch(data[0])
						{
						case "Emission":
							pps = Float.parseFloat(data[1]);
							ppe = (int)Float.parseFloat(data[2]);
							ppsVariance = Float.parseFloat(data[3]);
							break;
						case "Mesh":
							psMeshId = (int)Float.parseFloat(data[1]);
							break;
						default:
							attribs.add(parseAttrib(data));
						}
					}
					
					ParticleSystem ps = particles.addSystem(psName, psMeshId, attribs.toArray(new IParticleAttrib[0]));
					ps.setParticlesPerEmission(ppe);
					ps.setParticlesPerSecond(pps);
					ps.setEmissionVariance(ppsVariance);
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static IParticleAttrib parseAttrib(String[] data)
	{
		Class<?> clTarget = null;
		String target = data[0] + "ParticleAttrib";
		for(Class<?> cl : ParticleViewUIBuilder.attribClasses)
		{
			if (cl.getSimpleName().equals(target))
			{
				clTarget = cl;
				break;
			}
		}
		
		if (clTarget == null)
		{
			System.err.println("could not find class " + target);
			System.exit(-1);
		}
		
		Constructor<?> constructor = clTarget.getConstructors()[0];
		
		Object[] args = new Object[constructor.getParameterCount()];
		
		int j = 1;
		for(int i = 0; i < args.length; i++)
		{
			Class<?> type = constructor.getParameters()[i].getType();
			if (type == Vector4f.class)
			{
				args[i] = new Vector4f(Float.parseFloat(data[j++]), Float.parseFloat(data[j++]),
						Float.parseFloat(data[j++]), Float.parseFloat(data[j++]));
			}
			else if (type == Vector3f.class)
			{
				args[i] = new Vector3f(Float.parseFloat(data[j++]), Float.parseFloat(data[j++]),
						Float.parseFloat(data[j++]));
			}
			else if (type == float.class)
			{
				args[i] = Float.parseFloat(data[j++]);
			}
		}
		
		try
		{
			return (IParticleAttrib) constructor.newInstance(args);
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			e.printStackTrace();
		}
		
		System.exit(-1);
		return null;
	}
}
