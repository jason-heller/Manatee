package lwjgui.scene.image;

import static org.lwjgl.system.MemoryUtil.memFree;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.lwjgl.nanovg.NanoVG;

import lwjgui.LWJGUI;
import lwjgui.scene.Context;

public class ImageDirect implements IPicture {

	private int width = -1;
	private int height = -1;

	private String desiredPath;

	private boolean loaded;

	private int image = -1;

	public ImageDirect(String filePath) {
		setImage(filePath);
	}

	private void setImage(String filePath)
	{
		this.desiredPath = filePath;
		
		Context context = LWJGUI.getThreadWindow().getContext();
		if (context == null)
			throw new IllegalStateException("There is no OpenGL context current in the current thread");

		if (desiredPath == null)
			throw new NullPointerException("Cannot set 'filePath' because 'desiredPath' is null");

		long nvg = context.getNVG();

		ByteBuffer data = null;

		try {
			data = Context.ioResourceToByteBuffer(desiredPath, 4 * 1024);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (data == null)
			return;

		// Get image
		image = NanoVG.nvgCreateImageMem(nvg, 0, data);
		memFree(data);
		int[] w = new int[1];
		int[] h = new int[1];
		NanoVG.nvgImageSize(nvg, image, w, h);
		width = w[0];
		height = h[0];
		
		context.loadImage(this);

		loaded = true;
	}

	public int getImage() {
		if (loaded)
			return image;
		
		return -1;
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
