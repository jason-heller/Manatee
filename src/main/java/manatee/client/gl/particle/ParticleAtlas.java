package manatee.client.gl.particle;

import manatee.cache.definitions.texture.ITexture;

public class ParticleAtlas
{
	private ITexture texture;
	
	private boolean additiveBlending = false;
	private boolean lit = false;
	
	private int order;
	private float scale;
	
	public ParticleAtlas(ITexture texture, int widthInFrames, boolean additiveBlending, boolean lit)
	{
		this.lit = lit;
		this.texture = texture;
		this.additiveBlending = additiveBlending;

		this.order = widthInFrames;
		this.scale = 1f / widthInFrames;
	}

	public float getScale()
	{
		return scale;
	}

	public ITexture getTexture()
	{
		return texture;
	}
	
	public boolean hasAdditiveBlending()
	{
		return additiveBlending;
	}

	public void setAdditiveBlending(boolean additiveBlending)
	{
		this.additiveBlending = additiveBlending;
	}

	public boolean isLit()
	{
		return lit;
	}

	public void setLit(boolean lit)
	{
		this.lit = lit;
	}

	public void getTextureCoords(int orderIndex, float lifeFactor, float[] data, int index)
	{
		int nFrames = (order * order) - 1;
		
		int atlasIndex = (int) (lifeFactor * nFrames);
		
		int column = atlasIndex % order;
		int row = atlasIndex / order;

		float fOrder = (float) order;
		
		data[index + 0] = column / fOrder;
		data[index + 1] = row / fOrder;
		
		atlasIndex++;
		column = atlasIndex % order;
		
		data[index + 2] = column / fOrder;
		data[index + 3] = row / fOrder;
		
		data[index + 4] = lifeFactor;
		
	}
}
