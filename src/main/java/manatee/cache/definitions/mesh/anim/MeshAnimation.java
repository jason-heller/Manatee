package manatee.cache.definitions.mesh.anim;

import java.util.List;

public class MeshAnimation
{
	private float duration;
	private AnimFrame[] frames;

	public MeshAnimation(float duration, AnimFrame frame)
	{
		this.duration = duration;
		this.frames = new AnimFrame[] {frame};
	}

	public MeshAnimation(float duration, List<AnimFrame> frames)
	{
		this.duration = duration;
		this.frames = frames.toArray(new AnimFrame[0]);
	}

	public float getDuration()
	{
		return duration;
	}

	public AnimFrame[] getFrames()
	{
		return frames;
	}

}
