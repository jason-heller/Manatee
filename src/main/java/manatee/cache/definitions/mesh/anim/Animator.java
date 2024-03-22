package manatee.cache.definitions.mesh.anim;

import java.util.Map;

import org.joml.Matrix4f;

import manatee.cache.definitions.Model;
import manatee.cache.definitions.loader.MeshLoader;
import manatee.client.Time;

public class Animator
{
	private Map<String, MeshAnimation> animations;
	
	private MeshAnimation animation = null;
	
	private int frameId = -1;
	private int nFrames;
	
	private float progress;
	
	private AnimFrame currentFrame;
	
	private float timeScale = 25f;
	
	public Animator(Model model)
	{
		this.animations = model.getAnimations();
		resetCurrentFrame(MeshLoader.MAX_BONES);
	}

	public AnimFrame getCurrentFrame()
	{
		return currentFrame;
	}

	public MeshAnimation getAnimation()
	{
		return animation;
	}

	public void setAnimation(String name)
	{
		this.animation = animations.get(name);

		if (this.animation == null)
			return;
		
		this.nFrames = animation.getFrames().length;
		frameId = -1;
		
		resetCurrentFrame(animation.getFrames()[0].boneMatrices.length);
	}

	private void resetCurrentFrame(int nMatrices)
	{
		Matrix4f[] boneMatrices = new Matrix4f[nMatrices];
		for(int i = 0; i < nMatrices; i++)
			boneMatrices[i] = new Matrix4f();
		currentFrame = new AnimFrame(boneMatrices);
	}

	public int getFrameId()
	{
		return frameId;
	}
	
	public void setFrameId(int frameId)
	{
		this.frameId = frameId;
	}
	
	public void tick()
	{
		if (animation == null)
			return;
		
		float duration = animation.getDuration();
		progress = (progress + (Time.deltaTime * timeScale)) % (duration - 2f);
		float framePos = (progress / duration) * nFrames;
		
		frameId = (int)(framePos);
		int nextFrameId = (frameId + 1) % nFrames;
		
		float frameProgress = framePos - frameId;
		
		// Create lerp frame
		final Matrix4f[] frameMatrices = currentFrame.boneMatrices;
		final int nMatrices = frameMatrices.length;
		
		AnimFrame lastFrame = animation.getFrames()[frameId];
		AnimFrame nextFrame = animation.getFrames()[nextFrameId];
		
		for(int i = 0; i < nMatrices; i++)
		{
			frameMatrices[i].set(lastFrame.boneMatrices[i]);
			frameMatrices[i].lerp(nextFrame.boneMatrices[i], frameProgress);
		}
	}

	public void setCurrentFrame(AnimFrame currentFrame)
	{
		this.currentFrame = currentFrame;
	}
}
