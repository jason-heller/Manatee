package manatee.client.scene.editor.ui;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import lwjgui.gl.Renderer;
import lwjgui.scene.Context;
import lwjgui.scene.layout.OpenGLPane;
import lwjgui.scene.layout.Pane;
import manatee.client.gl.Shader;
import manatee.client.gl.mesh.TileFieldMesh;
import manatee.client.gl.renderer.BaseRenderer;
import manatee.client.map.tile.Tile;
import manatee.maths.Maths;
import manatee.maths.MatrixMath;
import manatee.maths.MCache;

public class TileVisualizer extends BaseRenderer implements Renderer
{
	private TileFieldMesh mesh;
	
	private Matrix4f matrix;
	
	private final Vector3f CAM_OFFSET = new Vector3f(0.5f, 0f, 0.5f);
	private final Vector3f CAM_LIGHT = new Vector3f(0.5f, -0.5f, 0f);
	
	private float lookAngle;
	private float zoom;
	
	private double lastX = Double.NaN;
	
	public TileVisualizer(Pane pane, int w, int h)
	{
		OpenGLPane ogl = new OpenGLPane();
		ogl.setPrefSize(w, h);
		ogl.setRendererCallback(this);
		
		pane.getChildren().add(ogl);
		
		shader = new Shader("shader/tile/generic.vsh", "shader/tile/generic.fsh");
		
		matrix = new Matrix4f();
		resetView();
		
		ogl.setOnMouseDragged((e) -> {
			if (Double.isNaN(lastX))
			{
				lastX = (float) e.getMouseX();
			}
			else {
				lookAngle += ((float) e.getMouseX() - lastX) / 100f;
				lastX = e.getMouseX();
				setView();
			}
		});
		
		ogl.setOnMouseScrolled((e) -> {
			zoom = Maths.clamp(zoom + (float) e.y, 1f, 5f);
			setView();
		});
	}
	
	public void resetView()
	{
		lookAngle = 0f;
		zoom = 2f;
		setView();
	}
	
	public void setView()
	{
		matrix.identity();
		Matrix4f view = new Matrix4f();

		MatrixMath.setProjectionMatrix(matrix, 90f, 1f, 0.1f, 100f);

		Vector3f look = new Vector3f(0, -zoom, 0);
		look.rotateZ(lookAngle);
		look.add(CAM_OFFSET);
		
		MatrixMath.setViewMatrix(view, look, -lookAngle, -Maths.HALFPI, 0);
		matrix.mul(view);
	}

	@Override
	public void render(Context context, int width, int height)
	{
		//matrix.set(Client.scene().getCamera().getProjectionViewMatrix());
		
		if (mesh == null)
			return;

		begin(matrix);
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
		GL11.glClearColor(0, 0, 1, 1);
		
		shader.setTexture("f_Diffuse", mesh.getTexture(), 0);
		shader.setUniform("v_Position", 0, 0);
		shader.setUniform("v_AmbientColor", MCache.ONE);
		shader.setUniform("v_AmbientVector", CAM_LIGHT);
		shader.setUniform("v_LightNum", 0);
		
		mesh.bind();
		
		shader.setTexture("f_Diffuse", mesh.getTexture(), 0);
		 
		glDrawArrays(GL_TRIANGLES, 0, mesh.getVertexCount());
		
		mesh.unbind();
		
		end();
	}
	
	public void dispose()
	{
		if (mesh != null)
			mesh.dispose();
	}
	
	public void setTile(Tile tile)
	{
		resetView();
		
		if (mesh != null)
			mesh.dispose();
		
		mesh = TileFieldMesh.build(null, tile);
	}
}
