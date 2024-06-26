package lwjgui.gl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

import lwjgui.LWJGUI;
import lwjgui.paint.Color;
import lwjgui.scene.Context;

public class OffscreenBuffer {
	private int width = 0;
	private int height = 0;
	private int colorTexId = 0;
	private int depthTexId = 0;
	
	private int fboId = 0;
	private int renderId = 0;
	
	protected boolean quadDirty = true;
	protected TexturedQuad quad = null;
	protected GenericShader quadShader = null;
	
	private boolean hasDepthTexture = false;
	
	public OffscreenBuffer(int width, int height, boolean hasDepthTexture) {
		
		if (width <= 0 || height <= 0) {
			throw new IllegalArgumentException(String.format("invalid size: %dx%d", width, height));
		}
		
		this.hasDepthTexture = true;
		
		// lazily create the quad and shader,
		// in case we want to render this buf in a different context than the one we created it in
		// (vertex arrays aren't shared between contexts, so neither are quads)
		quad = null;
		quadShader = null;
		resize(width, height);
	}
	
	/**
	 * Width of the buffer
	 * @return
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Height of the buffer
	 * @return
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * Resize the buffer to desired width/height
	 * @param width
	 * @param height
	 * @return
	 */
	public boolean resize(int width, int height) {
		if (this.width == width && this.height == height) {
			return false;
		}
		
		this.width = width;
		this.height = height;
	
		// resize the texture
		if (colorTexId != 0) {
			GL11.glDeleteTextures(colorTexId);
			
			if (hasDepthTexture && depthTexId != 0) {
				GL11.glDeleteTextures(depthTexId);
				depthTexId = 0;
			}
			
			GL30.glDeleteFramebuffers(fboId);
			colorTexId = 0;
			fboId = 0;
		}
		
		if (!hasDepthTexture)
		{
			GL30.glDeleteRenderbuffers(renderId);
			renderId = 0;
		}
		
		// Create texture
		if ( colorTexId == 0 )
			colorTexId = GL11.glGenTextures();
		if ( hasDepthTexture && depthTexId == 0 )
			depthTexId = GL11.glGenTextures();
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexId);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, 0);
		
		// Set default filtering
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		
		// update the framebuf
		if (fboId == 0)
			fboId = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboId);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, colorTexId, 0);
		
		if (hasDepthTexture) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexId);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (java.nio.ByteBuffer) null);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
			GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, depthTexId, 0);
		}
		else
		{
		
			// The depth buffer
			if ( renderId == 0 )
				renderId = GL30.glGenRenderbuffers();
			GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, renderId);
			GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, width, height);
			GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, renderId);
		}
		
		// remove the old quad
		quadDirty = true;
		

		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		
		return true;
	}
	
	/**
	 * OpenGL Color Texture ID
	 * @return
	 */
	public int getColorTexId() {
		return colorTexId;
	}
	
	/**
	 * OpenGL Depth Texture ID
	 * @return
	 */
	public int getDepthTexId() {
		return depthTexId;
	}
	
	/**
	 * OpenGL FBO ID
	 * @return
	 */
	public int getFboId() {
		return fboId;
	}
	
	/**
	 * Bind buffer
	 */
	private int unbindTo = -1;
	public void bind() {
		unbindTo = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, getFboId());
	}
	
	/**
	 * Unbind the buffer
	 */
	public void unbind() {
		if ( unbindTo == -1 )
			return;
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, unbindTo);
	}
	
	public void render(Context context) {
		render(context, 0, 0);
	}
	
	public void render(Context context, int x, int y) {
		render(context, x, y, width, height);
	}
	
	public void render(Context context, int x, int y, int w, int h) {
		if (quadShader == null) {
			quadShader = new GenericShader();
		}
		float pixelRatio = LWJGUI.getThreadWindow().getPixelRatio();
		x *= pixelRatio;
		y *= pixelRatio;
		GL11.glViewport(x, y,(int) (w*pixelRatio),(int) (h*pixelRatio));
		quadShader.bind();
		quadShader.projectOrtho(0, 0, w, h);
		
		if (quadDirty) {
			quadDirty = false;
			if (quad != null) {
				quad.cleanup();
			}
			quad = new TexturedQuad(0, 0, w, h, colorTexId);
		}
		if ( context.isCoreOpenGL() ) {
			if ( quad != null ) {
				quad.render();
			}
		} else {

			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexId);
			
			GL11.glBegin(GL11.GL_QUADS);
				GL11.glColor3f(1.0f, 1.0f, 1.0f);
				GL11.glTexCoord2f(0, 0);
				GL11.glVertex2f(0, 0);

				GL11.glColor3f(1.0f, 1.0f, 1.0f);
				GL11.glTexCoord2f(1, 0);
				GL11.glVertex2f(w, 0);

				GL11.glColor3f(1.0f, 1.0f, 1.0f);
				GL11.glTexCoord2f(1, 1);
				GL11.glVertex2f(w, h);

				GL11.glColor3f(1.0f, 1.0f, 1.0f);
				GL11.glTexCoord2f(0, 1);
				GL11.glVertex2f(0, h);
			GL11.glEnd();
		}
	}
	
	public void cleanup() {
		GL11.glDeleteTextures(colorTexId);
		if (depthTexId != 0)
			GL11.glDeleteTextures(colorTexId);
		
		if (fboId != 0) {
			GL30.glDeleteFramebuffers(fboId);
		}
		if (quad != null) {
			quad.cleanup();
		}
		if (quadShader != null) {
			quadShader.cleanup();
		}
	}

	public TexturedQuad getQuad() {
		return quad;
	}

	public void drawClearColor(Color color) {
		GL11.glClearColor(color.getRedF(), color.getGreenF(), color.getBlueF(), color.getAlphaF());
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
	}

	public void drawClearColorDepth(Color color) {
		GL11.glClearColor(color.getRedF(), color.getGreenF(), color.getBlueF(), color.getAlphaF());
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
	}
	
	public void drawClearDepth() {
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
	}
}
