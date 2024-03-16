package manatee.client.audio;

import static org.lwjgl.openal.ALC10.alcCloseDevice;
import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcDestroyContext;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;
import static org.lwjgl.openal.EXTEfx.AL_EFFECTSLOT_EFFECT;
import static org.lwjgl.openal.EXTEfx.AL_EFFECT_ECHO;
import static org.lwjgl.openal.EXTEfx.AL_EFFECT_REVERB;
import static org.lwjgl.openal.EXTEfx.AL_EFFECT_TYPE;
import static org.lwjgl.openal.EXTEfx.AL_FILTER_LOWPASS;
import static org.lwjgl.openal.EXTEfx.AL_FILTER_TYPE;
import static org.lwjgl.openal.EXTEfx.AL_LOWPASS_GAIN;
import static org.lwjgl.openal.EXTEfx.AL_LOWPASS_GAINHF;
import static org.lwjgl.openal.EXTEfx.AL_REVERB_DECAY_TIME;
import static org.lwjgl.openal.EXTEfx.alAuxiliaryEffectSloti;
import static org.lwjgl.openal.EXTEfx.alDeleteAuxiliaryEffectSlots;
import static org.lwjgl.openal.EXTEfx.alDeleteEffects;
import static org.lwjgl.openal.EXTEfx.alDeleteFilters;
import static org.lwjgl.openal.EXTEfx.alEffectf;
import static org.lwjgl.openal.EXTEfx.alEffecti;
import static org.lwjgl.openal.EXTEfx.alFilterf;
import static org.lwjgl.openal.EXTEfx.alFilteri;
import static org.lwjgl.openal.EXTEfx.alGenAuxiliaryEffectSlots;
import static org.lwjgl.openal.EXTEfx.alGenEffects;
import static org.lwjgl.openal.EXTEfx.alGenFilters;
import static org.lwjgl.openal.EXTThreadLocalContext.alcSetThreadContext;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3f;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;

import manatee.cache.definitions.sound.Sound;
import manatee.client.Client;
import manatee.client.dev.Command;
import manatee.client.gl.camera.ICamera;

public class AudioHandler
{

	private static Map<SoundEffects, SoundEffect> effects = new HashMap<SoundEffects, SoundEffect>();
	private static Map<SoundFilters, Integer> filters = new HashMap<SoundFilters, Integer>();

	private static final int MAX_SOURCES = 80;
	private static final int MAX_STATIC_SOURCES = 32;
	private static final int MAX_LOOPING_SOURCES = 8;
	private static Source[] sources = new Source[MAX_SOURCES];
	private static int sourcePtr = 0, sourceLoopPtr = MAX_STATIC_SOURCES - MAX_LOOPING_SOURCES,
			sourceCheckoutPtr = MAX_STATIC_SOURCES;

	public static float volume = 0.5f, sfxVolume = 1.0f, musicVolume = 1.0f;

	private static long device;
	private static long context;

	public static Source play(Sound sound)
	{
		Source src = sources[sourcePtr++];
		src.play(sound);
		if (sourcePtr == MAX_STATIC_SOURCES - MAX_LOOPING_SOURCES)
			sourcePtr = 0;
		return src;
	}

	public static Source checkoutSource()
	{
		Source src = sources[sourceCheckoutPtr++];
		if (sourcePtr == MAX_SOURCES)
			sourcePtr = MAX_STATIC_SOURCES;
		return src;
	}

	public static Source loop(Sound sound)
	{
		Source src = sources[sourceLoopPtr++];
		src.play(sound);
		if (sourceLoopPtr == MAX_STATIC_SOURCES)
			sourceLoopPtr = MAX_STATIC_SOURCES - MAX_LOOPING_SOURCES;
		return src;
	}

	public static void changeMasterVolume()
	{
		for (final Source source : sources)
		{
			source.update();
		}
	}

	public static void dispose()
	{

		for (Source source : sources)
		{
			source.delete();
		}

		for (final SoundEffect sfx : getEffects().values())
		{
			alDeleteEffects(sfx.getId());
			alDeleteAuxiliaryEffectSlots(sfx.getSlot());
		}

		for (final int filter : getFilters().values())
		{
			alDeleteFilters(filter);
		}

		alcSetThreadContext(NULL);

		if (context != NULL)
			alcDestroyContext(context);

		if (device != NULL)
			alcCloseDevice(device);
	}

	public static Map<SoundEffects, SoundEffect> getEffects()
	{
		return effects;
	}

	public static Map<SoundFilters, Integer> getFilters()
	{
		return filters;
	}

