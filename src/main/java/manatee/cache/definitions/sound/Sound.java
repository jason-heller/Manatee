package manatee.cache.definitions.sound;

import static org.lwjgl.openal.AL10.*;

import java.nio.ShortBuffer;

import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryUtil;

import manatee.cache.definitions.IAsset;
import manatee.cache.definitions.loader.AudioLoader;

public class Sound implements IAsset
{
	private int id;
	private ShortBuffer pcm;
	
	public Sound(String filePath)
	{
		id = alGenBuffers();
		
		try (STBVorbisInfo info = STBVorbisInfo.malloc())
		{
			pcm = AudioLoader.loadAudio(filePath, info);
			alBufferData(id, info.channels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, pcm, info.sample_rate());
		}
	}

	@Override
	public void dispose()
	{
		alDeleteBuffers(id);
		
		if (pcm != null)
			MemoryUtil.memFree(pcm);
	}

	@Override
	public int getId()
	{
		return id;
	}

}
