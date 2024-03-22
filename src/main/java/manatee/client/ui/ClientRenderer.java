package manatee.client.ui;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.opengl.GL11;

import lwjgui.LWJGUI;
import lwjgui.gl.Renderer;
import lwjgui.paint.Color;
import lwjgui.scene.Context;
import lwjgui.scene.Node;
import lwjgui.scene.Window;
import manatee.client.Client;
import manatee.client.Time;
import manatee.client.dev.Command;
import manatee.client.dev.DeveloperConsole;
import manatee.client.gl.camera.ICamera;
import manatee.client.gl.renderer.nvg.NVGObject;
import manatee.client.scene.IScene;
import manatee.client.scene.MapScene;
import manatee.maths.MCache;
import manatee.primitives.Primitive;
import manatee.primitives.Primitives;

public class ClientRenderer implements Renderer
{
	public static boolean fullbright;

	private IScene scene;
	
	private boolean cullFace = true;
	
	private boolean debugInfoUI;
	private boolean debugInfoScene;
	
	private Primitive[] gizmo = new Primitive[3];
	private Vector3f axisOrigin = new Vector3f();

	public ClientRenderer(IScene scene)
	{
		this.scene = scene;
		Command.add("gl_cullface", Command.BOOL_SYNTAX, this, "setCullFace", false);
		Command.add("uidebug", Command.BOOL_SYNTAX, this, "setDebugUIInfo", false);
		Command.add("show_info", Command.BOOL_SYNTAX, this, "setDebugSceneInfo", false);
		
		Command.add("render_fullbright", Command.BOOL_SYNTAX, this, "setFullbright", false);
	}

	@Override
	public void render(Context context, int width, int height)
	{
		ICamera camera = scene.getCamera();
		
		if (cullFace)
			GL11.glEnable(GL11.GL_CULL_FACE);
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		
		final Vector4f color = scene.getColor();
		GL11.glClearColor(color.x, color.y, color.z, color.w);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		Primitives.render(camera.getProjectionViewMatrix());

		scene.render();
		
		renderSpatialUIComponents(context.getNVG());
		
		// Developer UI
		if(debugInfoScene)
			drawSceneDebugInfo(context.getNVG());
		
		//Time.drawFramerateBox(ctx, 32, 64 + offset);
	}
	
	public void dispose()
	{
		Primitives.dispose();
	}
	
	private void renderSpatialUIComponents(long ctx)
	{
		ICamera camera = scene.getCamera();
		
		// Render gizmo in screenspace
		if (this.debugInfoScene)
		{
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			
			Matrix4f m = new Matrix4f();
			m.translate(-.86f,-.86f,0);
			
			m.rotateX(camera.getPitch());
			m.rotateY(camera.getRoll());
			m.rotateZ(camera.getYaw());

			m.scale(.075f);
			
			Primitives.renderBatch(gizmo[0].getId(), m, gizmo);
		}
		
		// Render NVG objects
		for(NVGObject object : scene.getNVGObjects())
		{
			object.draw(ctx, camera);
		}
	}

	private void drawSceneDebugInfo(long ctx)
	{
		StringBuilder str = new StringBuilder();
		
		Vector3f camPos = scene.getCamera().getPosition();
		
		str.append("cam: ").append(String.format("%.0f", camPos.x))
		.append(" ").append(String.format("%.0f", camPos.y))
		.append(" ").append(String.format("%.0f", camPos.z));
		
		int offset = DeveloperConsole.isVisible() ? 250 : 0;
		NanoVG.nvgText(ctx, 10, 20 + offset, "fps " + Time.fps);
		NanoVG.nvgText(ctx, 10, 40 + offset, str.toString());
		
		if (scene instanceof MapScene)
		{
			Vector3f lightVec = ((MapScene)Client.scene()).getLightVector();
			NanoVG.nvgText(ctx, 10, 60 + offset, "LightVec: " + String.format("%.1f", lightVec.x) + " "
					+ String.format("%.1f", lightVec.y) + " " + String.format("%.1f", lightVec.z));

			Vector3f v = ((MapScene) scene).getMouseWorldPosition();
			if (v != null)
				NanoVG.nvgText(ctx, 10, 80 + offset, String.format("%.0f", v.x) + " " + String.format("%.0f", v.y) + " "
						+ String.format("%.0f", v.z));
		}
	}

	public void drawUIDebugInfo()
	{
		if (!debugInfoUI)
			return;
		
		Window window = LWJGUI.getThreadWindow();
		Node hovered = window.getContext().getHovered();
		
		if (hovered != null)
		{
			long ctx = window.getContext().getNVG();
			window.nvgBegin();

			NanoVG.nvgBeginPath(ctx);
			NanoVG.nvgRect(ctx, (float) hovered.getX(), (float) hovered.getY(), (float) hovered.getWidth(),
					(float) hovered.getHeight());
			NanoVG.nvgStrokeColor(ctx, Color.RED.getNVG());
			NanoVG.nvgStrokeWidth(ctx, 5f);
			NanoVG.nvgStroke(ctx);
			NanoVG.nvgClosePath(ctx);
			
			String s = hovered.getElementType() + " [p=" + hovered.getParent().getElementType() + ']';
			
			NanoVG.nvgFillColor(ctx, Color.BLACK.getNVG());
			NanoVG.nvgText(ctx, (float) hovered.getX() + 1, (float) hovered.getY() + 16, s);
			NanoVG.nvgText(ctx, (float) hovered.getX() + 3, (float) hovered.getY() + 16, s);
			NanoVG.nvgText(ctx, (float) hovered.getX() + 2, (float) hovered.getY() + 15, s);
			NanoVG.nvgText(ctx, (float) hovered.getX() + 2, (float) hovered.getY() + 17, s);
			NanoVG.nvgFillColor(ctx, Color.WHITE.getNVG());
			NanoVG.nvgText(ctx, (float) hovered.getX() + 2, (float) hovered.getY() + 16, s);

			window.nvgEnd();
		}
	}
	
	public void setCullFace(boolean cullFace)
	{
		this.cullFace = cullFace;
	}
	
	public void setDebugUIInfo(boolean debugInfoUI)
	{
		this.debugInfoUI = debugInfoUI;
	}
	
	public void setFullbright(boolean fullbright)
	{
		ClientRenderer.fullbright = fullbright;
	}
	
	public void setDebugSceneInfo(boolean debugInfoScene)
	{
		this.debugInfoScene = debugInfoScene;
		
		if (this.debugInfoScene)
		{
			gizmo[0] = Primitives.addArrow(axisOrigin, new Vector3f(MCache.X_AXIS).mul(.01f), MCache.X_AXIS);
			gizmo[1] = Primitives.addArrow(axisOrigin, new Vector3f(MCache.Y_AXIS).mul(.01f), MCache.Y_AXIS);
			gizmo[2] = Primitives.addArrow(axisOrigin, new Vector3f(MCache.Z_AXIS).mul(.01f), MCache.Z_AXIS);
			
			gizmo[0].setScale(1f);
			gizmo[1].setScale(1f);
			gizmo[2].setScale(1f);
		}
		else
		{
			Primitives.remove(gizmo[0]);
			Primitives.remove(gizmo[1]);
			Primitives.remove(gizmo[2]);
		}
	}
}