	public static void init()
	{
		try
		{
			device = alcOpenDevice((ByteBuffer) null);
			if (device == 0)
				throw new IllegalStateException("Failed to open the default OpenAL device.");

			context = alcCreateContext(device, (IntBuffer) null);

			if (context == 0)
				throw new IllegalStateException("Failed to create OpenAL context.");

			ALCCapabilities deviceCaps = ALC.createCapabilities(device);
			alcMakeContextCurrent(context);
			AL.createCapabilities(deviceCaps);

			AL10.alDistanceModel(AL10.AL_INVERSE_DISTANCE_CLAMPED);

			setupEffects();
			setupFilters();
			// Thread.sleep(50);

			int i = 0;
			for (; i < MAX_STATIC_SOURCES - MAX_LOOPING_SOURCES; i++)
			{
				sources[i] = new Source();
			}
			for (; i < MAX_STATIC_SOURCES; i++)
			{
				sources[i] = new Source();
				sources[i].setLooping(true);
			}
			for (; i < MAX_SOURCES; i++)
			{
				sources[i] = new Source();
			}

			Command.add("sfx_test", "soundname", AudioHandler.class, "playAsset", false);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void playAsset(String name)
	{
		play(Client.scene().getAssets().getSound(name));
	}

	public static void pause()
	{
		for (int i = 0; i < sources.length; i++)
		{
			final Source s = sources[i];
			if (s.isPlaying())
			{
				s.pause();
			}
		}
	}

	private static void setupEffects()
	{
		int effect, slot;

		// Echo
		effect = alGenEffects();
		slot = alGenAuxiliaryEffectSlots();
		effects.put(SoundEffects.ECHO, new SoundEffect(effect, slot));

		alEffecti(effect, AL_EFFECT_TYPE, AL_EFFECT_ECHO);
		// alEffectf(effect, AL_ECHO_DELAY, 5.0f);
		alAuxiliaryEffectSloti(slot, AL_EFFECTSLOT_EFFECT, effect);

		effect = alGenEffects();
		slot = alGenAuxiliaryEffectSlots();
		effects.put(SoundEffects.REVERB, new SoundEffect(effect, slot));

		alEffecti(effect, AL_EFFECT_TYPE, AL_EFFECT_REVERB);
		alEffectf(effect, AL_REVERB_DECAY_TIME, 2.0f);
		alAuxiliaryEffectSloti(slot, AL_EFFECTSLOT_EFFECT, effect);
	}

	private static void setupFilters() throws Exception
	{
		int filter;

		// Low Pass Freq
		filter = alGenFilters();
		filters.put(SoundFilters.LOW_PASS_FREQ, filter);

		alFilteri(filter, AL_FILTER_TYPE, AL_FILTER_LOWPASS);
		alFilterf(filter, AL_LOWPASS_GAIN, 0.5f);
		alFilterf(filter, AL_LOWPASS_GAINHF, 0.5f);

		filter = alGenFilters();
		filters.put(SoundFilters.LOW_PASS_FILTER, filter);
		alFilteri(filter, AL_FILTER_TYPE, AL_FILTER_LOWPASS);
		alFilterf(filter, AL_LOWPASS_GAIN, 0.5f);
		alFilterf(filter, AL_LOWPASS_GAINHF, 0.5f);
	}

	public static void underwater(boolean submerged)
	{
		if (submerged)
		{
			for (final Source s : sources)
			{
				s.applyFilter(SoundFilters.LOW_PASS_FREQ);
			}
		}
		else
		{
			for (final Source s : sources)
			{
				s.removeFilter();
			}
		}

	}

	public static void unpause()
	{
		for (final Source s : sources)
		{
			// s.removeEffect();
			s.unpause();
		}
	}

	public static void update(ICamera camera)
	{
		final Vector3f p = camera.getPosition();
		AL10.alListener3f(AL10.AL_POSITION, p.x, p.y, p.z);
		AL10.alListener3f(AL10.AL_VELOCITY, 0, 0, 0);
	}

	public static void stop(Sound sound)
	{
		int soundId = sound.getId();
		for (int i = MAX_STATIC_SOURCES - MAX_LOOPING_SOURCES; i < MAX_STATIC_SOURCES; i++)
		{
			if (sources[i].getSoundId() == soundId)
			{
				sources[i].stop();
			}
		}
	}

	public static void stopAll()
	{
		for (int i = 0; i < MAX_STATIC_SOURCES; i++)
		{
			sources[i].stop();
		}
	}

}
