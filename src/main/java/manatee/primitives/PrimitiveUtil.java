package manatee.primitives;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import manatee.util.VecBuffer3;

public class PrimitiveUtil
{
	public static PrimitiveVao createCone(int resolution)
	{
		final float radius = .275f;
		final float TWOPI = (float) (Math.PI * 2);

		final float tailWidth = .015f;
		
		VecBuffer3 positions = new VecBuffer3(resolution + 1 + 8);
		VecBuffer3 indices = new VecBuffer3(resolution + 4);
		
		// Just the tip
		for (int i = 0; i < resolution; i++)
		{
			float angle = (i / (float)resolution) * TWOPI;
			float x = (float) Math.cos(angle);
			float y = (float) Math.sin(angle);

			positions.putf(x * radius, y * radius, 1);

			indices.puti(resolution, i, (1 + i) % resolution);
		}
		
		positions.putf(0, 0, 1.5f);
		
		positions.putf( tailWidth,  tailWidth, 1f);
		positions.putf(-tailWidth, -tailWidth, 1f);
		positions.putf(-tailWidth, -tailWidth, 0);
		positions.putf( tailWidth,  tailWidth, 0);
		
		positions.putf(-tailWidth,  tailWidth, 1f);
		positions.putf( tailWidth, -tailWidth, 1f);
		positions.putf( tailWidth, -tailWidth, 0);
		positions.putf(-tailWidth,  tailWidth, 0);
		
		indices.puti(resolution + 1, resolution + 2, resolution + 3);
		indices.puti(resolution + 1, resolution + 3, resolution + 4);
		
		indices.puti(resolution + 5, resolution + 6, resolution + 7);
		indices.puti(resolution + 5, resolution + 7, resolution + 8);
		
		PrimitiveVao p = new PrimitiveVao(positions.asFloatBuffer(), indices.asIntBuffer());
		
		positions.free();
		indices.free();
		
		return p;
	}
	
	public static PrimitiveVao createSphere(float resolution)
	{
		final float radius = 1f;

		List<Vector3f> positions = new ArrayList<>();
		List<Integer> indices = new ArrayList<>();

		int circCnt = (int) (resolution + .1f);
		if (circCnt < 4)
			circCnt = 4;
		int circCnt_2 = circCnt / 2;
		int layerCount = (int) (resolution + .1f);
		if (layerCount < 2)
			layerCount = 2;

		for (int tbInx = 0; tbInx <= layerCount; tbInx++)
		{
			// float v = (1f - (float) tbInx / layerCount);
			float z = (float) Math.sin((1.0 - 2.0 * tbInx / layerCount) * Math.PI / 2.0);
			float cosUp = (float) Math.sqrt(1.0 - z * z);

			for (int i = 0; i <= circCnt_2; i++)
			{
				float u = (float) i / (float) circCnt_2;
				float angle = (float) Math.PI * u;
				float x = (float) Math.cos(angle) * cosUp;
				float y = (float) Math.sin(angle) * cosUp;
				positions.add(new Vector3f(x * radius, y * radius, z * radius));
			}
			for (int i = 0; i <= circCnt_2; i++)
			{
				float u = (float) i / (float) circCnt_2;
				float angle = (float) (Math.PI * u + Math.PI);
				float x = (float) Math.cos(angle) * cosUp;
				float y = (float) Math.sin(angle) * cosUp;
				positions.add(new Vector3f(x * radius, y * radius, z * radius));
			}
		}

		int circSize_2 = circCnt_2 + 1;
		int circSize = circSize_2 * 2;
		for (int i = 0; i < circCnt_2; i++)
		{
			indices.add(circSize + i);
			indices.add(circSize + i + 1);
			indices.add(i);
		}
		for (int i = circCnt_2 + 1; i < 2 * circCnt_2 + 1; i++)
		{
			indices.add(circSize + i);
			indices.add(circSize + i + 1);
			indices.add(i);
		}

		for (int tbInx = 1; tbInx < layerCount - 1; tbInx++)
		{
			int ringStart = tbInx * circSize;
			int nextRingStart = (tbInx + 1) * circSize;
			for (int i = 0; i < circCnt_2; i++)
			{
				indices.add(ringStart + i);
				indices.add(nextRingStart + i);
				indices.add(ringStart + i + 1);

				indices.add(ringStart + i + 1);
				indices.add(nextRingStart + i);
				indices.add(nextRingStart + i + 1);
			}
			ringStart += circSize_2;
			nextRingStart += circSize_2;
			for (int i = 0; i < circCnt_2; i++)
			{

				indices.add(ringStart + i);
				indices.add(nextRingStart + i);
				indices.add(ringStart + i + 1);

				indices.add(ringStart + i + 1);
				indices.add(nextRingStart + i);
				indices.add(nextRingStart + i + 1);
			}
		}

		int start = (layerCount - 1) * circSize;
		for (int i = 0; i < circCnt_2; i++)
		{
			indices.add(start + i + 1);
			indices.add(start + i);
			indices.add(start + i + circSize);
		}

		for (int i = circCnt_2 + 1; i < 2 * circCnt_2 + 1; i++)
		{
			indices.add(start + i + 1);
			indices.add(start + i);
			indices.add(start + i + circSize);
		}

		float[] pos = new float[positions.size() * 3];
		int[] inds = new int[indices.size()];

		for (int i = 0; i < positions.size(); ++i)
		{
			pos[i * 3] = positions.get(i).x;
			pos[i * 3 + 1] = positions.get(i).y;
			pos[i * 3 + 2] = positions.get(i).z;
		}

		for (int i = 0; i < inds.length; ++i)
			inds[i] = indices.get(i);

		// System.out.println(inds.length);
		// System.out.println(pos.length);

		return new PrimitiveVao(pos, inds);
	}
}
