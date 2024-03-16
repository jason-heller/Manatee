package manatee.client.gl.renderer.nvg;

import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.nanovg.NanoVG;

import lwjgui.paint.Color;
import manatee.client.gl.camera.ICamera;
import manatee.client.scene.WindowPicker;

public class NVGText implements NVGObject
{
	private Color color = Color.WHITE;
	private Color backgroundColor;
	private String text;
	private Vector3f pos;

	public NVGText(Vector3f pos, String str)
	{
		this.pos = pos;
		this.text = str;
	}
	
	@Override
	public void draw(long ctx, ICamera camera)
	{
		Vector2i v = WindowPicker.worldSpaceToViewportSpace(camera, pos);
		
		if (backgroundColor != null)
		{
			float[] bounds = new float[4];
			NanoVG.nvgTextBounds(ctx, 0, 0, text, bounds);
			
			NanoVG.nvgBeginPath(ctx);
			NanoVG.nvgRect(ctx, v.x, v.y - 15, bounds[2], 18);
			NanoVG.nvgFillColor(ctx, backgroundColor.getNVG());
			NanoVG.nvgFill(ctx);
		}
		
		NanoVG.nvgFillColor(ctx, color.getNVG());
		NanoVG.nvgText(ctx, v.x, v.y, text);
	}

	public String getString()
	{
		return text;
	}

	public Vector3f getPos()
	{
		return pos;
	}

	public Color getColor()
	{
		return color;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}

	public Color getBackgroundColor()
	{
		return backgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor)
	{
		this.backgroundColor = backgroundColor;
	}

	public String getText()
	{
		return text;
	}
	
	public void setText(String text)
	{
		this.text = text;
	}
}
