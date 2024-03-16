package manatee.cache.definitions.loader;

import static org.lwjgl.stb.STBVorbis.stb_vorbis_close;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_get_info;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_get_samples_short_interleaved;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_open_filename;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_stream_length_in_samples;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class AudioLoader
{
	public static ShortBuffer loadAudio(String filePath, STBVorbisInfo info)
	{
		long decoder = NULL;

		try (MemoryStack stack = stackPush())
		{
			IntBuffer error = stack.mallocInt(1);
			decoder = stb_vorbis_open_filename(filePath, error, null);

			if (decoder == NULL)
				throw new RuntimeException("Failed to open Ogg Vorbis file. Error: " + error.get(0));
			
			stb_vorbis_get_info(decoder, info);
			
			int channels = info.channels();
			int lengthSamples = stb_vorbis_stream_length_in_samples(decoder);
			
			ShortBuffer result = MemoryUtil.memAllocShort(lengthSamples * channels);
			
			result.limit(stb_vorbis_get_samples_short_interleaved(decoder, channels, result) * channels);
			stb_vorbis_close(decoder);
			
			return result;
		}
	}
}
