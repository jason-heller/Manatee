package manatee.client.gl.renderer;

import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL11.glDrawArrays;

import java.util.Collection;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import manatee.cache.definitions.mesh.IMesh;
import manatee.client.gl.Shader;
import manatee.client.map.MapRegion;
import manatee.client.map.tile.Tilemap;

public class HeightFieldRenderer extends BaseRenderer
{
	private Vector3f lightColor;
	private Vector3f lightVector;
	
	public HeightFieldRenderer(Vector3f lightColor, Vector3f lightVector)
	{
		shader = new Shader("scene/heightfield.vsh", "scene/heightfield.fsh");
		
		this.lightColor = lightColor;
		this.lightVector = lightVector;
	}
	
	@Override
	public void begin(Matrix4f projectionView)
	{
		super.begin(projectionView);
		
		shader.setUniform("v_AmbientColor", lightColor);
		shader.setUniform("v_AmbientVector", lightVector);
	}
	
	public void draw(Matrix4f projectionView, Tilemap tileMap, Collection<MapRegion> regions)
	{
		begin(projectionView);
		
		shader.setTexture("v_Diffuse", tileMap.getTileAssets().getTexture("terrain"), 0);
		for(MapRegion region : regions)
		{
			shader.setUniform("v_Stride", (float)region.getXResolution());
			shader.setUniform("v_Resolution", (float)region.getSpacing());
			shader.setUniform("v_Position", (float)region.getPosition().x, (float)region.getPosition().y);

			region.passLights(shader);
			
			IMesh mesh = region.getHeightMesh();
			
			mesh.bind();
			
			glDrawArrays(GL_TRIANGLE_STRIP, 0, mesh.getVertexCount());
			
			mesh.unbind();
		}
		
		end();
	}
	
	@Override
	public void dispose()
	{
		super.dispose();
	}
}
