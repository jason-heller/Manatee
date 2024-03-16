package manatee.client.gl.renderer;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;

import java.util.Collection;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

import lwjgui.gl.OffscreenBuffer;
import lwjgui.scene.layout.OpenGLPane;
import manatee.client.Client;
import manatee.client.Time;
import manatee.client.gl.BlendMode;
import manatee.client.gl.Shader;
import manatee.client.gl.camera.ICamera;
import manatee.client.gl.mesh.TileFieldMesh;
import manatee.client.gl.mesh.TileShaderTarget;
import manatee.client.map.MapRegion;
import manatee.client.map.tile.TileAssets;
import manatee.client.scene.MapScene;
import manatee.client.scene.WaterHandler;

public class TileRenderer
{
	private Vector3f lightColor;
	private Vector3f lightVector;
	
	private Shader[] shaders;
	
	private int diffuseFbo;
    private int diffuseFboTex;
    
    private final int FBO_WIDTH = 800, FBO_HEIGHT = 600;
    
    private float tileGlobalTimer = 0f;
	private float shoreDisplacement;
	private float shoreDelta;
	
	public TileRenderer(Vector3f lightColor, Vector3f lightVector)
	{
		shaders = new Shader[] {
				new Shader("scene/tile/generic.vsh", "scene/tile/generic.fsh"),
				new Shader("scene/tile/foliage.vsh", "scene/tile/foliage.fsh"),
				new Shader("scene/tile/water.vsh", "scene/tile/water.fsh"),
				new Shader("scene/tile/translucent.vsh", "scene/tile/translucent.fsh")
		};
		
		this.lightColor = lightColor;
		this.lightVector = lightVector;
		
		createFbo();
	}
	
	public void begin(Shader shader, Matrix4f projectionView)
	{
		shader.bind();
		
		shader.setUniform("v_ProjectionView", projectionView);
	}
	
	public void draw(MapScene scene, Collection<MapRegion> regions)
	{
		Matrix4f projectionView = scene.getCamera().getProjectionViewMatrix();
		WaterHandler water = scene.getMap().getWater();
		
		BlendMode blendMode = scene.getBlendMode();
		
		GL11.glDisable(GL11.GL_BLEND);

		drawGeneric(shaders[0], TileShaderTarget.GENERIC, projectionView, regions);
		drawGeneric(shaders[1], TileShaderTarget.FOLIAGE, projectionView, regions);
		drawWater(shaders[2], scene.getCamera(), water, regions);
		drawTranslucent(shaders[3], scene.getBlendMode(), projectionView, regions);
		
		tileGlobalTimer += Time.deltaTime;
		
		shoreDelta = shoreDisplacement;
		shoreDisplacement = (float)Math.sin(tileGlobalTimer) * water.getWaveAmplitude();
		
		shoreDelta = (shoreDelta - shoreDisplacement) / Time.deltaTime;
	}

	private void drawTranslucent(Shader shader, BlendMode blendMode, Matrix4f projectionView, Collection<MapRegion> regions)
	{
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(blendMode.source(), blendMode.dest());
		drawGeneric(shader, TileShaderTarget.TRANSLUCENT, projectionView, regions);
		GL11.glDisable(GL11.GL_BLEND);
	}

	private void drawWater(Shader shader, ICamera camera, WaterHandler water, Collection<MapRegion> regions)
	{
		// Needed for getting the depth buffer
		OpenGLPane pane = Client.ui().getWorldRenderPane();
		
		if (pane == null || !pane.isVisible())
			return;
		
		// Blit depth buffer to new FBO, so that we don't have the water meshes in the depth buffer
		OffscreenBuffer buffer = pane.getBuffer();
		copyTexture(buffer.getFboId(), buffer.getWidth(), buffer.getHeight());
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		float[] vp = camera.getViewport();
	
		begin(shader, camera.getProjectionViewMatrix());
		
		shader.setUniform("f_WaterColor", water.getColor());
		shader.setUniform("v_Shift", water.getFlowOffset());
		
		shader.setUniform("f_ShoreLine", shoreDisplacement);
		shader.setUniform("f_SeaLevelDelta", shoreDelta);
		
		shader.setUniform("f_Viewport", new Vector2f(vp[2], vp[3]));
		
		bindTexture(shader, "f_Depth", diffuseFboTex, 0);
		shader.setTexture("f_Foam", TileAssets.waterFoam, 1);
		
		draw(shader, TileShaderTarget.WATER, regions, false);
		
		end(shader);
		
		GL11.glDisable(GL11.GL_BLEND);
	}

	private void drawGeneric(Shader shader, TileShaderTarget shaderTarget, Matrix4f projectionView, Collection<MapRegion> regions)
	{
		begin(shader, projectionView);
		
		shader.setUniform("v_AmbientColor", lightColor);
		shader.setUniform("v_AmbientVector", lightVector);
		
		draw(shader, shaderTarget, regions, true);
		
		end(shader);
	}

	private void bindTexture(Shader shader, String string, int depthTexId, int activeTextureIndex)
	{
		shader.setUniform(string, activeTextureIndex);
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + activeTextureIndex);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexId);
	}

	private void draw(Shader shader, TileShaderTarget shaderTarget, Collection<MapRegion> regions, boolean withLights)
	{
		// TODO: There is an optimization here if we batch the rendering by texture
		for(MapRegion region : regions)
		{
			
			for(TileFieldMesh mesh : region.getTileMeshes())
			{
	
				if (mesh == null || mesh.getShaderTarget() != shaderTarget)
					continue;
				
				shader.setTexture("f_Diffuse", mesh.getTexture(), 3);
				shader.setUniform("v_Position", (float)region.getPosition().x, (float)region.getPosition().y);
				
				if (withLights)
				{
					region.passLights(shader);
				}
				
				mesh.bind();
				
				glDrawArrays(GL_TRIANGLES, 0, mesh.getVertexCount());
				
				mesh.unbind();
				
			}
		}
	}
	
	private void end(Shader shader)
	{
		shader.unbind();
	}

	public void dispose()
	{
		for(Shader shader : shaders)
			if (shader != null)
				shader.dispose();
		
		GL11.glDeleteTextures(diffuseFboTex);
		GL30.glDeleteFramebuffers(diffuseFbo);
	}

	private void createFbo()
	{
		int lastFbo = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
		
		diffuseFbo = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, diffuseFbo);
		
		diffuseFboTex = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, diffuseFboTex);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, FBO_WIDTH, FBO_HEIGHT, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, 0);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, diffuseFboTex, 0);
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, lastFbo);
	}
	
	private void copyTexture(int sourceFbo, int srcWidth, int srcHeight)
	{
		int lastFbo = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
		
		int[] origViewport = new int[4];
		GL11.glGetIntegerv(GL11.GL_VIEWPORT, origViewport);
		GL11.glViewport(0, 0, FBO_WIDTH, FBO_HEIGHT);
		
		// Bind default FBO
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

		// Blit the source FBO texture to the destination FBO texture
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, sourceFbo);
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, diffuseFbo);
		GL30.glBlitFramebuffer(0, 0, srcWidth, srcHeight, 0, 0, FBO_WIDTH, FBO_HEIGHT, GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
		
		// Reset
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, lastFbo);
		GL11.glViewport(origViewport[0], origViewport[1], origViewport[2], origViewport[3]);
	}
}
