package manatee.cache.definitions.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class ResourceLoader
{
	private static final Logger logger = (Logger) LoggerFactory.getLogger("IO");
	
	public static String asString(String path)
	{
		StringBuilder result = new StringBuilder();

		try (BufferedReader reader = getReader(path))
		{
			String line = "";
			while ((line = reader.readLine()) != null)
			{
				result.append(line).append("\n");
			}
		}
		catch (IOException e)
		{
			logger.error("Couldn't find the file at " + path);
		}
		catch (NullPointerException e)
		{
			logger.error("Null pointer exception " + path);
			logger.error("Path: " + path);
		}

		return result.toString();
	}

	public static BufferedReader getReader(String path)
	{
		final InputStream inputStream = getInputStream(path);
		
		if (inputStream == null)
			return null;
		
		final InputStreamReader isr = new InputStreamReader(inputStream);
		final BufferedReader reader = new BufferedReader(isr);
		
		return reader;
	}
	
	public static InputStream getInputStream(String path)
	{
		try
		{
			final ClassLoader classLoader = ResourceLoader.class.getClassLoader();
			final InputStream inputStream = classLoader.getResourceAsStream(path);
			
			return inputStream;
		}
		catch (NullPointerException e)
		{
			logger.error("Failed to get input stream: path is null");
			return null;
		}
		catch (SecurityException e)
		{
			logger.error("Security Exception, Failed to get input stream for: " + path);
			return null;
		}
	}
	
	/**
	 * Reads the specified resource and returns the raw data as a ByteBuffer.
	 *
	 * @param resource   the resource to read
	 * @param bufferSize the initial buffer size
	 *
	 * @return the resource data
	 *
	 * @throws IOException if an IO error occurs
	 */
	public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize)
	{
		try
		{
			return ioResourceToByteBufferUnsafe(resource, bufferSize);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private static ByteBuffer ioResourceToByteBufferUnsafe(String resource, int bufferSize) throws IOException
	{
		ByteBuffer buffer;

		Path path = Paths.get(resource);
		if (path != null && Files.isReadable(path))
		{
			try (SeekableByteChannel fc = Files.newByteChannel(path))
			{
				buffer = BufferUtils.createByteBuffer((int) fc.size() + 1);
				while (fc.read(buffer) != -1) {}
			}
		}
		else
		{
			InputStream source = getInputStream(resource);
			
			if (source == null)
				return null;

			ReadableByteChannel rbc = Channels.newChannel(source);
			buffer = BufferUtils.createByteBuffer(bufferSize);

			while (true)
			{
				int bytes = rbc.read(buffer);
				
				if (bytes == -1)
					break;

				if (buffer.remaining() == 0)
					buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2); // 50%
			}
		}

		buffer.flip();
		return MemoryUtil.memSlice(buffer);
	}

	private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity)
	{
		ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
		buffer.flip();
		newBuffer.put(buffer);
		return newBuffer;
	}
}
