package lwjgui.scene.image;

import static org.lwjgl.system.MemoryUtil.memFree;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.lwjgl.nanovg.NanoVG;

import lwjgui.LWJGUI;
import lwjgui.scene.Context;

public class Image implements IPicture {

	private int width = -1;
	private int height = -1;

	private String desiredPath;

	private boolean loaded;

	private int image = -1;

	public Image(String filePath) {
		this.desiredPath = filePath;
		
		Context context = LWJGUI.getThreadWindow().getContext();
		if (context == null)
		{
			System.err.println("Image needs UI context to load");
			return;
		}

		if (desiredPath == null)
		{
			System.err.println("Image path cannot be null");
			return;
		}

		long nvg = context.getNVG();

		ByteBuffer data = null;

		try {
			data = Context.ioResourceToByteBuffer(desiredPath, 4 * 1024);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (data == null)
		{
			System.err.println("Failed to load image: " + desiredPath);
			return;
		}

		// Get image
		image = NanoVG.nvgCreateImageMem(nvg, 0, data);

		int[] w = new int[1];
		int[] h = new int[1];
		NanoVG.nvgImageSize(nvg, image, w, h);
		width = w[0];
		height = h[0];
		
		context.loadImage(this);
		
		memFree(data);

		loaded = true;
	}

	public int getImage() {
		return image;
	}

	public boolean isLoaded() {
		return getImage() != -1;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void dispose() {
		if ( loaded ) {
			loaded = false;
			Context context = LWJGUI.getThreadWindow().getContext();
			NanoVG.nvgDeleteImage(context.getNVG(), image);
		}
		
		image = -1;
	}
}
