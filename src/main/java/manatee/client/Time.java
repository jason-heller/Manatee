package manatee.client;

import org.lwjgl.nanovg.NanoVG;

import manatee.client.dev.Command;

public class Time
{
	private static int targetFramerate;

	private ClientSync sync;

	private int frameCounter = 0;

	private long lastFPSQueryTime;
	private long time;
	private long lastTime;

	public static float deltaTime;

	public static float timeScale = 1f;

	public static float averageFps;
	public static int[] frameDeltas = new int[120];

	public static int fps = 0;
	public static double captureFramerate = 0;

	public Time(int targetFramerate)
	{
		setTargetFramerate(targetFramerate);

		sync = new ClientSync();

		Command.add("fps", Command.INT_SYNTAX, this, "setTargetFramerate", false);

		lastTime = System.currentTimeMillis();
	}

	public void update()
	{
		frameCounter++;

		time = System.currentTimeMillis();

		deltaTime = (time - lastTime) / 1000f * timeScale;
		captureFramerate = ((time - lastTime) / 1000.0 * timeScale);

		lastTime = time;

		if (time >= lastFPSQueryTime + 1000)
		{
			lastFPSQueryTime = time;

			fps = frameCounter;
			frameCounter = 0;
		}

		for(int i = 1; i < frameDeltas.length; i++)
			frameDeltas[i - 1] = frameDeltas[i];
		
		frameDeltas[frameDeltas.length - 1] = (int)((captureFramerate - 0.1) * 200.0);

		if (targetFramerate > 0)
			sync.sync(targetFramerate);
	}

	public void setTargetFramerate(int targetFramerate)
	{
		Time.targetFramerate = Math.max(targetFramerate, 4);
	}

	public static void drawFramerateBox(long vg, int x, int y)
	{
		NanoVG.nvgBeginPath(vg);
		NanoVG.nvgRect(vg, x, y, frameDeltas.length * 2, 100);
		NanoVG.nvgFill(vg);
		
		int yBtm = y + 50;
		
		for(int i = 1; i < frameDeltas.length; i++)
		{
			int pos = x + ((i - 1) * 2);
			int y1 = yBtm + frameDeltas[i - 1];
			int y2 = yBtm + frameDeltas[i];
			
			NanoVG.nvgBeginPath(vg);
			NanoVG.nvgMoveTo(vg, pos, y1); // Starting point
            NanoVG.nvgLineTo(vg, pos + 2, y2); // Ending point
            //NanoVG.nvgStrokeColor(vg, color);
            //NanoVG.nvgStrokeWidth(vg, 1.0f);
            NanoVG.nvgStroke(vg);
		}
	}
}
