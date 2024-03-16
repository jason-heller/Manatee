package manatee.client.audio;

import static org.lwjgl.openal.AL10.AL_BUFFER;
import static org.lwjgl.openal.AL10.AL_FALSE;
import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.AL10.AL_LOOPING;
import static org.lwjgl.openal.AL10.AL_MAX_DISTANCE;
import static org.lwjgl.openal.AL10.AL_PITCH;
import static org.lwjgl.openal.AL10.AL_PLAYING;
import static org.lwjgl.openal.AL10.AL_POSITION;
import static org.lwjgl.openal.AL10.AL_REFERENCE_DISTANCE;
import static org.lwjgl.openal.AL10.AL_ROLLOFF_FACTOR;
import static org.lwjgl.openal.AL10.AL_SOURCE_STATE;
import static org.lwjgl.openal.AL10.AL_TRUE;
import static org.lwjgl.openal.AL10.AL_VELOCITY;
import static org.lwjgl.openal.AL10.alDeleteSources;
import static org.lwjgl.openal.AL10.alGenSources;
import static org.lwjgl.openal.AL10.alGetSourcei;
import static org.lwjgl.openal.AL10.alSource3f;
import static org.lwjgl.openal.AL10.alSourcePause;
import static org.lwjgl.openal.AL10.alSourcePlay;
import static org.lwjgl.openal.AL10.alSourceStop;
import static org.lwjgl.openal.AL10.alSourcef;
import static org.lwjgl.openal.AL10.alSourcei;
import static org.lwjgl.openal.AL11.AL_SAMPLE_OFFSET;
import static org.lwjgl.openal.AL11.alSource3i;
import static org.lwjgl.openal.EXTEfx.AL_AUXILIARY_SEND_FILTER;
import static org.lwjgl.openal.EXTEfx.AL_DIRECT_FILTER;
import static org.lwjgl.openal.EXTEfx.AL_EFFECTSLOT_NULL;
import static org.lwjgl.openal.EXTEfx.AL_FILTER_NULL;

import org.joml.Vector3f;

import manatee.cache.definitions.sound.Sound;

public class Source
{
	private final int sourceId;
	private float vol = 1.0f;
	// private int bufferOffset = 0;
	private boolean paused = false;
	private boolean looping = false;
	private int bufferOffset;

	private int soundId;

	public Source()
	{
		sourceId = alGenSources();

		// defaultAttenuation();
		alSourcef(sourceId, AL_ROLLOFF_FACTOR, 0f);
		update();
	}

	public void applyEffect(SoundEffects effect)
	{
		alSource3i(sourceId, AL_AUXILIARY_SEND_FILTER, AudioHandler.getEffects().get(effect).getSlot(), 0,
				AL_FILTER_NULL);
		// alSourcei(sourceId, AL_SAMPLE_OFFSET, bufferOffset);
	}

	public void applyFilter(SoundFilters filter)
	{
		alSourcei(sourceId, AL_DIRECT_FILTER, AudioHandler.getFilters().get(filter));
		// alSourcei(sourceId, AL_SAMPLE_OFFSET, bufferOffset);
	}

	public void defaultAttenuation()
	{
		setAttenuation(5f, 10f, 40f);
	}

	public void delete()
	{
		stop();
		alSourceStop(sourceId);
		alDeleteSources(sourceId);
	}

	public boolean isPlaying()
	{
		return alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PLAYING;
	}

	public void pause()
	{
		bufferOffset = alGetSourcei(sourceId, AL_SAMPLE_OFFSET);
		// soundId = alGetSourcei(sourceId, AL_BUFFER);
		alSourcePause(sourceId);
		paused = true;
	}

	void play(int soundId, boolean looping)
	{
		this.looping = looping;
		stop();

		this.soundId = soundId;

		if (soundHasVariance(soundId))
		{
			setPitch(.8f + (float) Math.random() * .4f);
			setGain(1f);
			alSourcei(sourceId, AL_BUFFER, (soundId & 0xffff));
			alSourcePlay(sourceId);

		}
		else
		{
			setPitch(1f);
			setGain(1f);
			alSourcei(sourceId, AL_BUFFER, (soundId & 0xffff));
			alSourcePlay(sourceId);

		}
	}

	public void play(Sound sound)
	{
		play(sound.getId(), false);
	}

	public static boolean soundHasVariance(int buffer)
	{
		return ((buffer & 0x100000) != 0);
	}

	public void removeEffect()
	{
		alSource3i(sourceId, AL_AUXILIARY_SEND_FILTER, AL_EFFECTSLOT_NULL, 0, AL_FILTER_NULL);
	}

	public void removeFilter()
	{
		alSourcei(sourceId, AL_DIRECT_FILTER, AL_FILTER_NULL);
	}

	public void setAttenuation(float rolloffFactor, float referenceDistance)
	{
		alSourcef(sourceId, AL_ROLLOFF_FACTOR, rolloffFactor);
		alSourcef(sourceId, AL_REFERENCE_DISTANCE, referenceDistance);
	}

	public void setAttenuation(float rolloffFactor, float referenceDistance, float maxDistance)
	{
		alSourcef(sourceId, AL_ROLLOFF_FACTOR, rolloffFactor);
		alSourcef(sourceId, AL_REFERENCE_DISTANCE, referenceDistance);
		alSourcef(sourceId, AL_MAX_DISTANCE, maxDistance);
	}

	public void setGain(float vol)
	{
		this.vol = vol;
		alSourcef(sourceId, AL_GAIN,
				AudioHandler.volume * vol * (looping ? AudioHandler.musicVolume : AudioHandler.sfxVolume));
	}

	public void setLooping(boolean loop)
	{
		alSourcei(sourceId, AL_LOOPING, loop ? AL_TRUE : AL_FALSE);
	}

	public void setPitch(float pitch)
	{
		alSourcef(sourceId, AL_PITCH, pitch);
	}

	public void setPosition(Vector3f pos)
	{
		alSource3f(sourceId, AL_POSITION, pos.x, pos.y, pos.z);
	}

	public void setVelocity(Vector3f vel)
	{
		alSource3f(sourceId, AL_VELOCITY, vel.x, vel.y, vel.z);
	}

	public void stop()
	{
		alSourceStop(sourceId);
		removeEffect();
	}

	public void unpause()
	{
		if (paused)
		{
			alSourcePlay(sourceId);
			alSourcei(sourceId, AL_SAMPLE_OFFSET, bufferOffset);
			paused = false;
		}
	}

	public void update()
	{
		alSourcef(sourceId, AL_GAIN,
				AudioHandler.volume * vol * (looping ? AudioHandler.musicVolume : AudioHandler.sfxVolume));
	}

	public int getSoundId()
	{
		return soundId;
	}
}
