package manatee.client.gl.renderer.nvg;

import lwjgui.paint.Color;
import manatee.client.gl.camera.ICamera;

public interface NVGObject
{
	public Color getColor();

	public void setColor(Color color);
	
	public void draw(long vg, ICamera camera);
}
