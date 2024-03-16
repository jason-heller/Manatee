package manatee.cache.definitions.binfile;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;

import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class LumpSerializer
{
	public static byte[] serialize(Object obj) throws IOException
	{

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		Class<?> lumpClass = obj.getClass();

		if (lumpClass.isArray())
		{
			int length = Array.getLength(obj);
			for (int i = 0; i < length; i++)
			{
				Object element = Array.get(obj, i);

				dos.write(serialize(element));
			}

			return baos.toByteArray();
		}

		try
		{

			Field[] fields = lumpClass.getFields();

			for (Field field : fields)
			{
				parseField(dos, field.get(obj));
			}
			
			dos.close();

			return baos.toByteArray();

		}
		catch (IllegalArgumentException | IllegalAccessException | IOException e)
		{
			e.printStackTrace();
		}
		
		dos.close();

		return null;
	}

	private static void parseField(DataOutputStream dos, Object obj)
			throws IOException, IllegalArgumentException, IllegalAccessException
	{
		// If we have an array, write the length as an int then run its element (ascending) thru this method
		if (obj != null && obj.getClass().isArray())
		{
			int length = Array.getLength(obj);
			dos.writeInt(length);
			
			for (int i = 0; i < length; i++)
			{
				Object element = Array.get(obj, i);
				parseField(dos, element);
			}
			return;
		}

		if (obj == null)
		{
			dos.writeByte(0); // Assume its a string.
			return;
		}

		if (obj instanceof Enum<?>)
		{
			// dos.writeChars(((Enum<?>)field).name());
			// dos.writeByte(0);
			dos.writeByte(((Enum<?>) obj).ordinal());
			return;
		}

		switch (obj.getClass().getSimpleName())
		{
		case "Double":
			dos.writeDouble((Double) obj);
			break;
		case "Float":
			dos.writeFloat((Float) obj);
			break;
		case "Long":
			dos.writeLong((Long) obj);
			break;
		case "Integer":
			dos.writeInt((Integer) obj);
			break;
		case "Short":
			dos.writeShort((Short) obj);
			break;
		case "Byte":
			dos.writeByte((Byte) obj);
			break;
		case "Boolean":
			dos.writeBoolean((Boolean) obj);
			break;
		case "String":
		{
			String s = ((String) obj);
			if (s != null)
				dos.writeChars(s);
			dos.writeByte(0);
			break;
		}
		case "Character":
			dos.writeChar((Character) obj);
			break;
		case "Vector2i":
		{
			Vector2i v = (Vector2i)obj;
			dos.writeInt(v.x);
			dos.writeInt(v.y);
			break;
		}
		case "Vector2f":
		{
			Vector2f v = (Vector2f)obj;
			dos.writeFloat(v.x);
			dos.writeFloat(v.y);
			break;
		}
		case "Vector3f":
		{
			Vector3f v = (Vector3f)obj;
			dos.writeFloat(v.x);
			dos.writeFloat(v.y);
			dos.writeFloat(v.z);
			break;
		}
		case "Vector4f":
		{
			Vector4f v = (Vector4f)obj;
			dos.writeFloat(v.x);
			dos.writeFloat(v.y);
			dos.writeFloat(v.z);
			dos.writeFloat(v.w);
			break;
		}
		case "Quaternionf":
		{
			Quaternionf v = (Quaternionf)obj;
			dos.writeFloat(v.x);
			dos.writeFloat(v.y);
			dos.writeFloat(v.z);
			dos.writeFloat(v.w);
			break;
		}
		case "HashMap":
		case "LinkedTreeMap":
		{
			@SuppressWarnings("unchecked")
			Map<String, String> tags = (Map<String, String>)obj;
			dos.writeByte(tags.entrySet().size());
			
			for(Entry<String, String> entry : tags.entrySet())
			{
				dos.writeChars(entry.getKey());
				dos.writeByte(0);
				
				dos.writeChars(entry.getValue());
				dos.writeByte(0);
			}
			
			break;
		}

		default:
			System.err.println("Unknown field: " + obj.getClass().getSimpleName());
		}
	}
}
