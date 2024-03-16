package manatee.client.gl.renderer;

import java.awt.Window;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import manatee.cache.definitions.mesh.IMesh;
import manatee.cache.definitions.texture.ITexture;
import manatee.client.Client;
import manatee.client.Time;
import manatee.client.entity.Form;
import manatee.client.gl.Shader;
import manatee.client.gl.mesh.EntityShaderTarget;
import manatee.client.map.MapGeometry;
import manatee.client.map.MapRegion;
import manatee.client.scene.MapScene;
import manatee.client.scene.WindHandler;
import manatee.maths.Vectors;

public class EntityRenderer
{
	private Shader[] shaders;
	
	private Shader activeShader;
	
	public EntityRenderer(Vector3f lightColor, Vector3f lightVector)
	{
		shaders = new Shader[] {
				new Shader("scene/entity/entity.vsh", "scene/entity/entity.fsh"),
				new Shader("scene/entity/e_foliage.vsh", "scene/entity/e_foliage.fsh")
		};
		
		this.lightColor = lightColor;
		this.lightVector = lightVector;
	}
	
	private Vector3f lightColor, lightVector;

	public void begin(EntityShaderTarget target, MapScene scene)
	{

		Matrix4f projectionView = scene.getCamera().getProjectionViewMatrix();
		
		WindHandler wind = scene.getMap().getWind();
		
		activeShader = shaders[target.ordinal()];
		activeShader.bind();
		activeShader.setUniform("v_ProjectionView", projectionView);
		activeShader.setUniform("v_AmbientColor", lightColor);
		activeShader.setUniform("v_AmbientVector", lightVector);
		
		if (target == EntityShaderTarget.FOLIAGE)
		{

			activeShader.setUniform("v_WindVector", wind.getWindVector());
			activeShader.setUniform("v_WindStrength", wind.getWindStrength());
			activeShader.setUniform("v_WindTime", wind.getWindTime());
			
			wind.tick();
		}
	}

	public void drawEntity(Vector3f cameraPos, MapGeometry geom, Form form)
	{
		if (!form.isVisible())
			return;

		activeShader.setUniform("v_ModelMatrix", form.getModelMatrix());
		
		Vector3f localLightVec = new Vector3f(lightVector);
		Quaternionf q = new Quaternionf(form.getRotation());
		q.invert();
		q.transform(localLightVec);
		
		activeShader.setUniform("v_Diffuse", form.getColor());
		if (form.isFullbright())
		{
			Vector3f light = new Vector3f(Client.scene().getCamera().getLookVector());
			light.negate();
			activeShader.setUniform("v_LightNum", 0);
			activeShader.setUniform("v_AmbientColor", Vectors.ONE);
			activeShader.setUniform("v_AmbientVector", light);
		}
		else
		{
			MapRegion region = geom.getRegionAt(form.getPosition().x, form.getPosition().y);
			
			if (region != null)
				region.passLights(activeShader);
			else
				activeShader.setUniform("v_LightNum", 0);
		}

		int nMeshes = form.getNumMeshes();

		if (form.hasLod())
		{
			float distSqr = form.getPosition().distanceSquared(cameraPos);
			int nLodIndices = (nMeshes - 1);
			int lod = nLodIndices - Math.min((int)(distSqr / form.getLodDistanceSqr()), nLodIndices);
			
			ITexture texture = form.getTexture(0);
			activeShader.setTexture("f_Diffuse", texture, 0);

			drawMeshIndexed(form.getMesh(lod));
		}
		else
		{
			for (int i = 0; i < nMeshes; i++)
			{
				IMesh mesh = form.getMesh(i);
				ITexture texture = form.getTexture(i);
				activeShader.setTexture("f_Diffuse", texture, 0);

				drawMeshIndexed(mesh);
			}
		}
		
		if (form.isFullbright())
		{
			activeShader.setUniform("v_AmbientColor", lightColor);
			activeShader.setUniform("v_AmbientVector", lightVector);
		}
	}
	
	private void drawMeshIndexed(IMesh mesh)
	{
		mesh.bind();
		
		GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
		
		mesh.unbind();
	}

	public void end()
	{
		activeShader.unbind();
	}
	
	public void dispose()
	{
		for(Shader shader : shaders)
			if (shader != null)
				shader.dispose();
	}
}
