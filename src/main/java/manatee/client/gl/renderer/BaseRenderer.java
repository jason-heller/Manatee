package manatee.client.gl.renderer;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import manatee.cache.definitions.mesh.IMesh;
import manatee.client.gl.Shader;

public abstract class BaseRenderer
{
	protected Shader shader;
	
	public void begin(Matrix4f projectionView)
	{
		shader.bind();
		
		shader.setUniform("v_ProjectionView", projectionView);
	}
	
	public void begin(Matrix4f projection, Matrix4f view)
	{
		shader.bind();
		
		shader.setUniform("v_Projection", projection);
		shader.setUniform("v_View", view);
	}
	
	protected void drawMesh(IMesh mesh)
	{
		mesh.bind();
		
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, mesh.getVertexCount());
		
		mesh.unbind();
	}
	
	protected void drawMeshIndexed(IMesh mesh)
	{
		mesh.bind();
		
		GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
		
		mesh.unbind();
	}
	
	public void end()
	{
		shader.unbind();
	}
	
	public void dispose()
	{
		shader.dispose();
	}
}
