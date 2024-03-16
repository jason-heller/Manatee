package manatee.client.gl.renderer.nvg;

import org.lwjgl.nanovg.NanoVG;

import lwjgui.paint.Color;
import manatee.client.gl.camera.ICamera;

public class NVGLine implements NVGObject
{
	private Color color;
	private int x1, y1, x2, y2;
	
	public NVGLine(int x1, int y1, int x2, int y2)
	{
		this(Color.WHITE, x1, y1, x2, y2);
	}
	
	public NVGLine(Color color, int x1, int y1, int x2, int y2)
	{
		this.color = color;
		setPoints(x1, y1, x2, y2);
	}

	public Color getColor()
	{
		return color;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}

	public int getX1()
	{
		return x1;
	}

	public void setX1(int x1)
	{
		this.x1 = x1;
	}

	public int getY1()
	{
		return y1;
	}

	public void setY1(int y1)
	{
		this.y1 = y1;
	}

	public int getX2()
	{
		return x2;
	}

	public void setX2(int x2)
	{
		this.x2 = x2;
	}

	public int getY2()
	{
		return y2;
	}

	public void setY2(int y2)
	{
		this.y2 = y2;
	}

	public void draw(long vg, ICamera camera)
	{
		NanoVG.nvgBeginPath(vg);
		NanoVG.nvgMoveTo(vg, x1, y1);
		NanoVG.nvgLineTo(vg, x2, y2);
		NanoVG.nvgStrokeColor(vg, color.getNVG()); // White color
		NanoVG.nvgStrokeWidth(vg, 1.0f);
        NanoVG.nvgStroke(vg);
	}

	public void setPoints(int x1, int y1, int x2, int y2)
	{
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}
}
