package manatee.cache.definitions.binfile;

import java.io.ByteArrayOutputStream;

public class BinaryFileUtil
{
	public static byte[] convertToByteArr(float[][] arr)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		for (float[] subArr : arr)
		{
			for(float value : subArr)
			{
				int intValue = Float.floatToIntBits((Float)value);
				
				baos.write((intValue >> 24));
				baos.write((intValue >> 16));
				baos.write((intValue >> 8));
				baos.write(intValue);
			}
		}
		
		return baos.toByteArray();
	}
	
	public static byte[] convertToByteArr(int[][] arr)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		for (int[] subArr : arr)
		{
			for(int value : subArr)
			{
				int intValue = ((Integer) value).intValue();
				
				baos.write((intValue >> 24));
				baos.write((intValue >> 16));
				baos.write((intValue >> 8));
				baos.write(intValue);
			}
		}
		
		return baos.toByteArray();
	}

	public static float[][] toFloat2d(byte[] data, int w, int h)
	{
		float[][] value = new float[w][h];
		
		int ptr = 0;
		
		for(int i = 0; i < w; i++)
		{
			for(int j = 0; j < h; j++)
			{
				value[i][j] = Float.intBitsToFloat(data[ptr++] << 24 | data[ptr++] << 16 | data[ptr++] << 8 | data[ptr++]);
			}
		}
		
		return value;
	}
	
	public static int[][] toInt2d(byte[] data, int w, int h)
	{
		int[][] value = new int[w][h];
		int ptr = 0;
		
		for(int i = 0; i < w; i++)
		{
			for(int j = 0; j < h; j++)
			{
				value[i][j] = data[ptr++] << 24 | data[ptr++] << 16 | data[ptr++] << 8 | data[ptr++];
			}
		}
		
		return value;
	}
}
