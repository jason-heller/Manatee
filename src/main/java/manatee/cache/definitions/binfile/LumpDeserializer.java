package manatee.cache.definitions.binfile;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class LumpDeserializer
{
	public static Collection<Object> deserialize(byte[] lump, Class<?> lumpClass) throws IOException
	{

		ByteArrayInputStream bais = new ByteArrayInputStream(lump);
		DataInputStream dis = new DataInputStream(bais);
		
		List<Object> result = new ArrayList<>();
		
		while(dis.available() > 0)
		{
			try
			{
				Field[] fields = lumpClass.getFields();

				try
				{
					result.add(lumpClass.getConstructors()[0].newInstance());
				}
				catch (InstantiationException | InvocationTargetException | SecurityException e)
				{
					e.printStackTrace();
					break;
				}
				
				for (Field field : fields)
				{
					field.set(result, readToField(dis, field.get(lump)));
				}

			}
			catch (IllegalArgumentException | IllegalAccessException | IOException e)
			{
				e.printStackTrace();
			}
		}
		
		dis.close();
		
		return result;
	}

	@SuppressWarnings("unchecked")
	private static Object readToField(DataInputStream dis, Object value)
			throws IOException, IllegalArgumentException, IllegalAccessException
	{
		if (value.getClass().isArray())
		{
			int length = dis.readInt();

			Object[] result = new Object[length];

			for (int i = 0; i < length; i++)
			{
				Object element = Array.get(value, i);
				result[i] = readToField(dis, element);
			}

			return result;
		}

		if (value instanceof Enum<?>)
		{
			Enum<?> e = (Enum<?>) value;
			return Enum.valueOf(e.getClass(), readString(dis));
		}

		switch (value.getClass().getSimpleName())
		{
		case "Double":
			return dis.readDouble();
		case "Float":
			return dis.readFloat();
		case "Long":
			return dis.readLong();
		case "Integer":
			return dis.readInt();
		case "Short":
			return dis.readShort();
		case "Byte":
			return dis.readByte();
		case "Boolean":
			return dis.readBoolean();
		case "String":
			return readString(dis);
		case "Character":
			return dis.readChar();
		case "Vector2i":
			return new Vector2i(dis.readInt(), dis.readInt());
		case "Vector2f":
			return new Vector2f(dis.readFloat(), dis.readFloat());
		case "Vector3f":
			return new Vector3f(dis.readFloat(), dis.readFloat(), dis.readFloat());
		case "Vector4f":
			return new Vector4f(dis.readFloat(), dis.readFloat(), dis.readFloat(), dis.readFloat());
		case "Quaternionf":
			return new Quaternionf(dis.readFloat(), dis.readFloat(), dis.readFloat(), dis.readFloat());
		case "LinkedTreeMap":
		case "HashMap":
		{
			HashMap<String, String> tags = new HashMap<>();
			int nEntries = dis.read();
			
			for(int i = 0; i < nEntries; i++)
			{
				String k = readString(dis);
				String v = readString(dis);
				
				tags.put(k, v);
			}
			
			return tags;
		}
		default:
			System.err.println("Unknown field: " + value.getClass().getSimpleName());
		}
		
		return null;
	}

	private static String readString(DataInputStream dis) throws IOException
	{
		StringBuilder sb = new StringBuilder();

		int byteRead;
		while ((byteRead = dis.read()) != 0)
			sb.append((char) byteRead);

		return sb.toString();
	}
}
