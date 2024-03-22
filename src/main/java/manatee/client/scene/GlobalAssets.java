package manatee.client.scene;

import org.joml.Vector3f;
import org.joml.Vector4f;

import manatee.cache.definitions.Model;
import manatee.cache.definitions.mesh.GenericMesh;
import manatee.cache.definitions.mesh.IMesh;
import manatee.cache.definitions.sound.Sound;
import manatee.cache.definitions.texture.ITexture;
import manatee.maths.MCache;

public class GlobalAssets extends Assets
{
	public static final Vector4f MISSING_COLOR = new Vector4f(1f, 1f, 1f, 1f);

	public static IMesh MISSING_MESH;
	public static IMesh BILLBOARD_MESH;
	
	public static ITexture MISSING_TEX;
	public static ITexture NO_TEX;
	public static ITexture PARTICLE_GENERIC;
	
	public static Model MISSING_MODEL;
	
	public static Sound MISSING_SOUND;
	
	@Override
	public void loadAssets()
	{
		MISSING_MESH = loadMesh("mesh/error.obj");
		BILLBOARD_MESH = createBillboard();
		
		MISSING_TEX = loadTexture2D("texture/default.png");
		NO_TEX = loadTexture2D("texture/textureless_fallback.png");
		
		MISSING_MODEL = new Model(MISSING_MESH, NO_TEX);

		MISSING_SOUND = loadSound("sound/missing_audio.ogg");
		
		PARTICLE_GENERIC = loadTexture2D("texture/particle/particles.png");
	}

	private IMesh createBillboard()
	{
		final float[] vertices =
		{
				0.5f, -0.5f, 0, 1f, 1f, 0, 0, 1,
				0.5f, 0.5f, 0, 1f, 0f, 0, 0, 1,
				-0.5f, 0.5f, 0, 0f, 0f, 0, 0, 1,
				-0.5f, -0.5f, 0, 0f, 1f, 0, 0, 1,
		};
		
		final int[] indices =
		{
				0, 1, 3, 3, 1, 2
		};


		GenericMesh billboard = new GenericMesh(vertices, indices, MCache.ONE);

		billboard.setBounds(new Vector3f(-0.5f, -0.5f, -0.5f), new Vector3f(0.5f, 0.5f, 0.5f));
		
		addMesh(billboard);
		
		return billboard;
	}
}
